package com.kenber.view;

/**
 * @author Kenber
 */
public interface DoubleStickHeadersListAdapter {
    /**
     * level 0 (sticky):return DoubleStickHeaderLevelEnum.HEADER_LEVEL_0
     * level 1 (sticky next to level 1 header it belongs to):return DoubleStickHeaderLevelEnum.HEADER_LEVEL_1
     * level 2 (not stick):return DoubleStickHeaderLevelEnum.ROWS
     */

    DoubleStickHeaderLevelEnum getHeaderLevel(int position);
}
