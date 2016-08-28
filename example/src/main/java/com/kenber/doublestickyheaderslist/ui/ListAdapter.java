package com.kenber.doublestickyheaderslist.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.kenber.doublestickyheaderslist.data.ListItem;
import com.kenber.view.DoubleStickHeadersListAdapter;
import com.kenber.view.DoubleStickyHeaderListView;

import java.util.Locale;

/**
 * @author Kenber
 */
public class ListAdapter extends ArrayAdapter<ListItem> implements DoubleStickHeadersListAdapter {
    private static final int LEVEL0_HEADERS_NUMBER = 10;
    private static final int LEVEL1_HEADERS_NUMBER = 4;
    private static final int LEVEL2_HEADERS_NUMBER = 5;

    private static final int[] BG_COLORS = {
            android.R.color.holo_blue_dark, android.R.color.holo_red_dark,
            android.R.color.holo_orange_dark, android.R.color.holo_green_dark};

    public ListAdapter(Context context, int resource) {
        super(context, resource);
        genListData();
    }

    public void genListData() {

        int listPosition = 0;
        for (char i = 0; i < LEVEL0_HEADERS_NUMBER; i++) {
            ListItem level0Header = new ListItem(DoubleStickyHeaderListView.HEADER_LEVEL_0, String.valueOf((char)('A' + i)));
            level0Header.listPosition = listPosition++;
            add(level0Header);

            for (char j = 0; j < LEVEL1_HEADERS_NUMBER; j++) {
                ListItem level1Header = new ListItem(DoubleStickyHeaderListView.HEADER_LEVEL_1,
                        level0Header.text.toUpperCase(Locale.ENGLISH) + " - " + String.valueOf((char)('a' + j)));
                add(level1Header);

                for (int k = 0; k < LEVEL2_HEADERS_NUMBER; k++) {
                    ListItem level2Header = new ListItem(DoubleStickyHeaderListView.HEADER_LEVEL_2, level1Header.text + " - " + k);
                    level2Header.listPosition = listPosition++;
                    add(level2Header);
                }
            }

        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView view = (TextView) super.getView(position, convertView, parent);
        view.setTextColor(Color.BLACK);
        view.setTag("" + position);
        ListItem item = getItem(position);
        if (item.level == DoubleStickyHeaderListView.HEADER_LEVEL_0 ||
            item.level == DoubleStickyHeaderListView.HEADER_LEVEL_1) {
            view.setBackgroundColor(parent.getResources().getColor(BG_COLORS[item.listPosition % BG_COLORS.length]));
        }
        return view;
    }

    @Override
    public int getHeaderLevel(int position) {
        return getItem(position).level;
    }
}
