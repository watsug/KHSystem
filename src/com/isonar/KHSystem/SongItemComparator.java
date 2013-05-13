package com.isonar.KHSystem;

import java.util.Comparator;

/**
 * Created with IntelliJ IDEA.
 * User: gustaw
 * Date: 13.05.13
 * Time: 11:15
 * To change this template use File | Settings | File Templates.
 */
public class SongItemComparator implements Comparator<SongItem> {
    @Override
    public int compare(SongItem s1, SongItem s2) {
        if (s1.getNumber() < s2.getNumber()) return -1;
        if (s1.getNumber() > s2.getNumber()) return 1;
        return 0;
    }
}
