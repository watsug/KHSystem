/*
  Copyright (C) 2006-2008 Hans-Christoph Steiner.  All rights reserved.

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  See file LICENSE.txt for further informations on licensing terms.

  formatted using the GNU C formatting and indenting
*/

/*
 * TODO: use Program Control to load stored profiles from EEPROM
 */

#include <Firmata.h>

#define MSG_ELEVATOR        0x00
#define CMD_ELEVATOR_UP     0x01
#define CMD_ELEVATOR_DOWN   0x02
#define CMD_ELEVATOR_STOP   0x00

#define MSG_TIMER           0x01
#define CMD_TIMER_STOP      0x00
#define CMD_TIMER_START     0x01
#define CMD_TIMER_RESTART   0x02
#define CMD_TIMER_5M_ON     0x03
#define CMD_TIMER_5M_OFF    0x04

#define LED_OFF 0
#define LED_ON  1
#define LED_BLINK 2

#define BTN_EXECUTION_COUNTER 3

/*==============================================================================
 * GLOBAL VARIABLES
 *============================================================================*/

/*==============================================================================
 * Elevator
 *============================================================================*/
unsigned long elevatorPrevMills = 0;       // for comparison with currentMillis
unsigned long oneSecondMills = 0;       // for comparison with currentMillis
const int ELEVATOR_MAX_TIME = 200;
const int BLINK_TIME = 500;

volatile bool elevatorRunning = false;
const int elevatorUp = 12;
const int elevatorDown = 13;
const int elevatorEnable = 11;

void SetupElevator();
void RunElevatorUp();
void RunElevatorDown();
void StopElevator();
void OnElevatorCmd(int cmd);

/*==============================================================================
 * Timer
 *============================================================================*/
volatile int timerLed = LED_OFF;
volatile bool blinking = false;
const int timerStart = 9;
const int timerStop = 10;
const int timerEnable = 8;
const int timer5M = 7;
const int buzzerPin = 6;
const int timerLedPin = 5;

void SetupTimer();
void StartTimer();
void StopTimer();
void RestartTimer();
void Line5M(bool enable);
void OnTimeCmd(int cmd);

/*==============================================================================
 * Panel
 *============================================================================*/
const int buttonUpPin = 2;
const int buttonDownPin = 3;
const int buttonTimerResetPin = 4;

void SetupPanel();
void ProcessButtons();
void TimerLed(bool on);
void Beep(int count);

/*==============================================================================
 * FUNCTIONS
 *============================================================================*/
void khmWriteCallback(byte cmd, int value) {
    switch (cmd) {
    case MSG_ELEVATOR:
      OnElevatorCmd(value);
      break;
    case MSG_TIMER:
      OnTimeCmd(value);
      break;
    }
}

/*==============================================================================
 * SYSEX-BASED commands
 *============================================================================*/
void sysexCallback(byte command, byte argc, byte *argv) {
  switch(command) {
  case CAPABILITY_QUERY:
    Serial.write(START_SYSEX);
    Serial.write(CAPABILITY_RESPONSE);

    // DIGITAL_0 - Elevator UP/DOWN
    Serial.write(OUTPUT);
    Serial.write(8);
    Serial.write(127);

    // DIGITAL_1 - Timer stop/start/reset
    Serial.write(OUTPUT);
    Serial.write(8);
    Serial.write(127);

    // DIGITAL_2 - 5 Minutes signal
    Serial.write(OUTPUT);
    Serial.write(1);
    Serial.write(127);

    Serial.write(END_SYSEX);
    break;
  }
}


/*==============================================================================
 * SETUP()
 *============================================================================*/
void setup()
{
  SetupElevator();
  SetupTimer();
  SetupPanel();

  Firmata.setFirmwareVersion(0, 2);
  Firmata.attach(START_SYSEX, sysexCallback);
  Firmata.attach(DIGITAL_MESSAGE, khmWriteCallback);

  Firmata.begin(115200);
}

/*==============================================================================
 * LOOP()
 *============================================================================*/
void loop() {
  /* SERIALREAD - processing incoming messagse as soon as possible, while still
   * checking digital inputs.  */
  while(Firmata.available()) {
    Firmata.processInput();
  }

  unsigned long currentMillis = millis();
  if (currentMillis - elevatorPrevMills > ELEVATOR_MAX_TIME) {
    elevatorPrevMills += ELEVATOR_MAX_TIME;
    StopElevator();
  }
  if (currentMillis - oneSecondMills > BLINK_TIME) {
    blinking = !blinking;
    oneSecondMills += BLINK_TIME;
    switch (timerLed) {
    case LED_OFF:
      TimerLed(false);
      break;
    case LED_ON:
      TimerLed(true);
      break;
    case LED_BLINK:
      TimerLed(blinking);
      break;
    }
  }
  ProcessButtons();
}


