/*
 * Copyright (C) 2016 Kenber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file kt in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kenber.view;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.kenber.library.BuildConfig;

public class DoubleStickyHeaderListView extends ListView {
    public static class StickyHeader {
        public View view;
        public int position;
        public long id;
    }

    private final Rect mTouchRect = new Rect();
    private final PointF mTouchPoint = new PointF();
    private View mTouchTarget;
    private int mTouchTargetLevel;

    private int mHeadersDistanceY0;
    private int mHeadersDistanceY1;
    private boolean isTargetPressed = false;
    private View lastTargetView;

    OnScrollListener mDelegateOnScrollListener;

    StickyHeader mRecycleHeader0;
    StickyHeader mRecycleHeader1;

    StickyHeader mStickyHeader0;
    StickyHeader mStickyHeader1;

    int mTranslateY0;
    int mTranslateY1;

    public static final int HEADER_LEVEL_0 = 0;
    public static final int HEADER_LEVEL_1 = 1;
    public static final int HEADER_LEVEL_2 = 2;

    private final OnScrollListener mOnScrollListener = new OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (mDelegateOnScrollListener != null) { 
                mDelegateOnScrollListener.onScrollStateChanged(view, scrollState);
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            if (mDelegateOnScrollListener != null) { 
                mDelegateOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }

            ListAdapter adapter = getAdapter();
            if (adapter == null || visibleItemCount == 0) return; 

            final boolean isFirstVisibleItemStickyHeader = isLevel1StickyHeader(adapter, firstVisibleItem);

            if (isFirstVisibleItemStickyHeader) {
                View stickyHeaderView = getChildAt(0);
                if (stickyHeaderView.getTop() == getPaddingTop()) {
                    destroyPinnedShadow(HEADER_LEVEL_0);
                    destroyPinnedShadow(HEADER_LEVEL_1);
                } else {
                    int stickyHeaderPosition1;
                 if (((DoubleStickHeadersListAdapter)adapter).getHeaderLevel(firstVisibleItem + 1) == HEADER_LEVEL_0) {
                        stickyHeaderPosition1 = -1;
                    } else {
                        stickyHeaderPosition1 = isLevel1HeaderSticky(firstVisibleItem + 1) ? (firstVisibleItem + 1) : -1;
                    }
                    ensureShadowForPosition(firstVisibleItem, stickyHeaderPosition1, firstVisibleItem, visibleItemCount);
                }
            } else {
                int stickyHeaderPosition0 = findCurrentStickyHeaderPosition(firstVisibleItem, HEADER_LEVEL_0);
                int stickyHeaderPosition1 = findCurrentLevel2StickyHeaderPosition(firstVisibleItem);
                if (stickyHeaderPosition0 > -1) { 
                    ensureShadowForPosition(stickyHeaderPosition0, stickyHeaderPosition1, firstVisibleItem, visibleItemCount);
                } else {
                    destroyPinnedShadow(HEADER_LEVEL_0);
                    destroyPinnedShadow(HEADER_LEVEL_1);
                }
            }
        }
    };

    private final DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            recreatePinnedShadow();
        }

        @Override
        public void onInvalidated() {
            recreatePinnedShadow();
        }
    };

    public DoubleStickyHeaderListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public DoubleStickyHeaderListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    private void initView() {
        setOnScrollListener(mOnScrollListener);
    }

    void createPinnedShadow(int stickyHeaderPosition, int level) {

        StickyHeader pinnedShadow = level == HEADER_LEVEL_0 ? mRecycleHeader0 : mRecycleHeader1;
        if (level == HEADER_LEVEL_0) {
            mRecycleHeader0 = null;
        } else {
            mRecycleHeader1 = null;
        }

        if (pinnedShadow == null) pinnedShadow = new StickyHeader();

        View pinnedView = getAdapter().getView(stickyHeaderPosition, pinnedShadow.view, DoubleStickyHeaderListView.this);

        ViewGroup.LayoutParams layoutParams = pinnedView.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = generateDefaultLayoutParams();
            pinnedView.setLayoutParams(layoutParams);
        }

        int heightMode = MeasureSpec.getMode(layoutParams.height);
        int heightSize = MeasureSpec.getSize(layoutParams.height);

        if (heightMode == MeasureSpec.UNSPECIFIED) heightMode = MeasureSpec.EXACTLY;

        int maxHeight = getHeight() - getListPaddingTop() - getListPaddingBottom();
        if (heightSize > maxHeight) heightSize = maxHeight;

        int ws = MeasureSpec.makeMeasureSpec(getWidth() - getListPaddingLeft() - getListPaddingRight(), MeasureSpec.EXACTLY);
        int hs = MeasureSpec.makeMeasureSpec(heightSize, heightMode);
        pinnedView.measure(ws, hs);
        pinnedView.layout(0, 0, pinnedView.getMeasuredWidth(), pinnedView.getMeasuredHeight());
        if (level == HEADER_LEVEL_0) {
            mTranslateY0 = 0;
        } else if (level == HEADER_LEVEL_1) {
            mTranslateY1 = 0;
        }

        pinnedShadow.view = pinnedView;
        pinnedShadow.position = stickyHeaderPosition;
        pinnedShadow.id = getAdapter().getItemId(stickyHeaderPosition);

        if (level == HEADER_LEVEL_0) {
            mStickyHeader0 = pinnedShadow;
        } else {
            mStickyHeader1 = pinnedShadow;
        }
    }

    void destroyPinnedShadow(int level) {
        if (level == HEADER_LEVEL_0) {
            if (mStickyHeader0 != null) {
                mRecycleHeader0 = mStickyHeader0;
                mStickyHeader0 = null;
            }
        } else if (level == HEADER_LEVEL_1) {
            if (mStickyHeader1 != null) {
                mRecycleHeader1 = mStickyHeader1;
                mStickyHeader1 = null;
            }
        }
    }

    void ensureShadowForPosition(int stickyHeader0Position, int stickyHeader1Position, int firstVisibleItem, int visibleItemCount) {
        if (visibleItemCount < 2) { 
            destroyPinnedShadow(HEADER_LEVEL_0);
            destroyPinnedShadow(HEADER_LEVEL_1);
            return;
        }

        if (mStickyHeader0 != null && mStickyHeader0.position != stickyHeader0Position) {
            destroyPinnedShadow(HEADER_LEVEL_0);
        }
        if (stickyHeader1Position == -1 || (mStickyHeader1 != null && mStickyHeader1.position != stickyHeader1Position)) {
            destroyPinnedShadow(HEADER_LEVEL_1);
        }

        if (mStickyHeader0 == null) {
            createPinnedShadow(stickyHeader0Position, HEADER_LEVEL_0);
        }
        if (stickyHeader1Position > -1 && mStickyHeader1 == null) {
            createPinnedShadow(stickyHeader1Position, HEADER_LEVEL_1);
        }

        int nextPosition0 = stickyHeader0Position + 1;
        int nextStickyHeaderPosition0 = -1;
        if (nextPosition0 < getCount()) {
            nextStickyHeaderPosition0 = findFirstVisibleStickyHeaderPosition(nextPosition0,
                    visibleItemCount - (nextPosition0 - firstVisibleItem), HEADER_LEVEL_0);
            if (nextStickyHeaderPosition0 > -1) {
                View nextStickyHeaderView0 = getChildAt(nextStickyHeaderPosition0 - firstVisibleItem);
                final int bottom = mStickyHeader0.view.getBottom() + getPaddingTop();
                mHeadersDistanceY0 = nextStickyHeaderView0.getTop() - bottom;
                if (mHeadersDistanceY0 < 0) {
                    mTranslateY0 = mHeadersDistanceY0;
                } else {
                    mTranslateY0 = 0;
                }
            } else {
                mTranslateY0 = 0;
                mHeadersDistanceY0 = Integer.MAX_VALUE;
            }
        }

        if (mStickyHeader1 == null) {
            return;
        }
        int nextPosition1 = stickyHeader1Position + 1;
        if (nextPosition1 < getCount()) {
            int firstVisibleStickyHeaderPosition = findFirstVisibleStickyHeaderPosition(nextPosition1, visibleItemCount - (nextPosition1 - firstVisibleItem), HEADER_LEVEL_1);
            int nextStickyHeaderPosition1;
            if (nextStickyHeaderPosition0 < 0) {
                nextStickyHeaderPosition1 = firstVisibleStickyHeaderPosition;
            } else {
                nextStickyHeaderPosition1 = Math.min(nextStickyHeaderPosition0, firstVisibleStickyHeaderPosition);
            }
            if (nextStickyHeaderPosition1 > -1) {
                View nextStickyHeaderView1 = getChildAt(nextStickyHeaderPosition1 - firstVisibleItem);
                final int bottom = mStickyHeader0.view.getHeight() + mStickyHeader1.view.getBottom() + getPaddingTop();
                mHeadersDistanceY1 = nextStickyHeaderView1.getTop() - bottom;
                if (mHeadersDistanceY1 < 0) {
                    mTranslateY1 = mHeadersDistanceY1;
                } else {
                    mTranslateY1 = 0;
                }
            } else {
                mTranslateY1 = 0;
                mHeadersDistanceY1 = Integer.MAX_VALUE;
            }
        }
    }

    boolean isLevel1HeaderSticky(int position) {
        if (position + 1 >= getAdapter().getCount()) {
            return false;
        }
        return ((DoubleStickHeadersListAdapter)getAdapter()).getHeaderLevel(position + 1) == HEADER_LEVEL_2;
    }

    int findFirstVisibleStickyHeaderPosition(int firstVisibleItem, int visibleItemCount, int level) {
        ListAdapter adapter = getAdapter();

        int adapterDataCount = adapter.getCount();
        if (getLastVisiblePosition() >= adapterDataCount)
            return -1; 

        if (firstVisibleItem + visibleItemCount >= adapterDataCount) { 
            visibleItemCount = adapterDataCount - firstVisibleItem;
        }

        for (int childIndex = 0; childIndex < visibleItemCount; childIndex++) {
            int position = firstVisibleItem + childIndex;
            int headerLevel = ((DoubleStickHeadersListAdapter)getAdapter()).getHeaderLevel(position);
            if (level == HEADER_LEVEL_0 && headerLevel == level)
                return position;
            if (level == HEADER_LEVEL_1 && headerLevel == level) return position;
        }
        return -1;
    }

    int findCurrentLevel2StickyHeaderPosition(int firstVisibleItem) {
        int headerLevel = ((DoubleStickHeadersListAdapter)getAdapter()).getHeaderLevel(firstVisibleItem + 1);
        int stickyHeaderPosition1;
        if (headerLevel == HEADER_LEVEL_2) {
            stickyHeaderPosition1 = findCurrentStickyHeaderPosition(firstVisibleItem + 1, 1);
        } else if (headerLevel == HEADER_LEVEL_1) {
            if (isLevel1HeaderSticky(firstVisibleItem + 1)) {
                stickyHeaderPosition1 = firstVisibleItem + 1;
            } else {
                stickyHeaderPosition1 = -1;
            }
        } else {
            stickyHeaderPosition1 = -1;
        }
        return stickyHeaderPosition1;
    }

    int findCurrentStickyHeaderPosition(int fromPosition, int level) {
        ListAdapter adapter = getAdapter();

        if (fromPosition >= adapter.getCount()) return -1; 

        for (int position = fromPosition; position >= 0; position--) {
            int headerLevel = ((DoubleStickHeadersListAdapter)getAdapter()).getHeaderLevel(position);
            if (level == HEADER_LEVEL_0 && headerLevel == level)
                return position;
            if (level == HEADER_LEVEL_1 && headerLevel == level) return position;
        }
        return -1;
    }

    void recreatePinnedShadow() {
        destroyPinnedShadow(HEADER_LEVEL_0);
        destroyPinnedShadow(HEADER_LEVEL_1);
        ListAdapter adapter = getAdapter();
        if (adapter != null && adapter.getCount() > 0) {
            int firstVisiblePosition = getFirstVisiblePosition();
            if (firstVisiblePosition + 1 >= adapter.getCount()) {
                return;
            }
            int stickyHeader0Position = findCurrentStickyHeaderPosition(firstVisiblePosition, HEADER_LEVEL_0);
            if (stickyHeader0Position == -1) return;
            ensureShadowForPosition(stickyHeader0Position, findCurrentLevel2StickyHeaderPosition(firstVisiblePosition),
                    firstVisiblePosition, getLastVisiblePosition() - firstVisiblePosition);
        }
    }

    @Override
    public void setOnScrollListener(OnScrollListener listener) {
        if (listener == mOnScrollListener) {
            super.setOnScrollListener(listener);
        } else {
            mDelegateOnScrollListener = listener;
        }
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        post(new Runnable() {
            @Override
            public void run() { 
                recreatePinnedShadow();
            }
        });
    }

    @Override
    public void setAdapter(ListAdapter adapter) {

        if (BuildConfig.DEBUG && adapter != null) {
            if (!(adapter instanceof DoubleStickHeadersListAdapter))
                throw new IllegalArgumentException("Your adapter doesn't implement DoubleStickHeadersListAdapter.");
        }

        ListAdapter oldAdapter = getAdapter();
        if (oldAdapter != null) oldAdapter.unregisterDataSetObserver(mDataSetObserver);
        if (adapter != null) adapter.registerDataSetObserver(mDataSetObserver);

        if (oldAdapter != adapter) {
            destroyPinnedShadow(HEADER_LEVEL_0);
            destroyPinnedShadow(HEADER_LEVEL_1);
        }

        super.setAdapter(adapter);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (mStickyHeader0 != null || mStickyHeader1 != null) {
            int parentWidth = r - l - getPaddingLeft() - getPaddingRight();
            int shadowWidth = mStickyHeader0.view.getWidth();
            if (parentWidth != shadowWidth) {
                recreatePinnedShadow();
            }
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (mStickyHeader0 != null) {
            int pLeft = getListPaddingLeft();
            int pTop = getListPaddingTop();
            View level0StickyHeaderView = mStickyHeader0.view;

            canvas.save();
            canvas.clipRect(pLeft, pTop, pLeft + level0StickyHeaderView.getWidth(), pTop + level0StickyHeaderView.getHeight());
            canvas.translate(pLeft, pTop + mTranslateY0);
            mStickyHeader0.view.draw(canvas);
            canvas.restore();

            if (mStickyHeader1 != null) {
                canvas.save();
                View level1StickyHeaderView = mStickyHeader1.view;
                canvas.clipRect(pLeft, pTop + level0StickyHeaderView.getHeight(), pLeft + level1StickyHeaderView.getWidth(), pTop + level0StickyHeaderView.getHeight() + level1StickyHeaderView.getHeight());
                canvas.translate(pLeft, pTop + level0StickyHeaderView.getHeight() + mTranslateY1);
                mStickyHeader1.view.draw(canvas);
                canvas.restore();
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        final float x = ev.getX();
        final float y = ev.getY();
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;
        boolean mStickyCatchTouch = false;

        if (mStickyHeader0 != null && isStickyHeaderTouched(mStickyHeader0.view, x, y, HEADER_LEVEL_0)) {
            mTouchTarget = mStickyHeader0.view;
            mStickyCatchTouch = true;
            mTouchTargetLevel = HEADER_LEVEL_0;
            mTouchPoint.x = x;
            mTouchPoint.y = y;
        }
        if (mStickyHeader1 != null && isStickyHeaderTouched(mStickyHeader1.view, x, y - mStickyHeader0.view.getHeight(), HEADER_LEVEL_1)) {
            mTouchTarget = mStickyHeader1.view;
            mStickyCatchTouch = true;
            mTouchTargetLevel = HEADER_LEVEL_1;
            mTouchPoint.x = x;
            mTouchPoint.y = y;
            ev.setLocation(x, y - mStickyHeader0.view.getHeight());
        }

        if (isTargetPressed && lastTargetView != mTouchTarget) {
            isTargetPressed = false;
            lastTargetView.setPressed(false);
            invalidate();
        }

        if (mStickyCatchTouch) {
            boolean handled = mTouchTarget.dispatchTouchEvent(ev);
            lastTargetView = mTouchTarget;

            if (!handled) {
                mTouchTarget.setPressed(true);
                if (!isTargetPressed) {
                    invalidate();
                }
                isTargetPressed = true;
            }

            if (action == MotionEvent.ACTION_UP) {
                if (handled) {
                    requestLayout();
                } else {
                    if (isTargetPressed) {
                        isTargetPressed = false;
                        mTouchTarget.setPressed(false);
                        invalidate();
                    }
                    int postion = mTouchTargetLevel == HEADER_LEVEL_0 ? mStickyHeader0.position : mStickyHeader1.position;
                    performItemClick(mTouchTarget, postion, postion);
                }
            }

            return true;
        }

        return super.dispatchTouchEvent(ev);
    }

    private boolean isStickyHeaderTouched(View view, float x, float y, int level) {
        view.getHitRect(mTouchRect);

        int translateY = level == HEADER_LEVEL_0 ? mTranslateY0 : mTranslateY1;
        mTouchRect.top += translateY;
        mTouchRect.bottom += translateY + getPaddingTop();
        mTouchRect.left += getPaddingLeft();
        mTouchRect.right -= getPaddingRight();
        return mTouchRect.contains((int) x, (int) y);
    }

    public static boolean isLevel1StickyHeader(ListAdapter adapter, int position) {
        if (adapter instanceof HeaderViewListAdapter) {
            adapter = ((HeaderViewListAdapter) adapter).getWrappedAdapter();
        }
        return ((DoubleStickHeadersListAdapter) adapter).getHeaderLevel(position) == HEADER_LEVEL_0;
    }

}
