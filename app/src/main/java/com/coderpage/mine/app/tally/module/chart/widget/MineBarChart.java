package com.coderpage.mine.app.tally.module.chart.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;

/**
 * @author lc. 2018-09-29 16:22
 * @since 0.6.0
 */

public class MineBarChart extends BarChart {

    private boolean mDrawMarkOnTop = false;

    public MineBarChart(Context context) {
        super(context);
    }

    public MineBarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MineBarChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init() {
        super.init();
        MineLineChartTouchListener touchListener =
                new MineLineChartTouchListener(this, mViewPortHandler.getMatrixTouch(), 3f);
        // 日柱图：同柱再点不取消；当月下点空白不重复回调
        touchListener.setStableSelection(true);
        // 不响应顶部 Marker 点击跳转列表，点上方按空白处理
        touchListener.setMarkerClickEnabled(false);
        mChartTouchListener = touchListener;
        mRenderer = new MineBarChartRenderer(this, mAnimator, mViewPortHandler);
        setHighlighter(new StrictBarHighlighter(this));
    }

    /**
     * 程序化高亮并同步触摸层的 lastHighlighted，避免再点同一柱被当成「切换」
     */
    public void highlightIndexSilently(int index) {
        if (index < 0 || getData() == null || getData().getDataSetCount() <= 0) {
            highlightValues(null);
            if (mChartTouchListener != null) {
                mChartTouchListener.setLastHighlighted(null);
            }
            return;
        }
        int entryCount = getData().getDataSetByIndex(0).getEntryCount();
        if (index >= entryCount) {
            highlightValues(null);
            if (mChartTouchListener != null) {
                mChartTouchListener.setLastHighlighted(null);
            }
            return;
        }
        highlightValue(index, 0, false);
        Highlight[] highs = getHighlighted();
        if (mChartTouchListener != null) {
            mChartTouchListener.setLastHighlighted(
                    highs != null && highs.length > 0 ? highs[0] : null);
        }
    }

    public void clearHighlightSilently() {
        highlightValues(null);
        if (mChartTouchListener != null) {
            mChartTouchListener.setLastHighlighted(null);
        }
    }

    public void setDrawMarkOnTop(boolean drawMarkOnTop) {
        this.mDrawMarkOnTop = drawMarkOnTop;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 参考 http://www.jianshu.com/p/fe3d109eb27e
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            requestDisallowInterceptTouchEvent(true);
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void drawMarkers(Canvas canvas) {
        if (mDrawMarkOnTop) {
            drawTopMarkers(canvas);
        } else {
            super.drawMarkers(canvas);
        }
    }

    private void drawTopMarkers(Canvas canvas) {
        if (mMarker == null) {
            return;
        }

        if (!isDrawMarkersEnabled() || !valuesToHighlight()) {
            return;
        }

        for (int i = 0; i < mIndicesToHighlight.length; i++) {

            Highlight highlight = mIndicesToHighlight[i];

            IDataSet set = mData.getDataSetByIndex(highlight.getDataSetIndex());

            Entry e = mData.getEntryForHighlight(mIndicesToHighlight[i]);
            if (e == null || set == null) {
                continue;
            }
            int entryIndex = set.getEntryIndex(e);

            // make sure entry not null
            if (entryIndex < 0 || entryIndex > set.getEntryCount() * mAnimator.getPhaseX()) {
                continue;
            }

            if (mMarker != null && mMarker instanceof View) {
                View markerView = (View) this.mMarker;

                int measuredHeight = markerView.getHeight();
                int measuredWidth = markerView.getWidth();

                float x = highlight.getDrawX() - measuredWidth / 2;
                if (!mViewPortHandler.isInBoundsLeft(x)) {
                    x = mViewPortHandler.contentLeft();
                }
                if (x + measuredWidth >= mViewPortHandler.contentRight()) {
                    x = mViewPortHandler.contentRight() - measuredWidth;
                }

                float y = mViewPortHandler.contentTop() - measuredHeight;

                // callbacks to update the content
                mMarker.refreshContent(e, highlight);

                // draw the marker
                mMarker.draw(canvas, x, y);
            }
        }
    }
}
