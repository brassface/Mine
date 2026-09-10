package com.coderpage.mine.app.tally.module.chart.widget;

import android.graphics.Matrix;
import android.view.MotionEvent;
import android.view.View;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet;
import com.github.mikephil.charting.listener.BarLineChartTouchListener;

/**
 * @author lc. 2019-05-19 14:59
 * @since 0.6.2
 */

public class MineLineChartTouchListener extends BarLineChartTouchListener {

    private boolean mHandlerByMarkerView = false;
    private float mDownX;
    private float mDownY;
    /** 为 true 时：再点同一高亮不取消选中；已无选中时点空白不重复回调 */
    private boolean mStableSelection;
    /** 是否响应顶部 Marker 点击（年折线关闭，点上方视为空白） */
    private boolean mMarkerClickEnabled = true;

    public MineLineChartTouchListener(BarLineChartBase<? extends BarLineScatterCandleBubbleData<? extends
            IBarLineScatterCandleBubbleDataSet<? extends Entry>>> chart, Matrix touchMatrix, float dragTriggerDistance) {
        super(chart, touchMatrix, dragTriggerDistance);
    }

    public void setStableSelection(boolean stableSelection) {
        mStableSelection = stableSelection;
    }

    public void setMarkerClickEnabled(boolean markerClickEnabled) {
        mMarkerClickEnabled = markerClickEnabled;
    }

    @Override
    protected void performHighlight(Highlight h, MotionEvent e) {
        if (!mStableSelection) {
            super.performHighlight(h, e);
            return;
        }
        if (h == null) {
            // 点击空白：已无选中则忽略，避免当月饼图重复渲染
            if (!mChart.valuesToHighlight() && mLastHighlighted == null) {
                return;
            }
            mChart.highlightValue(null, true);
            mLastHighlighted = null;
            return;
        }
        // 再次点击同一柱：保持选中，不切回当月
        if (h.equalTo(mLastHighlighted)) {
            return;
        }
        Highlight[] current = mChart.getHighlighted();
        if (current != null && current.length > 0 && h.equalTo(current[0])) {
            mLastHighlighted = h;
            return;
        }
        mChart.highlightValue(h, true);
        mLastHighlighted = h;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // 处理 marker view 点击（可关闭，关闭后触点交给图表按空白处理）
        IMarker marker = mChart.getMarker();
        if (mMarkerClickEnabled && marker instanceof MarkViewMine) {
            MarkViewMine markerView = (MarkViewMine) marker;
            switch (event.getAction() & MotionEvent.ACTION_MASK) {

                case MotionEvent.ACTION_DOWN:
                    mDownX = event.getX();
                    mDownY = event.getY();
                    mHandlerByMarkerView = markerView.getBound().contains(event.getX(), event.getY());
                    if (mHandlerByMarkerView) {
                        return true;
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    if (mHandlerByMarkerView) {
                        if (Math.abs(mDownX - event.getX()) <= 5 && Math.abs(mDownY - event.getY()) <= 5) {
                            markerView.performClick();
                        }
                        return true;
                    }
                    break;

                default:
                    break;
            }
        }
        return super.onTouch(v, event);
    }
}
