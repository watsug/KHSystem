package com.isonar.KHSystem;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

/**
 * Created by Adam on 2014-11-17.
 */
public class SplashScreen extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        ActivityStarter starter = new ActivityStarter();
        starter.start();
    }

    private class ActivityStarter extends Thread {

        @Override
        public void run() {
            try {
                ProgressBar mProgress = (ProgressBar) findViewById(R.id.progressBar);
                if (null != mProgress)
                {
                    mProgress.setIndeterminate(true);
                }
                int counter = 20;
                // check for SD card availability
                while (((counter--) < 0) && !KHMConfig.isStorageMounted())
                {
                    Thread.sleep(100);
                }
            } catch (Exception e) {
                Log.e("SplashScreen", e.getMessage());
            }

            // switch to main activity
            Intent intent = new Intent(SplashScreen.this, MyActivity.class);
            SplashScreen.this.startActivity(intent);
            SplashScreen.this.finish();
        }
    }
}