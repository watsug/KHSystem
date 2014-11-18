package com.isonar.KHSystem;

/**
 * Created by Adam on 2014-11-17.
 */
public class ElevatorCmd {
    private final int MAX_RETRY = 5;
    private int prevCmd = 0;
    private int currentCmd = 0;
    private int counter = 0;

    public ElevatorCmd()
    {
        counter = MAX_RETRY;
    }

    public void setCmd(int cmd, boolean force)
    {
        prevCmd = currentCmd;
        currentCmd = cmd;
        counter = (prevCmd != currentCmd || force) ? MAX_RETRY : 0;
    }

    public int getCmd()
    {
        return currentCmd;
    }

    public boolean resend(boolean decrement)
    {
        if (counter > 0)
        {
            counter --;
            return true;
        }
        return false;
    }
}
