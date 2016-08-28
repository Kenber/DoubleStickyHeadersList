package com.kenber.doublestickyheaderslist.data;

/**
 * @author Kenber
 */
public class ListItem {
    public int level;
    public final String text;

    public int listPosition;

    public ListItem(int level, String text) {
        this.level = level;
        this.text = text;
    }

    @Override public String toString() {
        return text;
    }
}