void SetupElevator() {
  pinMode(elevatorUp,OUTPUT);
  pinMode(elevatorDown,OUTPUT);
  pinMode(elevatorEnable,OUTPUT);
}
void RunElevatorUp() {
  digitalWrite(elevatorUp, HIGH);
  digitalWrite(elevatorDown, LOW);
  digitalWrite(elevatorEnable, HIGH);
  elevatorPrevMills = millis();
}
void RunElevatorDown() {
  digitalWrite(elevatorUp, LOW);
  digitalWrite(elevatorDown, HIGH);
  digitalWrite(elevatorEnable, HIGH);
  elevatorPrevMills = millis();
}
void StopElevator() {
  digitalWrite(elevatorUp, LOW);
  digitalWrite(elevatorDown, LOW);
  digitalWrite(elevatorEnable, LOW);
}
void OnElevatorCmd(int cmd) {
  switch (cmd) {
  case CMD_ELEVATOR_UP:
    elevatorRunning = true;
    RunElevatorUp();
    Firmata.sendString("UP");
    break;
  case CMD_ELEVATOR_DOWN:
    elevatorRunning = true;
    RunElevatorDown();
    Firmata.sendString("DOWN");
    break;
  case CMD_ELEVATOR_STOP:
    elevatorRunning = false;
    StopElevator();
    Firmata.sendString("STOP");
    break;
  }
}

void SetupTimer() {
  pinMode(timerStart,OUTPUT);
  pinMode(timerStop,OUTPUT);
  pinMode(timerEnable,OUTPUT);
  pinMode(timer5M,OUTPUT);
  OnTimeCmd(CMD_TIMER_RESTART);
}
void StartTimer() {
  digitalWrite(timerStart, HIGH);
  digitalWrite(timerStop, LOW);
  digitalWrite(timerEnable, HIGH);
  timerLed = LED_BLINK;
}
void StopTimer() {
  digitalWrite(timerStart, LOW);
  digitalWrite(timerStop, HIGH);
  digitalWrite(timerEnable, HIGH);
  timerLed = LED_OFF;
}
void RestartTimer() {
  StopTimer();
  delay(500);
  StartTimer();
}
void Line5M(bool enable) {
  digitalWrite(timer5M, enable ? HIGH : LOW);
}

void OnTimeCmd(int cmd) {
  switch (cmd) {
  case CMD_TIMER_STOP:
    StopTimer();
    Firmata.sendString("STOP");
    break;
  case CMD_TIMER_START:
    StartTimer();
    Firmata.sendString("START");
    break;
  case CMD_TIMER_RESTART:
    RestartTimer();
    Firmata.sendString("RESTART");
    break;
  case CMD_TIMER_5M_ON:
    Line5M(true);
    Firmata.sendString("5M ON");
    break;
  case CMD_TIMER_5M_OFF:
    Line5M(false);
    Firmata.sendString("5M OFF");
    break;
  }
}

void SetupPanel() {
  pinMode(buttonUpPin,INPUT);
  pinMode(buttonDownPin,INPUT);
  pinMode(buttonTimerResetPin,INPUT);
  pinMode(timerLedPin,OUTPUT);

  pinMode(buzzerPin,OUTPUT);
  digitalWrite(buzzerPin, LOW);
  Beep(1);
}

int resetPinCounter = 0;
void ProcessButtons() {
  if (1 == digitalRead(buttonUpPin)) {
    OnElevatorCmd(CMD_ELEVATOR_UP);
  } else if (1 == digitalRead(buttonDownPin)) {
    OnElevatorCmd(CMD_ELEVATOR_DOWN);
  }
  if (!elevatorRunning) {
    if (1 == digitalRead(buttonTimerResetPin)) {
      if (resetPinCounter > BTN_EXECUTION_COUNTER) {
        timerLed = LED_ON;
      }
      resetPinCounter ++;
    } else {
      if (resetPinCounter > BTN_EXECUTION_COUNTER) {
        OnTimeCmd(CMD_TIMER_RESTART);
        Beep(2);
        resetPinCounter = 0;
      }
    }
  }
}

void TimerLed(bool on) {
  digitalWrite(timerLedPin, on ? HIGH : LOW);
}

void Beep(int count) {
  for (int i = 0; i < count; i++) {
    delay(100);
    tone(buzzerPin,2048,100);
    delay(100);
    noTone(buzzerPin);
  }
}
