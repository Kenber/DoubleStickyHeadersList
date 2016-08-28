package com.kenber.view;

/**
 * @author Kenber
 */
public interface DoubleStickHeadersListAdapter {
    /**
     * level 0 (sticky):return 0
     * level 1 (sticky next to level 1 header it belongs to):return 1
     * level 2 (not stick):return 2
     */
    int getHeaderLevel(int position);
}
