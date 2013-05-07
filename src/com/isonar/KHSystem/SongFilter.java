package com.isonar.KHSystem;

import android.*;
import android.R;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created with IntelliJ IDEA.
 * User: gustaw
 * Date: 07.05.13
 * Time: 12:18
 * To change this template use File | Settings | File Templates.
 */
public class SongFilter implements FilenameFilter {

    private final String prefix;
    private final String postfix;

    public SongFilter() {
        prefix = "iasn_";
        postfix = ".mp3";
    }
    public SongFilter(String pre, String post) {
        prefix = pre;
        postfix = post;
    }

    @Override
    public boolean accept(File dir, String filename) {
        boolean result = filename.startsWith(prefix);
        if (!result) return result;
        result = filename.endsWith(postfix);
        return result;
    }
}
