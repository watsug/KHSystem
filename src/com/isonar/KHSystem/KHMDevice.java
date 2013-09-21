package com.isonar.KHSystem;

import android.hardware.usb.UsbManager;
import android.util.Log;

import java.io.IOException;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;

/**
 * Created by watsug on 9/20/13.
 */
public class KHMDevice {

    private final String TAG = "KHMDevice";
    private final int DIGITAL_MESSAGE   = 0x90; // send data for a digital port


    private UsbManager usbMgr;
    public KHMDevice(UsbManager usbMgr) {
        this.usbMgr = usbMgr;
    }

    private UsbSerialDriver driver = null;
    private int usbDeviceId = -1;

    public boolean initialize() {
        try {
            // Find the first available driver.
            driver = UsbSerialProber.acquire(usbMgr);
            if (null == driver) {
                return false;
            }
            try {
                usbDeviceId = driver.getDevice().getDeviceId();
                driver.open();
                driver.setBaudRate(115200);

                byte buffer[] = new byte[16];
                int numBytesRead = driver.read(buffer, 1000);
                Log.d(TAG, "Read " + numBytesRead + " bytes.");
                return true;
            } catch (IOException e) {
                // Deal with error.
                Log.e(TAG, e.getMessage(), e);
                driver = null; usbDeviceId = -1;
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            return false;
        }
    }

    public boolean active() {
        try {
            if (null == driver) return false;
            android.hardware.usb.UsbDevice dev = driver.getDevice();
            return usbDeviceId == dev.getDeviceId();
        } catch (Exception ex) {
            Log.w(TAG, ex.getMessage(), ex);
            return false;
        }
    }

    public boolean release() {
        try {
            if (null == driver) return true;
            driver.close();
            return true;
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage(), ex);
            return false;
        } finally {
            driver = null;
        }
    }

    public boolean khmMessage(int cmd, int value) {
        try {
            byte[] message = new byte[3];
            int newCmd = cmd & 0x0f;
            message[0] = (byte)(DIGITAL_MESSAGE | newCmd);
            message[1] = (byte)(value & 0x7F);
            message[2] = (byte)(value >> 7);

            int written = driver.write(message, 100);
            return written == message.length;
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage(), ex);
            return false;
        }
    }

}
