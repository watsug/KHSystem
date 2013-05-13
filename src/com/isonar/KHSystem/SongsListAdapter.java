package com.isonar.KHSystem;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gustaw
 * Date: 14.12.12
 * Time: 21:00
 * To change this template use File | Settings | File Templates.
 */
public class SongsListAdapter extends ArrayAdapter {

    private Context mContext;
    private int id;
    private List<SongItem> items;
    private int selectedSong = -1;

    public SongsListAdapter(Context context, int textViewResourceId, List<SongItem> list )
    {
        super(context, textViewResourceId, list);
        mContext = context;
        id = textViewResourceId;
        items = list ;
    }

    public void selectSong(SongItem item) {
        selectedSong = -1;
        if (null == item) {
            return;
        }
        for (int i=0; i < items.size(); i++) {
            if (items.get(i).getNumber() == item.getNumber()) {
                selectedSong = i;
                return;
            }
        }
    }

    public int getSongIndex(int songNumber) {
        for (int i=0; i < items.size(); i++) {
            if (items.get(i).getNumber() == songNumber) {
                return i;
            }
        }
        return -1;
    }

    public SongItem getSong(int songNumber) {
        for (int i=0; i < items.size(); i++) {
            if (items.get(i).getNumber() == songNumber) {
                return items.get(i);
            }
        }
        return null;
    }

    public SongItem getSelectedSong() {
        if (selectedSong < 0) {
            return null;
        } else {
            return items.get(selectedSong);
        }
    }

    @Override
    public View getView(int position, View v, ViewGroup parent)
    {
        View mView = v ;
        if(mView == null){
            LayoutInflater vi = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mView = vi.inflate(id, null);
        }

        TextView text = (TextView) mView.findViewById(R.id.songItemView);
        if(items.get(position) != null )
        {
            SongItem item = items.get(position);
            text.setTextColor(item.getPath().isEmpty() ? Color.RED : Color.WHITE);
            text.setText(item.toString());
            int bkColor = (position == selectedSong) ? Color.GRAY : Color.TRANSPARENT;
            text.setBackgroundColor(bkColor);
        }
        return mView;
    }

}