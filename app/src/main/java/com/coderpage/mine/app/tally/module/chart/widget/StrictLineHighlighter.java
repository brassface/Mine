package com.coderpage.mine.app.tally.module.chart.widget;

import com.github.mikephil.charting.highlight.ChartHighlighter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.utils.MPPointD;

/**
 * @author zzy. 2026-09-10 11:35
 * @since 0.6.2
 *
 * 仅在命中折线点附近（含点下方至基线）时高亮，避免点击空白吸附到最近月份。
 */
public class StrictLineHighlighter extends ChartHighlighter<LineDataProvider> {

    /** 相邻月份间距为 1，水平容差取半距略小 */
    private static final float HALF_X_RANGE = 0.45f;

    public StrictLineHighlighter(LineDataProvider chart) {
        super(chart);
    }

    @Override
    public Highlight getHighlight(float x, float y) {
        Highlight highlight = super.getHighlight(x, y);
        if (highlight == null || mChart.getLineData() == null
                || mChart.getLineData().getDataSetCount() <= 0) {
            return null;
        }

        MPPointD pos = mChart.getTransformer(mChart.getLineData().getDataSetByIndex(
                highlight.getDataSetIndex()).getAxisDependency()).getValuesByTouchPoint(x, y);
        float touchX = (float) pos.x;
        float touchY = (float) pos.y;
        float pointX = highlight.getX();
        float pointY = highlight.getY();

        if (Math.abs(touchX - pointX) > HALF_X_RANGE) {
            return null;
        }

        // 点本身及其下方（到基线）；零值点仅响应靠近基线
        if (pointY <= 0f) {
            if (touchY < 0f || touchY > 0.01f) {
                return null;
            }
        } else if (touchY < 0f || touchY > pointY) {
            return null;
        }

        return highlight;
    }
}
