package com.isonar.KHSystem;

import android.media.MediaMetadataRetriever;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created with IntelliJ IDEA.
 * User: gustaw
 * Date: 11.12.12
 * Time: 15:27
 * To change this template use File | Settings | File Templates.
 */
public class SongBase {
    private File path = null;

    public SongBase(String path) {
        if (null != path) {
            this.path = new File(path);
        } else {
            this.path =
                new File(Environment.getExternalStorageDirectory(),"KHM/snd");
        }
    }

    public ArrayList<SongItem> getSongs() {
        return songs;
    }

    private ArrayList<SongItem> songs = new ArrayList<SongItem>();
    public void build() {

        File dir = new File(getFullPath(""));
        String[] songFiles = dir.list(new SongFilter());

        for (String file: songFiles) {
            try {
                String fullName = getFullPath(file);
                int number = getSongNumber(file);
                String title = "";
                if (isFile(fullName)) {
                    try {
                        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                        mmr.setDataSource(fullName);
                        title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                        title = title.replace("-F", " -");
                    } catch (Exception ex) {
                    }
                    if (null == title || title.isEmpty()) {
                        title = String.format("%03d",number);
                    }
                    songs.add(new SongItem(number,title,fullName));
                } else {
                    songs.add(new SongItem(number,file,""));
                }
            } catch (Exception ex) {
                String tmp = ex.toString();
                // TODO: error handling
            }
        }
    }

    private int getSongNumber(String fileName) {
        try {
            if (fileName.endsWith(".mp3")) {
                fileName = fileName.substring(0,fileName.length() - 4);
            }
            fileName = fileName.replaceAll("\\D+","");
            return Integer.parseInt(fileName);
        } catch (Exception ex) {
            return -1;
        }
    }

    public SongItem getNext(SongItem item) {
        try {
            if (null == item) {
                return songs.get(0);
            }
            int idx = item.getNumber();
            if (idx >= songs.size()) {
                return songs.get(0);
            } else {
                // idx - song number is '1' not zero based!
                return songs.get(idx);
            }
        } catch (Exception ex) {
            return null;
        }
    }

    private String constructName(int idx) {
        return String.format("iasn_P_%03d.mp3",idx);
    }

    private String getFullPath(String name) {
        return new File(path, name).getPath();
    }

    private boolean isFile(String filePath) {
        try {
            File f = new File(filePath);
            if (!f.isFile()) {
                return false;
            }
            return true;
        } catch (Exception ex) {
            // TODO: error handling
            String tmp = ex.toString();
            return false;
        }
    }
}
