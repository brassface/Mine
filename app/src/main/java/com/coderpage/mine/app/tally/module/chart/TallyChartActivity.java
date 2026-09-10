package com.coderpage.mine.app.tally.module.chart;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.coderpage.base.utils.ArrayUtils;
import com.coderpage.base.utils.ResUtils;
import com.coderpage.base.utils.UIUtils;
import com.coderpage.mine.MineApp;
import com.coderpage.mine.R;
import com.coderpage.mine.app.tally.common.router.TallyRouter;
import com.coderpage.mine.app.tally.module.chart.data.CategoryData;
import com.coderpage.mine.app.tally.module.chart.data.DailyData;
import com.coderpage.mine.app.tally.module.chart.data.Month;
import com.coderpage.mine.app.tally.module.chart.data.MonthlyData;
import com.coderpage.mine.app.tally.module.chart.data.MonthlyEntryData;
import com.coderpage.mine.app.tally.module.chart.widget.MarkerViewDailyData;
import com.coderpage.mine.app.tally.module.chart.widget.MarkerViewMonthData;
import com.coderpage.mine.app.tally.module.chart.widget.MineBarChart;
import com.coderpage.mine.app.tally.module.chart.widget.MineLineChart;
import com.coderpage.mine.app.tally.module.chart.widget.MinePieChart;
import com.coderpage.mine.app.tally.persistence.model.CategoryModel;
import com.coderpage.mine.common.Font;
import com.coderpage.mine.tally.module.chart.TallyChartActivityBinding;
import com.coderpage.mine.ui.BaseActivity;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lc. 2018-09-24 14:37
 * @since 0.6.0
 */
@Route(path = TallyRouter.CHART)
public class TallyChartActivity extends BaseActivity {

    static final String EXTRA_YEAR = "extra_year";
    static final String EXTRA_MONTH = "extra_month";

    private final int[] categoryExpenseColorArray = new int[]{
            ResUtils.getColor(MineApp.getAppContext(), R.color.categoryExpenseColor1),
            ResUtils.getColor(MineApp.getAppContext(), R.color.categoryExpenseColor2),
            ResUtils.getColor(MineApp.getAppContext(), R.color.categoryExpenseColor3),
            ResUtils.getColor(MineApp.getAppContext(), R.color.categoryExpenseColor4),
            ResUtils.getColor(MineApp.getAppContext(), R.color.categoryExpenseColor5),
            ResUtils.getColor(MineApp.getAppContext(), R.color.categoryExpenseColor6),
            ResUtils.getColor(MineApp.getAppContext(), R.color.categoryExpenseColor7),
            ResUtils.getColor(MineApp.getAppContext(), R.color.categoryExpenseColor9)
    };
    private final int[] categoryIncomeColorArray = new int[]{
            ResUtils.getColor(MineApp.getAppContext(), R.color.categoryIncomeColor1),
            ResUtils.getColor(MineApp.getAppContext(), R.color.categoryIncomeColor2),
            ResUtils.getColor(MineApp.getAppContext(), R.color.categoryIncomeColor3),
            ResUtils.getColor(MineApp.getAppContext(), R.color.categoryIncomeColor4),
            ResUtils.getColor(MineApp.getAppContext(), R.color.categoryIncomeColor5),
            ResUtils.getColor(MineApp.getAppContext(), R.color.categoryIncomeColor6),
            ResUtils.getColor(MineApp.getAppContext(), R.color.categoryIncomeColor7),
            ResUtils.getColor(MineApp.getAppContext(), R.color.categoryIncomeColor9)
    };

    private MineBarChart mBarChart;
    private MineLineChart mLineChart;
    private MinePieChart mPieChart;
    private RecyclerView mCategoryDataRecycler;
    private TallyChartCategoryDataAdapter mCategoryDataAdapter;

