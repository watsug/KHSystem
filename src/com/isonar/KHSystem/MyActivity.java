package com.isonar.KHSystem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.hardware.usb.UsbManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class MyActivity extends Activity implements MediaPlayer.OnCompletionListener {
    private final String TAG = "KHMActivity";

    private Button btnSong1;
    private Button btnSong2;
    private Button btnSong3;
    private Button btnTimerStart;
    private Button btnTimer5Min;
    private ImageButton btnPlay;
    private ImageButton btnRepeat;
    private ImageButton btnElevatorUp;
    private ImageButton btnElevatorDown;
    private Chronometer khmTimer;
    private Chronometer nonstopTimer;
    ListView songsList;
    SongBase songBase = new SongBase(null);
    private SongsListAdapter songsAdapter = null;
    SeekBar seekBar;

    private MediaPlayer mediaPlayer = new MediaPlayer();
    private boolean continousPlay = false;
    private boolean started = false;
    private boolean fiveMinutEnabled = false;
    private Timer myTimer;
    private Timer myTimer2;
    private final Handler handler = new Handler();

    private boolean songStarted = false;
    private KHMDevice khmDev;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            if (!KHMConfig.existsKHMDir()) {
                File path = KHMConfig.getKHMDir();
                showError(String.format("Data directory not found: %s!",path.getPath()));
            }
        } catch (Exception ex) {
            showError("Internal error: " + ex.toString());
        }

        int densityDpi = KHMConfig.getDensityDPI();
        if (densityDpi > 0) {
            Resources res = this.getResources();
            DisplayMetrics metrics = res.getDisplayMetrics();
            if (null != metrics) {
                getWindowManager().getDefaultDisplay().getMetrics(metrics);
                //String tmp = String.format("density: %f\n", metrics.density);
                //tmp = tmp + String.format("densityDpi: %d\n", metrics.densityDpi);
                //tmp = tmp + String.format("widthPixels: %d\n", metrics.widthPixels);
                //tmp = tmp + String.format("scaledDensity: %f\n", metrics.scaledDensity);
                //showError("metrics: \n" + tmp);
                metrics.density = (float)densityDpi/160.0f;
                metrics.densityDpi = densityDpi;
                metrics.scaledDensity = (float)densityDpi/160.0f;
                res.updateConfiguration(res.getConfiguration(),metrics);
            }
        }

        setContentView(R.layout.main);
        setUpView();

        try {
            // Get UsbManager from Android.
            UsbManager mgr = (UsbManager) getSystemService(Context.USB_SERVICE);
            khmDev = new KHMDevice(mgr);
        } catch (Exception ex) {
            Log.w(TAG, ex.getMessage(), ex);
        }

        myTimer = new Timer();
        myTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                TimerMethod();
            }
        }, 0, 200);
        myTimer2 = new Timer();
        myTimer2.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                TimerMethod2();
            }
        }, 0, 150);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * Called when the activity is destroyed.
     */
    @Override
    public void onDestroy() {
        if (null != mediaPlayer && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.warnOnExit)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MyActivity.this.finish();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }



    private void setUpView() {
        try {
            btnSong1 = (Button)this.findViewById(R.id.btnSong1);
            btnSong2 = (Button)this.findViewById(R.id.btnSong2);
            btnSong3 = (Button)this.findViewById(R.id.btnSong3);
            btnTimerStart = (Button)this.findViewById(R.id.btnTimerStart);
            btnTimer5Min = (Button)this.findViewById(R.id.btnTimer5Min);
            khmTimer = (Chronometer)this.findViewById(R.id.khmTimer);
            nonstopTimer = (Chronometer)this.findViewById(R.id.nonstopTimer);
            btnPlay = (ImageButton)this.findViewById(R.id.btnPlay);
            btnRepeat = (ImageButton)this.findViewById(R.id.btnRepeat);
            seekBar = (SeekBar)this.findViewById(R.id.seekBar);
            btnElevatorUp = (ImageButton)this.findViewById(R.id.btnElevatorUp);
            btnElevatorDown = (ImageButton)this.findViewById(R.id.btnElevatorDown);
            songsList = (ListView)findViewById(R.id.listView);

            khmTimer.setBase(SystemClock.elapsedRealtime());
            btnTimer5Min.setText(R.string.fiveMin);


            try {
                songBase.build();
                songsAdapter = new SongsListAdapter(MyActivity.this,R.layout.songs_list,songBase.getSongs());
                songsList.setAdapter(songsAdapter);
            } catch (Exception ex) {
                Log.w(TAG, ex.getMessage(), ex);
            }

            try {
                songsList.setOnItemClickListener(new ListView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> a, View v, int position, long l) {
                        SongItem song = null;
                        if (position >= 0) {
                            song = (SongItem)a.getItemAtPosition(position);
                            songsList.setItemChecked(position,true);
                        }
                        songsAdapter.selectSong(song);
                        v.refreshDrawableState();
                    }
                });
            } catch (Exception ex) {
                Log.w(TAG, ex.getMessage(), ex);
            }

            btnSong1.setLongClickable(true);
            btnSong1.setOnLongClickListener((new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    SongItem song = songsAdapter.getSelectedSong();
                    btnSong1.setText(null != song ? String.format("%d",song.getNumber()) : "P1");
                    return true;
                }
            }));
            btnSong1.setOnClickListener((new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        int idx = Integer.parseInt(btnSong1.getText().toString());
                        selectSong(idx);
                    } catch (Exception ex) {
                    }
                }
            }));
            btnSong2.setLongClickable(true);
            btnSong2.setOnLongClickListener((new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    SongItem song = songsAdapter.getSelectedSong();
                    btnSong2.setText(null != song ? String.format("%d",song.getNumber()) : "P2");
                    return true;
                }
            }));
            btnSong2.setOnClickListener((new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        int idx = Integer.parseInt(btnSong2.getText().toString());
                        selectSong(idx);
                    } catch (Exception ex) {
                    }
                }
            }));
            btnSong3.setLongClickable(true);
            btnSong3.setOnLongClickListener((new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    SongItem song = songsAdapter.getSelectedSong();
                    btnSong3.setText(null != song ? String.format("%d",song.getNumber()) : "P3");
                    return true;
                }
            }));
            btnSong3.setOnClickListener((new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        int idx = Integer.parseInt(btnSong3.getText().toString());
                        selectSong(idx);
                    } catch (Exception ex) {
                    }
                }
            }));

            btnTimerStart.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    onTimerButton();
                }
            });

            if (null != nonstopTimer) {
                nonstopTimer.start();
                nonstopTimer.setOnLongClickListener((new View.OnLongClickListener() {
                    public boolean onLongClick(View v) {
                        try {
                            nonstopTimer.setBase(SystemClock.elapsedRealtime());
                        } catch (Exception ex) {
                        }
                        return true;
                    }
                }));
            }

            btnPlay.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (!songStarted) {
                        playSound(true);
                    } else {
                        playSound(false);
                        continousPlay = false;
                    }
                    songStarted = !songStarted;
                    refreshPlayButton();
                    refreshRepeatButton();
                }
            });

            btnRepeat.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    continousPlay = !continousPlay;
                    refreshRepeatButton();
                }
            });

            btnTimer5Min.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    fiveMinutEnabled = !fiveMinutEnabled;
                    refresh5MinButton();
                }
            });

            /*
            btnElevatorUp.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            elevatorCmd.setCmd(KHMDevice.CMD_ELEVATOR_UP, true);
                            break;
                        case MotionEvent.ACTION_UP:
                            elevatorCmd.setCmd(KHMDevice.CMD_ELEVATOR_STOP, true);
                            break;
                    }
                    return true;
                }
            });

            btnElevatorDown.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            elevatorCmd.setCmd(KHMDevice.CMD_ELEVATOR_DOWN, true);
                            break;
                        case MotionEvent.ACTION_UP:
                            elevatorCmd.setCmd(KHMDevice.CMD_ELEVATOR_STOP, true);
                            break;
                    }
                    return true;
                }
            });*/

            mediaPlayer.setOnCompletionListener(this);

            refreshStartButton();
            refresh5MinButton();
            refreshPlayButton();
            refreshRepeatButton();
            tryReinitializeUsb();
            onTimerButton();

        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage(), ex);
        }
    }

    private void onTimerButton() {
        try {
            if (!started) {
                khmTimer.setBase(SystemClock.elapsedRealtime());
                khmTimer.start();
                startTimer(true);
            } else {
                khmTimer.stop();
                startTimer(false);
            }
            started = !started;
            refreshStartButton();
        } catch (Exception ex) {
            Log.wtf(TAG, ex);
        }
    }

    private void selectSong(int songNumber) {
        try {
            SongItem song = songsAdapter.getSong(songNumber);
            int idx = songsAdapter.getSongIndex(songNumber);
            if (idx < 0 || null == song) {
                return;
            }
            songsAdapter.selectSong(song);
            songsList.invalidateViews();
            songsList.setSelection(idx);
            View sv = songsList.getSelectedView();
            if (null != sv) {
                Rect rect = new Rect(sv.getLeft(), sv.getTop(), sv.getRight(), sv.getBottom());
                songsList.requestChildRectangleOnScreen(sv, rect, false);
            }
        } catch (Exception ex) {
        }
    }

    public void startPlayProgressUpdater() {
        int duration = mediaPlayer.getDuration();
        int progress = (duration > 0) ? (mediaPlayer.getCurrentPosition() * 100) / duration : 0;
        seekBar.setProgress(progress);
        if (mediaPlayer.isPlaying()) {
            Runnable notification = new Runnable() {
                public void run() {
                    startPlayProgressUpdater();
                }
            };
            handler.postDelayed(notification,1000);
        } else {
            mediaPlayer.pause();
            seekBar.setProgress(0);
        }
        refreshPlayButton();
    }

    private void showError(String err) {
        AlertDialog deleteAlert = new AlertDialog.Builder(this).create();
        deleteAlert.setTitle("Internal error");
        deleteAlert.setMessage(err);
        deleteAlert.show();
    }

    private void playSound(boolean play) {
        try {
            if (null == songsAdapter) {
                return;
            }
            SongItem song = songsAdapter.getSelectedSong();
            if (play && null != song) {
                mediaPlayer.setVolume(0.7f,0.7f);
                mediaPlayer.reset();
                mediaPlayer.setDataSource(song.getPath());
                mediaPlayer.prepare();
                mediaPlayer.start();
                startPlayProgressUpdater();
            } else if (!play) {
                if (null != mediaPlayer) {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                    }
                }
            }
        } catch (IOException ex) {
            refreshPlayButton();
        }
    }

    private void startTimer(boolean start) {
        try {
            if (null != khmDev && khmDev.active()) {
                khmDev.timer(start ? KHMDevice.CMD_TIMER_RESTART : KHMDevice.CMD_TIMER_STOP);
                khmDev.timer(start ? KHMDevice.CMD_TIMER_RESTART : KHMDevice.CMD_TIMER_STOP);
                khmDev.timer(start ? KHMDevice.CMD_TIMER_RESTART : KHMDevice.CMD_TIMER_STOP);
            }
        } catch (Exception ex) {
        }
    }

    private void refreshPlayButton() {
        boolean play = null == mediaPlayer || !mediaPlayer.isPlaying();
        btnPlay.setImageResource(play ? R.drawable.button_play : R.drawable.button_stop);
    }
    private void refreshStartButton() {
        btnTimerStart.setText(started ? R.string.stop : R.string.start);
        btnTimerStart.setTextColor(started ? Color.RED : Color.GREEN);
    }
    private void refresh5MinButton() {
        boolean lighted = fiveMinutEnabled && (1 == (timerCounter % 2));
        btnTimer5Min.setTextColor(lighted ? Color.RED : Color.GRAY);
    }
    private void refreshRepeatButton() {
        boolean lighted = continousPlay && (1 == (timerCounter % 2));
        btnRepeat.setImageResource(lighted ? R.drawable.repeat : R.drawable.repeat_disabled);
    }

    private ElevatorCmd elevatorCmd = new ElevatorCmd();
    private void refreshUsb() {
        tryReinitializeUsb();
        try {
            if (btnElevatorDown.isPressed()) {
                elevatorCmd.setCmd(KHMDevice.CMD_ELEVATOR_DOWN, true);
            } else if (btnElevatorUp.isPressed()) {
                elevatorCmd.setCmd(KHMDevice.CMD_ELEVATOR_UP, true);
            } else {
                elevatorCmd.setCmd(KHMDevice.CMD_ELEVATOR_STOP, true);
            }
            if (null != khmDev && khmDev.active()) {
                if (elevatorCmd.resend(true))
                {
                    khmDev.elevator(elevatorCmd.getCmd());
                }
            }
        } catch (Exception ex) {
        }
    }

    private void tryReinitializeUsb() {
        try {
            if (!khmDev.active()) {
                boolean result = khmDev.initialize();
                int bkColor = result ? 0xff37783E : 0xff783a3b;

                View v = this.findViewById(R.id.elevatorFrame);
                if (null != v) {
                    v.setBackgroundColor(bkColor);
                }
                v = this.findViewById(R.id.timerFrame);
                if (null != v) {
                    v.setBackgroundColor(bkColor);
                }
                btnTimer5Min.setEnabled(false);
            }
        } catch (Exception ex) {
        }
    }

    private int timerCounter = 0;
    private void TimerMethod() {
        //This method is called directly by the timer
        //and runs in the same thread as the timer.
        timerCounter = timerCounter + 1;
        //We call the method that will work with the UI
        //through the runOnUiThread method.
        this.runOnUiThread(Timer_Tick);
    }
    private void TimerMethod2() {
        this.runOnUiThread(Timer_Tick2);
    }

    private Runnable Timer_Tick = new Runnable() {
        public void run() {
            refresh5MinButton();
            refreshPlayButton();
            refreshRepeatButton();
            refreshUsb();
        }
    };
    private Runnable Timer_Tick2 = new Runnable() {
        public void run() {
            refreshUsb();
        }
    };

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (continousPlay) {
            SongItem song = songBase.getNext(songsAdapter.getSelectedSong());
            selectSong(song.getNumber());
            playSound(true);
        }
    }
}
