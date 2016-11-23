package com.kenber.view;

/**
 * This is the item you have to provide to the ArrayAdapter
 * @param <T> This is the class of the item you want to display Initially
 */
public class DoubleStickHeaderItem<T> {
    private DoubleStickHeaderLevelEnum level;
    private final T item;


    public DoubleStickHeaderItem(DoubleStickHeaderLevelEnum level, T item) {
        this.level = level;
        this.item = item;
    }

    public DoubleStickHeaderLevelEnum getLevel() {
        return level;
    }

    public T getItem() {
        return item;
    }

    @Override public String toString() {
        return item.toString();
    }
}
