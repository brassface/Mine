package com.coderpage.mine.app.tally.module.chart.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;
import android.widget.TextView;

import com.coderpage.base.utils.ResUtils;
import com.coderpage.mine.R;
import com.coderpage.mine.app.tally.common.utils.TallyUtils;
import com.coderpage.mine.app.tally.databinding.CommonBindAdapter;
import com.coderpage.mine.app.tally.module.chart.data.Month;
import com.coderpage.mine.app.tally.module.chart.data.MonthlyEntryData;
import com.coderpage.mine.common.Font;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;

/**
 * @author lc. 2019-04-08 23:25
 * @since 0.6.0
 *
 * 年账单折线图。每月的数据
 */

public class MarkerViewMonthData extends MarkViewMine {

    private TextView mTvData;
    private TextView mTvDate;
    private Entry mEntry;
    private OnClickListener mListener;
    private boolean mShowExpense;

    public MarkerViewMonthData(Context context, int layoutResource) {
        this(context, layoutResource, true);
    }

    public MarkerViewMonthData(Context context, int layoutResource, boolean showExpense) {
        super(context, layoutResource);
        mShowExpense = showExpense;
        mTvData = findViewById(R.id.tvData);
        mTvDate = findViewById(R.id.tvDate);
        CommonBindAdapter.setTypeFace(mTvData, Font.QUICKSAND_MEDIUM);
        CommonBindAdapter.setTypeFace(mTvDate, Font.QUICKSAND_BOLD);

        setOnClickListener(v -> {
            if (mListener != null && mEntry != null) {
                mListener.onClick(v, mEntry);
            }
        });
    }

    public void setOnClickListener(OnClickListener l) {
        mListener = l;
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        mEntry = e;
        if (e == null || !(e.getData() instanceof MonthlyEntryData)) {
            mTvDate.setText("");
            mTvData.setText("");
            super.refreshContent(e, highlight);
            return;
        }
        MonthlyEntryData entryData = (MonthlyEntryData) e.getData();
        Month month = entryData.getMonth();
        String monthInfo = ResUtils.getString(getContext(), R.string.tally_month_info_format,
                month.getYear(), month.getMonth());
        double amount = mShowExpense ? entryData.getExpenseAmount() : entryData.getIncomeAmount();
        String moneyInfo = "¥" + TallyUtils.formatDisplayMoney(amount);
        mTvDate.setText(monthInfo);
        mTvData.setText(moneyInfo);
        super.refreshContent(e, highlight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public interface OnClickListener {
        /**
         * 点击回调
         *
         * @param view  view
         * @param entry 数据
         */
        void onClick(View view, Entry entry);
    }
}
