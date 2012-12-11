package com.isonar.KHSystem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MyActivity extends Activity {
    private Button btnSong1;
    private Button btnSong2;
    private Button btnSong3;
    private Button btnTimerStart;
    private Button btnTimer5Min;
    private ImageButton btnPlay;
    private ImageButton btnElevatorUp;
    private ImageButton btnElevatorDown;
    private Chronometer khmTimer;
    private TextView timerView;
    ListView songsList;
    int selectedSongIndex = -1;
    SongItem selectedSong;
    SeekBar seekBar;

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

        Resources res = this.getResources();
        DisplayMetrics metrics = res.getDisplayMetrics();

        if (null != metrics) {
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            String tmp = String.format("density: %f\n", metrics.density);
            tmp = tmp + String.format("densityDpi: %d\n", metrics.densityDpi);
            tmp = tmp + String.format("widthPixels: %d\n", metrics.widthPixels);
            tmp = tmp + String.format("scaledDensity: %f\n", metrics.scaledDensity);
            showError("metrics: \n" + tmp);
            metrics.density = 1.50f;
            //metrics.densityDpi = 120;
            //metrics.scaledDensity = 0.75f;
            res.updateConfiguration(res.getConfiguration(),metrics);
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

    private void setUpView() {
        try {
            btnSong1 = (Button)this.findViewById(R.id.btnSong1);
            btnSong2 = (Button)this.findViewById(R.id.btnSong2);
            btnSong3 = (Button)this.findViewById(R.id.btnSong3);
            btnTimerStart = (Button)this.findViewById(R.id.btnTimerStart);
            btnTimer5Min = (Button)this.findViewById(R.id.btnTimer5Min);
            khmTimer = (Chronometer)this.findViewById(R.id.khmTimer);
            btnPlay = (ImageButton)this.findViewById(R.id.btnPlay);
            seekBar = (SeekBar)this.findViewById(R.id.seekBar);
            btnElevatorUp = (ImageButton)this.findViewById(R.id.btnElevatorUp);
            btnElevatorDown = (ImageButton)this.findViewById(R.id.btnElevatorDown);
            songsList = (ListView)findViewById(R.id.listView);

            khmTimer.setBase(SystemClock.elapsedRealtime());
            btnTimer5Min.setText(R.string.fiveMin);

            try {
                String tmp = Environment.getExternalStorageDirectory() + "/KHM/snd/";
                ArrayList<SongItem> FilesInFolder = GetFiles(tmp);
                songsList.setAdapter(new ArrayAdapter<SongItem>(this,
                        android.R.layout.simple_list_item_1, FilesInFolder));
            } catch (Exception ex) {
                showError("c1" + ex.toString());
            }

            try {
                songsList.setSelector(R.drawable.selector);
                songsList.setOnItemClickListener(new ListView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> a, View v, int position, long l) {
                        selectedSongIndex = position;
                        if (position >= 0) {
                            selectedSong = (SongItem)a.getItemAtPosition(position);
                        }
                    }
                });
            } catch (Exception ex) {
                showError("l1: " + ex.toString());
            }

            btnSong1.setLongClickable(true);
            btnSong1.setOnLongClickListener((new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    if (selectedSongIndex >= 0) {
                        btnSong1.setText(selectedSongIndex >= 0 ? String.format("%d",selectedSongIndex) : "P1");
                    }
                    return true;
                }
            }));
            btnSong1.setOnClickListener((new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        int idx = Integer.parseInt(btnSong1.getText().toString());
                        songsList.setSelection(idx);
                        songsList.setItemChecked(idx,true);
                        selectedSong = (SongItem)songsList.getItemAtPosition(idx);
                    } catch (Exception ex) {
                    }
                }
            }));
            btnSong2.setLongClickable(true);
            btnSong2.setOnLongClickListener((new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    if (selectedSongIndex >= 0) {
                        btnSong2.setText(selectedSongIndex >= 0 ? String.format("%d",selectedSongIndex) : "P1");
                    }
                    return true;
                }
            }));
            btnSong2.setOnClickListener((new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        int idx = Integer.parseInt(btnSong2.getText().toString());
                        songsList.setSelection(idx);
                        songsList.setItemChecked(idx,true);
                        selectedSong = (SongItem)songsList.getItemAtPosition(idx);
                    } catch (Exception ex) {
                    }
                }
            }));
            btnSong3.setLongClickable(true);
            btnSong3.setOnLongClickListener((new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    if (selectedSongIndex >= 0) {
                        btnSong3.setText(selectedSongIndex >= 0 ? String.format("%d",selectedSongIndex) : "P1");
                    }
                    return true;
                }
            }));
            btnSong3.setOnClickListener((new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        int idx = Integer.parseInt(btnSong3.getText().toString());
                        songsList.setSelection(idx);

                        songsList.setItemChecked(idx,true);
                        selectedSong = (SongItem)songsList.getItemAtPosition(idx);
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

            btnPlay.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (!songStarted) {
                        playSound(true);
                    } else {
                        playSound(false);
                    }
                    songStarted = !songStarted;
                    refreshPlayButton();
                }
            });

            btnTimer5Min.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    fiveMinutEnabled = !fiveMinutEnabled;
                    refresh5MinButton();
                }
            });
            refreshStartButton();
            refresh5MinButton();
            refreshPlayButton();
        } catch (Exception ex) {
            showError("c2" + ex.toString());
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

    private MediaPlayer mediaPlayer = new MediaPlayer();
    private void playSound(boolean play) {
        try {
            if (play && null != selectedSong) {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(selectedSong.getPath());
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
        }
    }

    public ArrayList<SongItem> GetFiles(String DirectoryPath) {
        ArrayList<SongItem> ret = new ArrayList<SongItem>();
        try {
            File f = new File(DirectoryPath);
            if (null != f) {
                File[] files = f.listFiles();
                if (null == files || 0 == files.length)
                    return ret;
                else {
                    for (int i=0; i<files.length; i++)
                        ret.add(
                                new SongItem(
                                        files[i].getName(),
                                        DirectoryPath + files[i].getName()));
                }
            }
        } catch (Exception ex) {
            showError(ex.toString());
        }
        return ret;
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
        }
    };
}
