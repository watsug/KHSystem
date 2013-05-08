package com.isonar.KHSystem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class MyActivity extends Activity implements MediaPlayer.OnCompletionListener {
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
    private TextView timerView;
    ListView songsList;
    SongBase songBase = new SongBase(null);
    private SongsListAdapter songsAdapter = null;
    SeekBar seekBar;

    private MediaPlayer mediaPlayer = new MediaPlayer();
    private boolean continousPlay = false;
    private boolean started = false;
    private boolean fiveMinutEnabled = false;
    private Timer myTimer;
    private final Handler handler = new Handler();

    private boolean songStarted = false;

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
                String tmp = String.format("density: %f\n", metrics.density);
                tmp = tmp + String.format("densityDpi: %d\n", metrics.densityDpi);
                tmp = tmp + String.format("widthPixels: %d\n", metrics.widthPixels);
                tmp = tmp + String.format("scaledDensity: %f\n", metrics.scaledDensity);
                //showError("metrics: \n" + tmp);
                metrics.density = (float)densityDpi/160.0f;
                metrics.densityDpi = densityDpi;
                metrics.scaledDensity = (float)densityDpi/160.0f;
                res.updateConfiguration(res.getConfiguration(),metrics);
            }
        }

        setContentView(R.layout.main);
        setUpView();

        myTimer = new Timer();
        myTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                TimerMethod();
            }
        }, 0, 500);

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
                showError("c1" + ex.toString());
            }

            try {
                songsList.setOnItemClickListener(new ListView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> a, View v, int position, long l) {
                        SongItem song = null;
                        if (position >= 0) {
                            song = (SongItem)a.getItemAtPosition(position);
                        }
                        songsAdapter.selectSong(song);
                        v.refreshDrawableState();
                    }
                });
            } catch (Exception ex) {
                showError("l1: " + ex.toString());
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
                    if (!started) {
                        khmTimer.setBase(SystemClock.elapsedRealtime());
                        khmTimer.start();
                    } else {
                        khmTimer.stop();
                    }
                    started = !started;
                    refreshStartButton();
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

            mediaPlayer.setOnCompletionListener(this);

            refreshStartButton();
            refresh5MinButton();
            refreshPlayButton();
            refreshRepeatButton();
        } catch (Exception ex) {
            showError("c2" + ex.toString());
        }
    }

    private void selectSong(int idx) {
        try {
            songsList.setSelection(idx-1);
            SongItem song = (SongItem)songsList.getItemAtPosition(idx-1);
            songsAdapter.selectSong(song);
            songsList.invalidateViews();
            songsList.setSelection(idx-1);
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

    private int timerCounter = 0;
    private void TimerMethod() {
        //This method is called directly by the timer
        //and runs in the same thread as the timer.
        timerCounter = timerCounter + 1;
        //We call the method that will work with the UI
        //through the runOnUiThread method.
        this.runOnUiThread(Timer_Tick);
    }

    private Runnable Timer_Tick = new Runnable() {
        public void run() {
            refresh5MinButton();
            refreshPlayButton();
            refreshRepeatButton();
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
