package com.isonar.KHSystem;

/**
 * Created with IntelliJ IDEA.
 * User: gustaw
 * Date: 02.12.12
 * Time: 14:13
 * To change this template use File | Settings | File Templates.
 */
public class SongItem {
    private int number;
    private String name;
    private String path;

    public SongItem(int number, String name, String path) {
        this.number = number;
        this.name = name;
        this.path = path;
    }

    public int getNumber() {
        return this.number;
    }

    public String getPath() {
        return this.path;
    }

    @Override public String toString() {
        return this.name;
    }
}
