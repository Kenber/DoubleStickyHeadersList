package com.kenber.doublestickyheaderslist.ui;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.kenber.view.DoubleStickHeaderItem;
import com.kenber.view.DoubleStickHeaderLevelEnum;
import com.kenber.view.DoubleStickHeadersListAdapter;

import java.util.Locale;

/**
 * @author Kenber
 */
public class ListAdapter extends ArrayAdapter<DoubleStickHeaderItem<String>> implements DoubleStickHeadersListAdapter {
    private static final int LEVEL0_HEADERS_NUMBER = 10;
    private static final int LEVEL1_HEADERS_NUMBER = 4;
    private static final int LEVEL2_HEADERS_NUMBER = 5;

    private static final int BG_COLOR_LEVEL_0 = android.R.color.holo_purple;
    private static final int BG_COLOR_LEVEL_1 = android.R.color.holo_green_light;
    private static final int BG_COLOR_LEVEL_2 = android.R.color.white;
    public ListAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
        genListData();
    }

    public void genListData() {

        for (char i = 0; i < LEVEL0_HEADERS_NUMBER; i++) {
            DoubleStickHeaderItem<String> level0Header = new DoubleStickHeaderItem<>(DoubleStickHeaderLevelEnum.HEADER_LEVEL_0, String.valueOf((char)('A' + i)));
            add(level0Header);

            for (char j = 0; j < LEVEL1_HEADERS_NUMBER; j++) {
                DoubleStickHeaderItem<String> level1Header = new DoubleStickHeaderItem<>(DoubleStickHeaderLevelEnum.HEADER_LEVEL_1,
                        level0Header.getItem().toUpperCase(Locale.ENGLISH) + " - " + String.valueOf((char)('a' + j)));
                add(level1Header);

                for (int k = 0; k < LEVEL2_HEADERS_NUMBER; k++) {
                    DoubleStickHeaderItem<String> level2Header = new DoubleStickHeaderItem<>(DoubleStickHeaderLevelEnum.ROWS, level1Header.getItem() + " - " + k);
                    add(level2Header);
                }
            }

        }
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        TextView view = (TextView) super.getView(position, convertView, parent);
        view.setTextColor(Color.BLACK);
        view.setTag("" + position);
        DoubleStickHeaderLevelEnum level = getHeaderLevel(position);
        if (level == DoubleStickHeaderLevelEnum.HEADER_LEVEL_0) {
            view.setBackgroundColor(parent.getResources().getColor(BG_COLOR_LEVEL_0));
        } else if (level == DoubleStickHeaderLevelEnum.HEADER_LEVEL_1) {
            view.setBackgroundColor(parent.getResources().getColor(BG_COLOR_LEVEL_1));
        } else {
            view.setBackgroundColor(parent.getResources().getColor(BG_COLOR_LEVEL_2));
        }
        return view;
    }

    @Override
    public DoubleStickHeaderLevelEnum getHeaderLevel(int position) {
        return getItem(position).getLevel();
    }
}