    private TallyChartActivityBinding mBinding;
    private TallyChartViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.tally_module_chart_tally_chart_activity);
        mViewModel = ViewModelProviders.of(this).get(TallyChartViewModel.class);
        getLifecycle().addObserver(mViewModel);

        initView();
        subScribeUi();
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setToolbarAsClose(v -> finish());
    }

    /**
     * 打开图片页
     *
     * @param activity activity
     * @param year     年
     * @param month    月
     */
    public static void open(Activity activity, int year, int month) {
        Intent intent = new Intent(activity, TallyChartActivity.class);
        intent.putExtra(EXTRA_YEAR, year);
        intent.putExtra(EXTRA_MONTH, month);
        activity.startActivity(intent);
    }

    private void initView() {
        mBarChart = mBinding.barChart;
        mLineChart = mBinding.lineChart;
        mPieChart = mBinding.pieChart;
        mCategoryDataRecycler = mBinding.recyclerCategory;
        mCategoryDataRecycler.setLayoutManager(new LinearLayoutManager(self(), LinearLayoutManager.VERTICAL, false));
        mCategoryDataAdapter = new TallyChartCategoryDataAdapter(mViewModel);
        mCategoryDataRecycler.setAdapter(mCategoryDataAdapter);
        mCategoryDataRecycler.setHasFixedSize(true);
        mCategoryDataRecycler.setNestedScrollingEnabled(false);
    }

    private void subScribeUi() {
        mBinding.setActivity(this);
        mBinding.setVm(mViewModel);

        // 日消费柱状图
        mViewModel.getDailyExpenseList().observe(this, dailyDataList -> {
            if (dailyDataList != null) {
                showDailyBarChart(true, dailyDataList);
            }
        });
        // 日收入柱状图
        mViewModel.getDailyIncomeList().observe(this, dailyDataList -> {
            if (dailyDataList != null) {
                showDailyBarChart(false, dailyDataList);
            }
        });
        // 月支出、月收入折线图
        mViewModel.getMonthlyDataList().observe(this, monthlyDataList -> {
            if (monthlyDataList != null) {
                boolean isExpense = mViewModel.getDisplayExpenseChart().get();
                List<MonthlyData> list = isExpense
                        ? monthlyDataList.getExpenseList() : monthlyDataList.getIncomeList();
                showMonthlyLineChart(isExpense, list);
            }
        });
        // 支出分类饼图
        mViewModel.getCategoryExpenseDataList().observe(this, categoryDataList -> {
            if (categoryDataList != null) {
                mCategoryDataAdapter.setDataList(categoryDataList);
                showPieChart(categoryDataList, CategoryModel.TYPE_EXPENSE);
            }
        });
        // 收入分类饼图
        mViewModel.getCategoryIncomeDataList().observe(this, categoryDataList -> {
            if (categoryDataList != null) {
                mCategoryDataAdapter.setDataList(categoryDataList);
                showPieChart(categoryDataList, CategoryModel.TYPE_INCOME);
            }
        });
        mViewModel.getViewReliedTask().observe(this, task -> {
            if (task != null) {
                task.execute(this);
            }
        });
        mViewModel.getBarHighlightIndex().observe(this, index -> {
            if (mViewModel.getDisplayDailyChart().get()) {
                applyBarHighlight(index, true);
            } else {
                applyLineHighlight(index);
            }
        });
    }

    /**
     * 安全高亮日柱，避免数据切换时下标越界导致闪退
     */
    private void applyBarHighlight(Integer index, boolean moveToVisible) {
        if (mBarChart == null) {
            return;
        }
        if (index == null || index < 0 || mBarChart.getData() == null
                || mBarChart.getData().getDataSetCount() <= 0) {
            mBarChart.clearHighlightSilently();
            return;
        }
        int entryCount = mBarChart.getData().getDataSetByIndex(0).getEntryCount();
        if (index >= entryCount) {
            mBarChart.clearHighlightSilently();
            return;
        }
        mBarChart.highlightIndexSilently(index);
        if (moveToVisible) {
            float targetX = Math.max(0, index - 4);
            mBarChart.moveViewToX(targetX);
        }
    }

    /**
     * 安全高亮年折线月份点
     */
    private void applyLineHighlight(Integer index) {
        if (mLineChart == null) {
            return;
        }
        if (index == null || index < 0 || mLineChart.getData() == null
                || mLineChart.getData().getDataSetCount() <= 0) {
            mLineChart.clearHighlightSilently();
            return;
        }
        int entryCount = mLineChart.getData().getDataSetByIndex(0).getEntryCount();
        if (index >= entryCount) {
            mLineChart.clearHighlightSilently();
            return;
        }
        mLineChart.highlightIndexSilently(index);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_tally_chart, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_date_select:
                mViewModel.onSelectDateClick(this);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 显示柱状图
     *
     * @param isShowExpense 是否显示为支出图
     * @param dailyDataList 每日支出或每日收入数据
     */
    private void showDailyBarChart(boolean isShowExpense, List<DailyData> dailyDataList) {
        int barColor = isShowExpense ? UIUtils.getColor(this, R.color.expenseColor)
                : UIUtils.getColor(this, R.color.incomeColor);

        List<BarEntry> yValues = new ArrayList<>();
        if (dailyDataList != null) {
            for (int i = 0; i < dailyDataList.size(); i++) {
                DailyData dailyData = dailyDataList.get(i);
                BarEntry barEntry = new BarEntry(i, dailyData.getAmount());
                barEntry.setData(dailyData);
                yValues.add(barEntry);
            }
        }

        BarData barData = null;
        if (!yValues.isEmpty()) {
            BarDataSet barDataSet = new BarDataSet(yValues, "");
            barDataSet.setColor(Color.GRAY);
            barDataSet.setDrawValues(false);
            barDataSet.setFormLineWidth(0);
            barDataSet.setBarShadowColor(ResUtils.getColor(this, R.color.chartGridLine));
            barDataSet.setColor(barColor);

            barData = new BarData(barDataSet);
            barData.setBarWidth(0.8f);
        }

        XAxis xAxis = mBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisLineColor(ResUtils.getColor(this, R.color.chartGridLine));
        xAxis.setDrawGridLines(false);
        xAxis.setGridColor(ResUtils.getColor(this, R.color.chartGridLine));
        xAxis.setDrawAxisLine(true);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(Math.min(10, Math.max(yValues.size(), 1)));
        xAxis.setValueFormatter((value, axis) -> {
            String format = "";
            int index = Math.round(value);
            if (index < 0 || dailyDataList == null || index >= dailyDataList.size()) {
                return format;
            }
            // 首尾与中间刻度显示日期，避免一屏过密
            if (index != 0
                    && index != dailyDataList.size() - 1
                    && index != axis.mEntryCount / 2
                    && index % 3 != 0) {
                return format;
            }
            DailyData dailyData = dailyDataList.get(index);
            if (dailyData != null) {
                return dailyData.getMonth() + "-" + dailyData.getDayOfMonth();
            }
            return format;
        });

        YAxis axisLeft = mBarChart.getAxisLeft();
        axisLeft.setAxisMinimum(0);
        axisLeft.setDrawLabels(false);
        axisLeft.setDrawGridLines(false);
        axisLeft.setDrawAxisLine(false);

        YAxis axisRight = mBarChart.getAxisRight();
        axisRight.setAxisMinimum(0);
        axisRight.setDrawLabels(false);
        axisRight.setDrawGridLines(false);
        axisRight.setDrawAxisLine(false);

        mBarChart.setViewPortOffsets(
                UIUtils.dp2px(self(), 16F),
                UIUtils.dp2px(self(), 40F),
                UIUtils.dp2px(self(), 16F),
                UIUtils.dp2px(self(), 16F));
        MarkerViewDailyData markerView = new MarkerViewDailyData(self(), R.layout.tally_module_chart_marker_view);
        mBarChart.setDrawMarkOnTop(true);
        mBarChart.setMarker(markerView);
        mBarChart.setNoDataText(ResUtils.getString(self(), R.string.tally_chart_empty_tip));
        mBarChart.setNoDataTextColor(ResUtils.getColor(self(), R.color.appTextColorPrimary));
        mBarChart.setScaleEnabled(true);
        mBarChart.setScaleYEnabled(false);
        mBarChart.setScaleXEnabled(false);
        mBarChart.setDragEnabled(true);
        mBarChart.setPinchZoom(false);
        mBarChart.setDoubleTapToZoomEnabled(false);
        mBarChart.setDragDecelerationEnabled(true);
        mBarChart.setDrawBorders(false);
        mBarChart.setDrawBarShadow(true);
        mBarChart.setDrawGridBackground(false);
        mBarChart.setDrawValueAboveBar(false);
        mBarChart.setFitBars(true);
        mBarChart.setDescription(null);
        mBarChart.getLegend().setEnabled(false);
        mBarChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e != null && e.getData() instanceof DailyData) {
                    mViewModel.onDailyBarSelected((DailyData) e.getData());
                }
            }

            @Override
            public void onNothingSelected() {
                mViewModel.onDailyBarCleared();
            }
        });
        // 先清空旧高亮，再换数据，避免切换支出/收入时沿用旧下标闪退
        mBarChart.clearHighlightSilently();
        mBarChart.setData(barData);
        if (barData != null && yValues.size() > 0) {
            float visibleCount = Math.min(10f, yValues.size());
            mBarChart.setVisibleXRangeMaximum(visibleCount);
            mBarChart.setVisibleXRangeMinimum(visibleCount);
            // 默认展示 1~10 号；若已选中某天则滚到该天附近
            DailyData selected = mViewModel.getSelectedDailyData();
            int selectedIndex = -1;
            if (selected != null && dailyDataList != null) {
                for (int i = 0; i < dailyDataList.size(); i++) {
                    DailyData item = dailyDataList.get(i);
                    if (item != null
                            && item.getYear() == selected.getYear()
                            && item.getMonth() == selected.getMonth()
                            && item.getDayOfMonth() == selected.getDayOfMonth()) {
                        selectedIndex = i;
                        break;
                    }
                }
            }
            if (selectedIndex >= 0) {
                mBarChart.moveViewToX(Math.max(0, selectedIndex - 4));
                mBarChart.highlightIndexSilently(selectedIndex);
            } else {
                mBarChart.moveViewToX(0f);
            }
            mBarChart.animateY(500);
        } else {
            mBarChart.invalidate();
        }
    }

    /**
     * 显示年账单折线图（按支出/收入 Tab 只画一条线）
     *
     * @param isShowExpense 是否显示支出
     * @param monthlyDataList 每月数据
     */
    private void showMonthlyLineChart(boolean isShowExpense, List<MonthlyData> monthlyDataList) {
        int lineColor = isShowExpense ? UIUtils.getColor(this, R.color.expenseColor)
                : UIUtils.getColor(this, R.color.incomeColor);

        List<Entry> yValues = new ArrayList<>();
        List<String> xAxisLabels = new ArrayList<>();
        if (monthlyDataList != null) {
            for (int i = 0; i < monthlyDataList.size(); i++) {
                MonthlyData item = monthlyDataList.get(i);
                if (item == null) {
                    continue;
                }
                Entry entry = new Entry(i, item.getAmount());
                MonthlyEntryData monthlyEntryData = new MonthlyEntryData().setMonth(item.getMonth());
                if (isShowExpense) {
                    monthlyEntryData.setExpenseAmount(item.getAmount());
                } else {
                    monthlyEntryData.setIncomeAmount(item.getAmount());
                }
                entry.setData(monthlyEntryData);
                yValues.add(entry);
                Month month = item.getMonth();
                String label = month == null ? ""
                        : UIUtils.getString(this, R.string.tally_month_format, month.getMonth());
                xAxisLabels.add(label);
            }
        }

        LineData lineData = null;
        if (!yValues.isEmpty()) {
            LineDataSet dataSet = new LineDataSet(yValues, "");
            dataSet.setColor(lineColor);
            dataSet.setDrawValues(false);
            dataSet.setFormLineWidth(0);
            dataSet.setCircleColor(lineColor);
            dataSet.setCircleRadius(3f);
            dataSet.setDrawHorizontalHighlightIndicator(false);
            dataSet.setDrawVerticalHighlightIndicator(true);
            dataSet.setHighLightColor(ResUtils.getColor(self(), R.color.black));
            dataSet.setMode(LineDataSet.Mode.LINEAR);
            lineData = new LineData(dataSet);
        }

        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setDrawLabels(true);
        xAxis.setTextColor(ResUtils.getColor(getApplicationContext(), R.color.appTextColorLabel));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisLineColor(ResUtils.getColor(this, R.color.chartGridLine));
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter((value, axis) -> {
            int index = Math.round(value);
            if (index < 0 || index >= xAxisLabels.size()) {
                return "";
            }
            return xAxisLabels.get(index);
        });

        YAxis axisLeft = mLineChart.getAxisLeft();
        axisLeft.setAxisMinimum(0);
        axisLeft.setDrawLabels(false);
        axisLeft.setDrawGridLines(false);
        axisLeft.setDrawAxisLine(false);

        YAxis axisRight = mLineChart.getAxisRight();
        axisRight.setAxisMinimum(0);
        axisRight.setDrawLabels(false);
        axisRight.setDrawGridLines(false);
        axisRight.setDrawAxisLine(false);

        if (lineData != null) {
            mLineChart.getXAxis().setAxisMaximum(lineData.getXMax() + 0.2f);
            mLineChart.getXAxis().setAxisMinimum(Math.max(0, lineData.getXMin() - 0.2f));
        }

        mLineChart.setViewPortOffsets(
                UIUtils.dp2px(self(), 16F),
                UIUtils.dp2px(self(), 40F),
                UIUtils.dp2px(self(), 16F),
                UIUtils.dp2px(self(), 16F));
        MarkerViewMonthData markerView = new MarkerViewMonthData(
                self(), R.layout.tally_module_chart_marker_view, isShowExpense);
        mLineChart.setNoDataText(ResUtils.getString(self(), R.string.tally_chart_empty_tip));
        mLineChart.setNoDataTextColor(ResUtils.getColor(self(), R.color.appTextColorPrimary));
        mLineChart.setDragEnabled(true);
        mLineChart.setScaleEnabled(false);
        mLineChart.setVisibleXRange(0, 11);
        mLineChart.setDrawBorders(false);
        mLineChart.setDrawGridBackground(false);
        mLineChart.setDescription(null);
        mLineChart.getLegend().setEnabled(false);
        mLineChart.setScaleMinima(1, 1);
        mLineChart.setDrawMarkers(true);
        mLineChart.setDrawMarkOnTop(true);
        mLineChart.setMarker(markerView);
        mLineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e != null && e.getData() instanceof MonthlyEntryData) {
                    mViewModel.onMonthlyPointSelected((MonthlyEntryData) e.getData());
                }
            }

            @Override
            public void onNothingSelected() {
                mViewModel.onMonthlyPointCleared();
            }
        });
        mLineChart.clearHighlightSilently();
        mLineChart.setData(lineData);
        if (lineData != null && yValues.size() > 0) {
            Month selected = mViewModel.getSelectedMonth();
            int selectedIndex = -1;
            if (selected != null && monthlyDataList != null) {
                for (int i = 0; i < monthlyDataList.size(); i++) {
                    MonthlyData item = monthlyDataList.get(i);
                    if (item != null && item.getMonth() != null
                            && item.getMonth().getYear() == selected.getYear()
                            && item.getMonth().getMonth() == selected.getMonth()) {
                        selectedIndex = i;
                        break;
                    }
                }
            }
            if (selectedIndex >= 0) {
                mLineChart.highlightIndexSilently(selectedIndex);
            }
            mLineChart.animateY(500);
        } else {
            mLineChart.invalidate();
        }
    }

    private void showPieChart(List<CategoryData> categoryDataList, int type) {
        final DecimalFormat percentFormat = new DecimalFormat("0.00");
        Typeface valueTypeface = Typeface.createFromAsset(getAssets(), "font/" + Font.QUICKSAND_MEDIUM.getName());

        List<PieEntry> pieEntryList = new ArrayList<>();
        ArrayUtils.forEach(categoryDataList, (count, index, item) -> {
            pieEntryList.add(new PieEntry((float) item.getAmount(), item.getCategoryName()));
        });

        PieDataSet pieDataSet = new PieDataSet(pieEntryList, "");
        int[] colors = type == CategoryModel.TYPE_INCOME ? categoryIncomeColorArray : categoryExpenseColorArray;
        pieDataSet.setColors(colors);
        ArrayList<Integer> colorList = new ArrayList<>(colors.length);
        for (int color : colors) {
            colorList.add(color);
        }

        pieDataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        pieDataSet.setValueLineColor(ResUtils.getColor(self(), R.color.appTextColorPrimary));
        pieDataSet.setValueTextColors(colorList);
        pieDataSet.setValueTextSize(9);
        pieDataSet.setValueTypeface(valueTypeface);
        pieDataSet.setValueLineVariableLength(true);
        pieDataSet.setValueFormatter((value, entry, dataSetIndex, viewPortHandler) -> {
            return percentFormat.format(value) + "%";
        });

        PieData pieData = null;
        if (!pieEntryList.isEmpty()) {
            pieData = new PieData(pieDataSet);
        }

        mPieChart.setNoDataText(ResUtils.getString(self(), R.string.tally_chart_empty_tip));
        mPieChart.setNoDataTextColor(ResUtils.getColor(self(), R.color.appTextColorPrimary));
        mPieChart.setExtraTopOffset(20);
        mPieChart.setExtraBottomOffset(20);
        mPieChart.setUsePercentValues(true);
        mPieChart.setDescription(null);
        mPieChart.setCenterTextSize(20f);
        mPieChart.setDrawEntryLabels(false);
        mPieChart.setHighlightPerTapEnabled(true);
        mPieChart.getLegend().setEnabled(false);
        mPieChart.getLegend().setOrientation(Legend.LegendOrientation.HORIZONTAL);
        mPieChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        mPieChart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        mPieChart.getLegend().setWordWrapEnabled(true);
        mPieChart.setData(pieData);
        mPieChart.animateY(1400, Easing.EasingOption.EaseInOutQuart);
    }
}
