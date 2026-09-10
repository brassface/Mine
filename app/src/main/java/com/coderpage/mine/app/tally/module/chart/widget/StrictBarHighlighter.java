package com.coderpage.mine.app.tally.module.chart.widget;

import com.github.mikephil.charting.highlight.BarHighlighter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider;
import com.github.mikephil.charting.utils.MPPointD;

/**
 * @author zzy. 2026-09-10 11:15
 * @since 0.6.2
 *
 * 仅在真实命中柱体时返回高亮，避免点击空白吸附到最近一天。
 */
public class StrictBarHighlighter extends BarHighlighter {

    public StrictBarHighlighter(BarDataProvider chart) {
        super(chart);
    }

    @Override
    public Highlight getHighlight(float x, float y) {
        Highlight highlight = super.getHighlight(x, y);
        if (highlight == null || mChart.getBarData() == null) {
            return null;
        }

        MPPointD pos = mChart.getTransformer(mChart.getBarData().getDataSetByIndex(
                highlight.getDataSetIndex()).getAxisDependency()).getValuesByTouchPoint(x, y);
        float touchX = (float) pos.x;
        float touchY = (float) pos.y;
        float barX = highlight.getX();
        float halfWidth = mChart.getBarData().getBarWidth() / 2f;

        // 水平方向：必须落在柱宽范围内，不能吸附到邻柱
        if (Math.abs(touchX - barX) > halfWidth) {
            return null;
        }

        float barValue = highlight.getY();
        // 垂直方向：必须点在柱体高度内；零值柱仅响应靠近基线的极小区域，避免点到阴影空白被选中
        if (barValue <= 0f) {
            if (touchY < 0f || touchY > 0.01f) {
                return null;
            }
        } else if (touchY < 0f || touchY > barValue) {
            return null;
        }

        return highlight;
    }
}
