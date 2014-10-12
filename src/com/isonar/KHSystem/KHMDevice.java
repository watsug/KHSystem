package com.isonar.KHSystem;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

/**
 * Created by watsug on 9/20/13.
 */
public class KHMDevice {

    private final String TAG = "KHMDevice";
    private final int DIGITAL_MESSAGE   = 0x90; // send data for a digital port


    public static final int MSG_ELEVATOR = 0x00;
    public static final int CMD_ELEVATOR_UP = 0x01;
    public static final int CMD_ELEVATOR_DOWN = 0x02;
    public static final int CMD_ELEVATOR_STOP = 0x00;

    public static final int MSG_TIMER = 0x01;
    public static final int CMD_TIMER_STOP = 0x00;
    public static final int CMD_TIMER_START = 0x01;
    public static final int CMD_TIMER_RESTART = 0x02;
    public static final int CMD_TIMER_5M_ON = 0x03;
    public static final int CMD_TIMER_5M_OFF = 0x04;

    private UsbManager usbMgr;
    public KHMDevice(UsbManager usbMgr) {
        this.usbMgr = usbMgr;
    }
    private UsbSerialPort port = null;

    public boolean initialize() {
        try {
            // Find the first available driver.
            List<UsbSerialDriver> drivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbMgr);
            if (drivers.isEmpty()) return false;
            // TODO: more devices???
            List<UsbSerialPort> ports = drivers.get(0).getPorts();
            if (ports.isEmpty()) return false;
            port = ports.get(0);
            if (null == port) {
                return false;
            }

            UsbDeviceConnection connection = usbMgr.openDevice(port.getDriver().getDevice());
            if (connection == null) return false;

            try {
                //usbDeviceId = port. driver.getDevice().getDeviceId();
                port.open(connection);
                port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

                byte buffer[] = new byte[16];
                int numBytesRead = port.read(buffer, 1000);
                Log.d(TAG, "Read " + numBytesRead + " bytes.");
                return true;
            } catch (IOException e) {
                // Deal with error.
                Log.e(TAG, e.getMessage(), e);
                port = null;
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            return false;
        }
    }

    public boolean active() {
        return null != port;
    }

    public boolean release() {
        try {
            if (null == port) return true;
            port.close();
            return true;
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage(), ex);
            return false;
        } finally {
            port = null;
        }
    }

    public boolean timer(int value) {
        return khmMessage(MSG_TIMER, value);
    }

    public boolean elevator(int value) {
        return khmMessage(MSG_ELEVATOR, value);
    }

    public boolean khmMessage(int cmd, int value) {
        try {
            byte[] message = new byte[3];
            int newCmd = cmd & 0x0f;
            message[0] = (byte)(DIGITAL_MESSAGE | newCmd);
            message[1] = (byte)(value & 0x7F);
            message[2] = (byte)(value >> 7);

            int written = port.write(message, 100);
            return written == message.length;
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage(), ex);
            try {
                port.close();
            } catch (IOException e) {}
            port = null;
            return false;
        }
    }

}
