package com.coderpage.mine.app.tally.module.chart;

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Intent;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.util.Pair;

import com.coderpage.base.common.Callback;
import com.coderpage.base.common.IError;
import com.coderpage.base.common.SimpleCallback;
import com.coderpage.base.utils.ArrayUtils;
import com.coderpage.base.utils.LogUtils;
import com.coderpage.base.utils.ResUtils;
import com.coderpage.base.utils.WrappedLong;
import com.coderpage.base.utils.WrappedObject;
import com.coderpage.framework.BaseViewModel;
import com.coderpage.framework.ViewReliedTask;
import com.coderpage.mine.R;
import com.coderpage.mine.app.tally.common.utils.TallyUtils;
import com.coderpage.mine.app.tally.module.chart.data.CategoryData;
import com.coderpage.mine.app.tally.module.chart.data.DailyData;
import com.coderpage.mine.app.tally.module.chart.data.Month;
import com.coderpage.mine.app.tally.module.chart.data.MonthlyData;
import com.coderpage.mine.app.tally.module.chart.data.MonthlyDataList;
import com.coderpage.mine.app.tally.module.chart.data.MonthlyEntryData;
import com.coderpage.mine.app.tally.module.records.RecordQuery;
import com.coderpage.mine.app.tally.module.records.RecordsActivity;
import com.coderpage.mine.app.tally.ui.widget.MonthSelectDialog;
import com.coderpage.mine.app.tally.utils.DateUtils;
import com.coderpage.mine.app.tally.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lc. 2018-09-24 15:34
 * @since 0.6.0
 *
 * 展示 年账单折线图、日支出柱状图、日收入柱状图、支出分类饼图、收入分类饼图
 */

public class TallyChartViewModel extends BaseViewModel implements LifecycleObserver {

    private static final String TAG = LogUtils.makeLogTag(TallyChartViewModel.class);

    /** 账单开始日期 */
    private Calendar mStartDate;
    /** 账单结束日期 */
    private Calendar mEndDate;

    private List<Month> mSelectableMonthList = new ArrayList<>();

    /** 当前时间 */
    private ObservableField<String> mCurrentDateText = new ObservableField<>("");
    /** 支出总额 */
    private ObservableField<String> mExpenseTotalAmountText = new ObservableField<>("");
    /** 收入总额 */
    private ObservableField<String> mIncomeTotalAmountText = new ObservableField<>("");
    /** 是否显示为日账单 */
    private ObservableBoolean mDisplayDailyChart = new ObservableBoolean(true);
    /** 是否显示为支出账单 */
    private ObservableBoolean mDisplayExpenseChart = new ObservableBoolean(true);

    private List<DailyData> mDailyExpenseList = new ArrayList<>();
    private List<DailyData> mDailyIncomeList = new ArrayList<>();
    private List<MonthlyData> mMonthlyExpenseList = new ArrayList<>();
    private List<MonthlyData> mMonthlyIncomeList = new ArrayList<>();
    private List<CategoryData> mCategoryDailyExpenseList = new ArrayList<>();
    private List<CategoryData> mCategoryDailyIncomeList = new ArrayList<>();
    private List<CategoryData> mCategoryYearlyExpenseList = new ArrayList<>();
    private List<CategoryData> mCategoryYearlyIncomeList = new ArrayList<>();

    /** 当前选中的日期；null 表示展示整月 */
    private DailyData mSelectedDailyData;
    /** 当前选中的月份；null 表示展示整年 */
    private Month mSelectedMonth;

    /** 月单位的收入列表 */
    private MutableLiveData<MonthlyDataList> mObservableMonthlyDataList = new MutableLiveData<>();
    /** 日单位的支出列表 */
    private MutableLiveData<List<DailyData>> mObservableDailyExpenseList = new MutableLiveData<>();
    /** 日单位的收入列表 */
    private MutableLiveData<List<DailyData>> mObservableDailyIncomeList = new MutableLiveData<>();
    /** 消费分类金额列表 */
    private MutableLiveData<List<CategoryData>> mObservableCategoryExpenseDataList = new MutableLiveData<>();
    /** 收入分类金额列表 */
    private MutableLiveData<List<CategoryData>> mObservableCategoryIncomeDataList = new MutableLiveData<>();
    /** 柱图/折线图选中下标，供 Activity 高亮；-1 表示取消选中 */
    private MutableLiveData<Integer> mObservableBarHighlightIndex = new MutableLiveData<>();

    private MutableLiveData<ViewReliedTask<Activity>> mViewReliedTask = new MutableLiveData<>();

    private TallyChartRepository mRepository;

    public TallyChartViewModel(Application application) {
        super(application);
        mRepository = new TallyChartRepository();
    }

    public ObservableField<String> getCurrentDateText() {
        return mCurrentDateText;
    }

    public ObservableBoolean getDisplayDailyChart() {
        return mDisplayDailyChart;
    }

    public ObservableField<String> getExpenseTotalAmountText() {
        return mExpenseTotalAmountText;
    }

    public ObservableField<String> getIncomeTotalAmountText() {
        return mIncomeTotalAmountText;
    }

    public ObservableBoolean getDisplayExpenseChart() {
        return mDisplayExpenseChart;
    }

    LiveData<List<DailyData>> getDailyExpenseList() {
        return mObservableDailyExpenseList;
    }

    LiveData<List<DailyData>> getDailyIncomeList() {
        return mObservableDailyIncomeList;
    }

    LiveData<MonthlyDataList> getMonthlyDataList() {
        return mObservableMonthlyDataList;
    }

    LiveData<List<CategoryData>> getCategoryExpenseDataList() {
        return mObservableCategoryExpenseDataList;
    }

    LiveData<List<CategoryData>> getCategoryIncomeDataList() {
        return mObservableCategoryIncomeDataList;
    }

    LiveData<Integer> getBarHighlightIndex() {
        return mObservableBarHighlightIndex;
    }

    /** 当前选中的日期，未选中时返回 null */
    DailyData getSelectedDailyData() {
        return mSelectedDailyData;
    }

    /** 当前选中的月份，未选中时返回 null */
    Month getSelectedMonth() {
        return mSelectedMonth;
    }

    LiveData<ViewReliedTask<Activity>> getViewReliedTask() {
        return mViewReliedTask;
    }

    /**
     * 选择日期点击
     *
     * @param activity activity
     */
    void onSelectDateClick(Activity activity) {
        Month currentMonth = new Month();
        currentMonth.setYear(mStartDate.get(Calendar.YEAR));
        currentMonth.setMonth(mStartDate.get(Calendar.MONTH) + 1);

        new MonthSelectDialog(activity, mSelectableMonthList, new MonthSelectDialog.DateSelectListener() {
            @Override
            public void onMonthSelect(MonthSelectDialog dialog, Month month) {
                dialog.dismiss();
                Pair<Long, Long> monthDateRange = DateUtils.monthDateRange(month.getYear(), month.getMonth());
                mStartDate.setTimeInMillis(monthDateRange.first);
                mEndDate.setTimeInMillis(monthDateRange.second);

                clearSelectedDay(false);
                clearSelectedMonth(false);
                clearData();
                refreshData();
            }
        }, currentMonth).show();
    }

    /**
     * 日柱选中：下方饼图/分类切换为当天数据
     */
    void onDailyBarSelected(DailyData dailyData) {
        if (dailyData == null || !mDisplayDailyChart.get()) {
            return;
        }
        // 已选中同一天：不重复刷新饼图/详情
        if (isSameDailyData(mSelectedDailyData, dailyData)) {
            return;
        }
        mSelectedDailyData = dailyData;
        displaySelectedDayAmountTotal();
        loadCategoryForSelectedDay();
    }

    /**
     * 取消日柱选中：下方恢复当月数据
     */
    void onDailyBarCleared() {
        // 已是当月视图：点空白不重复渲染
        if (mSelectedDailyData == null) {
            return;
        }
        clearSelectedDay(true);
    }

    /**
     * 年折线点选中：下方饼图/分类切换为当月数据
     */
    void onMonthlyPointSelected(MonthlyEntryData entryData) {
        if (entryData == null || entryData.getMonth() == null || mDisplayDailyChart.get()) {
            return;
        }
        Month month = entryData.getMonth();
        if (isSameMonth(mSelectedMonth, month)) {
            return;
        }
        mSelectedMonth = new Month(month.getYear(), month.getMonth());
        displaySelectedMonthAmountTotal();
        loadCategoryForSelectedMonth();
    }

    /**
     * 取消月选中：下方恢复当年数据
     */
    void onMonthlyPointCleared() {
        if (mSelectedMonth == null) {
            return;
        }
        clearSelectedMonth(true);
    }

    /** 上一天 / 上一月 */
    public void onPrevDayClick() {
        if (mDisplayDailyChart.get()) {
            navigatePrevDay();
        } else {
            navigatePrevMonth();
        }
    }

    /** 下一天 / 下一月 */
    public void onNextDayClick() {
        if (mDisplayDailyChart.get()) {
            navigateNextDay();
        } else {
            navigateNextMonth();
        }
    }

    private void navigatePrevDay() {
        if (!mDisplayDailyChart.get()) {
            return;
        }
        DailyData baseline = mSelectedDailyData;
        if (baseline == null) {
            List<DailyData> list = getCurrentDailyList();
            if (list == null || list.isEmpty()) {
                return;
            }
            baseline = list.get(0);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, baseline.getYear());
        calendar.set(Calendar.MONTH, baseline.getMonth() - 1);
        calendar.set(Calendar.DAY_OF_MONTH, baseline.getDayOfMonth());
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        selectOrSwitchToDay(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));
    }

    private void navigateNextDay() {
        if (!mDisplayDailyChart.get()) {
            return;
        }
        DailyData baseline = mSelectedDailyData;
        if (baseline == null) {
            List<DailyData> list = getCurrentDailyList();
            if (list == null || list.isEmpty()) {
                return;
            }
            baseline = list.get(0);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, baseline.getYear());
        calendar.set(Calendar.MONTH, baseline.getMonth() - 1);
        calendar.set(Calendar.DAY_OF_MONTH, baseline.getDayOfMonth());
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        selectOrSwitchToDay(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));
    }

    private void navigatePrevMonth() {
        Month baseline = mSelectedMonth;
        if (baseline == null) {
            List<MonthlyData> list = getCurrentMonthlyList();
            if (list == null || list.isEmpty() || list.get(0).getMonth() == null) {
                baseline = new Month(getChartYear(), 1);
            } else {
                baseline = list.get(0).getMonth();
            }
        }
        Month target = baseline.pre();
        selectOrSwitchToMonth(target.getYear(), target.getMonth());
    }

    private void navigateNextMonth() {
        Month baseline = mSelectedMonth;
        if (baseline == null) {
            List<MonthlyData> list = getCurrentMonthlyList();
            if (list == null || list.isEmpty() || list.get(0).getMonth() == null) {
                baseline = new Month(getChartYear(), 1);
            } else {
                baseline = list.get(0).getMonth();
            }
        }
        Month target = baseline.next();
        selectOrSwitchToMonth(target.getYear(), target.getMonth());
    }

    /** 月账单 每日数据 点击 */
    void onDailyMarkerViewClick(Activity activity, DailyData dailyData) {
        Pair<Long, Long> dayTimeRange = DateUtils.dayDateRange(
                dailyData.getYear(), dailyData.getMonth(), dailyData.getDayOfMonth());
        RecordQuery query = new RecordQuery.Builder()
                .setType(mDisplayExpenseChart.get() ? RecordQuery.TYPE_EXPENSE : RecordQuery.TYPE_INCOME)
                .setStartTime(dayTimeRange.first)
                .setEndTime(dayTimeRange.second)
                .build();
        RecordsActivity.open(activity, query);
    }

    /** 年账单 每月数据 点击 */
    void onMonthlyMarkerViewClick(Activity activity, MonthlyEntryData monthlyData) {
        Pair<Long, Long> dayTimeRange = DateUtils.monthDateRange(
                monthlyData.getMonth().getYear(), monthlyData.getMonth().getMonth());
        RecordQuery query = new RecordQuery.Builder()
                .setType(mDisplayExpenseChart.get() ? RecordQuery.TYPE_EXPENSE : RecordQuery.TYPE_INCOME)
                .setStartTime(dayTimeRange.first)
                .setEndTime(dayTimeRange.second)
                .build();
        RecordsActivity.open(activity, query);
    }

    /** 格式化分类数据金额 */
    public String formatCategoryDataAmount(CategoryData data) {
        return "¥" + TallyUtils.formatDisplayMoney(data.getAmount());
    }

    /** 显示为 支出账单 点击 */
    public void onSelectAsExpenseChartClick() {
        if (mDisplayExpenseChart.get()) {
            return;
        }
        mDisplayExpenseChart.set(true);
        refreshData();
    }

    /** 显示为 收入账单 点击 */
    public void onSelectAsIncomeChartClick() {
        if (!mDisplayExpenseChart.get()) {
            return;
        }
        mDisplayExpenseChart.set(false);
        refreshData();
    }

    /** 切换 日账单、年账单点击 */
    public void onSwitchChartModelClick() {
        if (mDisplayDailyChart.get()) {
            // 当前显示为 日账单，切换为年账单
            clearSelectedDay(false);
            clearSelectedMonth(false);
            onSelectAsYearChartClick();
        } else {
            // 当前显示为 年账单，切换为日账单
            clearSelectedMonth(false);
            clearSelectedDay(false);
            onSelectAsDailyChartClick();
        }
    }

    /** 分类数据 ITEM 点击 */
    public void onCategoryDataItemClick(CategoryData categoryData) {
        mViewReliedTask.setValue(activity -> {
            RecordsActivity.open(activity, new RecordQuery.Builder()
                    .setStartTime(categoryData.getStartDate())
                    .setEndTime(categoryData.getEndDate())
                    .setType(categoryData.getType())
                    .setCategoryUniqueNameArray(new String[]{categoryData.getCategoryUniqueName()})
                    .build());
        });
    }

    /** 显示为 年账单 点击 */
    private void onSelectAsYearChartClick() {
        mDisplayDailyChart.set(false);
        refreshData();
    }

    /** 显示为 日账单 点击 */
    private void onSelectAsDailyChartClick() {
        mDisplayDailyChart.set(true);
        refreshData();
    }

    private void init() {
        mRepository.queryFirstRecordTime(new Callback<Long, IError>() {
            @Override
            public void success(Long firstRecordTime) {
                Month firstMonth = new Month();
                Month currentMonth = new Month();

                Calendar calendar = Calendar.getInstance();
                currentMonth.setYear(calendar.get(Calendar.YEAR));
                currentMonth.setMonth(calendar.get(Calendar.MONTH) + 1);

                calendar.setTimeInMillis(firstRecordTime);
                firstMonth.setYear(calendar.get(Calendar.YEAR));
                firstMonth.setMonth(calendar.get(Calendar.MONTH) + 1);

                for (int y = firstMonth.getYear(); y <= currentMonth.getYear(); y++) {

                    for (int m = 1; m <= 12; m++) {
                        Month month = new Month();
                        month.setYear(y);
                        month.setMonth(m);
                        mSelectableMonthList.add(month);

                        if (y == currentMonth.getYear() && m == currentMonth.getMonth()) {
                            break;
                        }
                    }
                }
            }

            @Override
            public void failure(IError iError) {
                LogUtils.LOGE(TAG, "query first record time error:" + iError.toString());
            }
        });
    }

    private void clearData() {
        mDailyExpenseList.clear();
        mDailyIncomeList.clear();
        mMonthlyExpenseList.clear();
        mMonthlyIncomeList.clear();
        mCategoryDailyExpenseList.clear();
        mCategoryDailyIncomeList.clear();
        mCategoryYearlyExpenseList.clear();
        mCategoryYearlyIncomeList.clear();

        mObservableMonthlyDataList.setValue(null);
        mObservableDailyExpenseList.setValue(null);
        mObservableDailyIncomeList.setValue(null);
        mObservableCategoryExpenseDataList.setValue(null);
        mObservableCategoryIncomeDataList.setValue(null);
    }

    private void refreshData() {
        boolean isDisplayAsDaily = mDisplayDailyChart.get();
        boolean isDisplayExpense = mDisplayExpenseChart.get();

        if (isDisplayAsDaily) {
            // 查询日账单数据
            queryDailyData(v -> {
                // 显示日账单图标
                boolean hasExpense = ArrayUtils.contains(mDailyExpenseList, item -> item.getAmount() > 0);
                boolean hasIncome = ArrayUtils.contains(mDailyIncomeList, item -> item.getAmount() > 0);
                // 没有消费记录，清除集合
                if (!hasExpense) {
                    mDailyExpenseList.clear();
                }
                // 没有收入记录，清除集合
                if (!hasIncome) {
                    mDailyIncomeList.clear();
                }
                if (isDisplayExpense) {
                    mObservableDailyExpenseList.postValue(new ArrayList<>(mDailyExpenseList));
                } else {
                    mObservableDailyIncomeList.postValue(new ArrayList<>(mDailyIncomeList));
                }

                // 显示账单金额
                if (mSelectedDailyData != null) {
                    // 用日期匹配，避免依赖已清空列表里的对象引用
                    int year = mSelectedDailyData.getYear();
                    int month = mSelectedDailyData.getMonth();
                    int day = mSelectedDailyData.getDayOfMonth();
                    // 优先在当前 Tab 列表找；找不到则从另一侧列表补齐日期信息
                    DailyData matched = findDailyData(getCurrentDailyList(), year, month, day);
                    if (matched == null) {
                        matched = findDailyData(mDailyExpenseList, year, month, day);
                    }
                    if (matched == null) {
                        matched = findDailyData(mDailyIncomeList, year, month, day);
                    }
                    if (matched == null && (!mDailyExpenseList.isEmpty() || !mDailyIncomeList.isEmpty())) {
                        // 当前月仍有日数据（例如某一侧被清空），构造占位日以保持选中
                        matched = new DailyData();
                        matched.setYear(year);
                        matched.setMonth(month);
                        matched.setDayOfMonth(day);
                    }
                    if (matched != null
                            && year == mStartDate.get(Calendar.YEAR)
                            && month == mStartDate.get(Calendar.MONTH) + 1) {
                        mSelectedDailyData = matched;
                        displaySelectedDayAmountTotal();
                        loadCategoryForSelectedDay();
                        List<DailyData> chartList = getCurrentDailyList();
                        int index = findDailyIndex(chartList, matched);
                        // 当前 Tab 无柱数据时不强制高亮，避免越界闪退
                        mObservableBarHighlightIndex.postValue(index);
                    } else {
                        clearSelectedDay(true);
                        publishMonthCategoryData();
                        displayDailyExpenseAmountTotal();
                        displayDailyIncomeAmountTotal();
                    }
                } else {
                    publishMonthCategoryData();
                    displayDailyExpenseAmountTotal();
                    displayDailyIncomeAmountTotal();
                }
            });
        } else {
            queryYearlyData(v -> {
                // 年账单折线图：按 Tab 只展示支出或收入一侧
                MonthlyDataList monthlyDataList = new MonthlyDataList();
                if (isDisplayExpense) {
                    monthlyDataList.setExpenseList(new ArrayList<>(mMonthlyExpenseList));
                } else {
                    monthlyDataList.setIncomeList(new ArrayList<>(mMonthlyIncomeList));
                }
                mObservableMonthlyDataList.postValue(monthlyDataList);

                if (mSelectedMonth != null) {
                    int year = mSelectedMonth.getYear();
                    int month = mSelectedMonth.getMonth();
                    if (year == getChartYear()) {
                        displaySelectedMonthAmountTotal();
                        loadCategoryForSelectedMonth();
                        int index = findMonthlyIndex(getCurrentMonthlyList(), year, month);
                        mObservableBarHighlightIndex.postValue(index);
                    } else {
                        clearSelectedMonth(true);
                    }
                } else {
                    // 年账单 分类饼图
                    publishYearCategoryData();
                    displayMonthlyExpenseAmountTotal();
                    displayMonthlyIncomeAmountTotal();
                }
            });
        }
    }

    /***
     * 查询日账单数据
     * @param callback 回调
     */
    private void queryDailyData(SimpleCallback<Void> callback) {

        long startTime = mStartDate.getTimeInMillis();
        long endTime = mEndDate.getTimeInMillis();

        int queryModuleCount = 4;
        AtomicInteger queryCount = new AtomicInteger(0);

        // 读取日支出数据
        if (!mDailyExpenseList.isEmpty()) {
            if (queryCount.incrementAndGet() == queryModuleCount) {
                callback.success(null);
            }
        } else {
            mRepository.queryDailyExpense(startTime, endTime, new Callback<List<DailyData>, IError>() {
                @Override
                public void success(List<DailyData> dailyDataList) {
                    dailyDataList = completeEmptyDailyData(startTime, endTime, dailyDataList);
                    mDailyExpenseList.clear();
                    mDailyExpenseList.addAll(dailyDataList);

                    if (queryCount.incrementAndGet() == queryModuleCount) {
                        callback.success(null);
                    }
                }

                @Override
                public void failure(IError iError) {
                    mDailyExpenseList.clear();
                    if (queryCount.incrementAndGet() == queryModuleCount) {
                        callback.success(null);
                    }
                }
            });
        }

        // 查询日收入数据
        if (!mDailyIncomeList.isEmpty()) {
            if (queryCount.incrementAndGet() == queryModuleCount) {
                callback.success(null);
            }
        } else {
            mRepository.queryDailyInCome(startTime, endTime, new Callback<List<DailyData>, IError>() {
                @Override
                public void success(List<DailyData> dailyDataList) {
                    dailyDataList = completeEmptyDailyData(startTime, endTime, dailyDataList);
                    mDailyIncomeList.clear();
                    mDailyIncomeList.addAll(dailyDataList);
                    if (queryCount.incrementAndGet() == queryModuleCount) {
                        callback.success(null);
                    }
                }

                @Override
                public void failure(IError iError) {
                    mDailyIncomeList.clear();
                    if (queryCount.incrementAndGet() == queryModuleCount) {
                        callback.success(null);
                    }
                }
            });
        }

        // 查询日账单支出分类数据
        if (!mCategoryDailyExpenseList.isEmpty()) {
            if (queryCount.incrementAndGet() == queryModuleCount) {
                callback.success(null);
            }
        } else {
            mRepository.queryCategoryExpense(startTime, endTime, new Callback<List<CategoryData>, IError>() {
                @Override
                public void success(List<CategoryData> categoryData) {
                    mCategoryDailyExpenseList.clear();
                    if (categoryData != null) {
                        mCategoryDailyExpenseList.addAll(categoryData);
                    }
                    if (queryCount.incrementAndGet() == queryModuleCount) {
                        callback.success(null);
                    }
                }

                @Override
                public void failure(IError iError) {
                    mCategoryDailyExpenseList.clear();
                    if (queryCount.incrementAndGet() == queryModuleCount) {
                        callback.success(null);
                    }
                }
            });
        }

        // 查询日账单收入分类数据
        if (!mCategoryDailyIncomeList.isEmpty()) {
            if (queryCount.incrementAndGet() == queryModuleCount) {
                callback.success(null);
            }
        } else {
            mRepository.queryCategoryIncome(startTime, endTime, new Callback<List<CategoryData>, IError>() {
                @Override
                public void success(List<CategoryData> categoryData) {
                    mCategoryDailyIncomeList.clear();
                    if (categoryData != null) {
                        mCategoryDailyIncomeList.addAll(categoryData);
                    }
                    if (queryCount.incrementAndGet() == queryModuleCount) {
                        callback.success(null);
                    }
                }

                @Override
                public void failure(IError iError) {
                    mCategoryDailyIncomeList.clear();
                    if (queryCount.incrementAndGet() == queryModuleCount) {
                        callback.success(null);
                    }
                }
            });
        }
    }

    /** 查询年账单数据 */
    private void queryYearlyData(SimpleCallback<Void> callback) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mStartDate.getTimeInMillis() + (mEndDate.getTimeInMillis() - mStartDate.getTimeInMillis()) / 2);
        Pair<Long, Long> yearDateRange = DateUtils.yearDateRange(calendar.get(Calendar.YEAR));
        long startTime = yearDateRange.first;
        long endTime = yearDateRange.second;

        int queryModuleCount = 4;
        AtomicInteger queryCount = new AtomicInteger(0);

        // 查询支出年账单
        if (!mMonthlyExpenseList.isEmpty()) {
            if (queryCount.incrementAndGet() == queryModuleCount) {
                callback.success(null);
            }
        } else {
            mRepository.queryMonthlyExpense(startTime, endTime, new Callback<List<MonthlyData>, IError>() {
                @Override
                public void success(List<MonthlyData> list) {
                    // 补全空数据
                    list = completeEmptyMonthlyData(startTime, endTime, list);
                    mMonthlyExpenseList.clear();
                    mMonthlyExpenseList.addAll(list);
                    if (queryCount.incrementAndGet() == queryModuleCount) {
                        callback.success(null);
                    }
                }

                @Override
                public void failure(IError iError) {
                    if (queryCount.incrementAndGet() == queryModuleCount) {
                        callback.success(null);
                    }
                }
            });
        }

        // 查询收入年账单数据
        if (!mMonthlyIncomeList.isEmpty()) {
            if (queryCount.incrementAndGet() == queryModuleCount) {
                callback.success(null);
            }
        } else {
            mRepository.queryMonthlyIncome(startTime, endTime, new Callback<List<MonthlyData>, IError>() {
                @Override
                public void success(List<MonthlyData> list) {
                    // 补全空数据
                    list = completeEmptyMonthlyData(startTime, endTime, list);
                    mMonthlyIncomeList.clear();
                    mMonthlyIncomeList.addAll(list);
                    if (queryCount.incrementAndGet() == queryModuleCount) {
                        callback.success(null);
                    }
                }

                @Override
                public void failure(IError iError) {
                    if (queryCount.incrementAndGet() == queryModuleCount) {
                        callback.success(null);
                    }
                }
            });
        }

        // 查询年账单支出分类数据
        if (!mCategoryYearlyExpenseList.isEmpty()) {
            if (queryCount.incrementAndGet() == queryModuleCount) {
                callback.success(null);
            }
        } else {
            mRepository.queryCategoryExpense(startTime, endTime, new Callback<List<CategoryData>, IError>() {
                @Override
                public void success(List<CategoryData> categoryData) {
                    mCategoryYearlyExpenseList.clear();
                    if (categoryData != null) {
                        mCategoryYearlyExpenseList.addAll(categoryData);
                    }
                    if (queryCount.incrementAndGet() == queryModuleCount) {
                        callback.success(null);
                    }
                }

                @Override
                public void failure(IError iError) {
                    mCategoryYearlyExpenseList.clear();
                    if (queryCount.incrementAndGet() == queryModuleCount) {
                        callback.success(null);
                    }
                }
            });
        }

        // 查询年账单收入分类数据
        if (!mCategoryYearlyIncomeList.isEmpty()) {
            if (queryCount.incrementAndGet() == queryModuleCount) {
                callback.success(null);
            }
        } else {
            mRepository.queryCategoryIncome(startTime, endTime, new Callback<List<CategoryData>, IError>() {
                @Override
                public void success(List<CategoryData> categoryData) {
                    mCategoryYearlyIncomeList.clear();
                    if (categoryData != null) {
                        mCategoryYearlyIncomeList.addAll(categoryData);
                    }
                    if (queryCount.incrementAndGet() == queryModuleCount) {
                        callback.success(null);
                    }
                }

                @Override
                public void failure(IError iError) {
                    mCategoryYearlyIncomeList.clear();
                    if (queryCount.incrementAndGet() == queryModuleCount) {
                        callback.success(null);
                    }
                }
            });
        }
    }

    /** 发布当月分类数据到饼图/列表 */
    private void publishMonthCategoryData() {
        if (mDisplayExpenseChart.get()) {
            mObservableCategoryExpenseDataList.postValue(new ArrayList<>(mCategoryDailyExpenseList));
        } else {
            mObservableCategoryIncomeDataList.postValue(new ArrayList<>(mCategoryDailyIncomeList));
        }
    }

    /** 发布当年分类数据到饼图/列表 */
    private void publishYearCategoryData() {
        if (mDisplayExpenseChart.get()) {
            mObservableCategoryExpenseDataList.postValue(new ArrayList<>(mCategoryYearlyExpenseList));
        } else {
            mObservableCategoryIncomeDataList.postValue(new ArrayList<>(mCategoryYearlyIncomeList));
        }
    }

    private void clearSelectedDay(boolean publishMonthData) {
        mSelectedDailyData = null;
        // 查询回调可能在子线程，统一 post 避免 setValue 线程异常
        mObservableBarHighlightIndex.postValue(-1);
        if (publishMonthData && mDisplayDailyChart.get()) {
            publishMonthCategoryData();
            displayDailyExpenseAmountTotal();
            displayDailyIncomeAmountTotal();
        }
    }

    private void clearSelectedMonth(boolean publishYearData) {
        mSelectedMonth = null;
        mObservableBarHighlightIndex.postValue(-1);
        if (publishYearData && !mDisplayDailyChart.get()) {
            publishYearCategoryData();
            displayMonthlyExpenseAmountTotal();
            displayMonthlyIncomeAmountTotal();
        }
    }

    private List<DailyData> getCurrentDailyList() {
        return mDisplayExpenseChart.get() ? mDailyExpenseList : mDailyIncomeList;
    }

    private List<MonthlyData> getCurrentMonthlyList() {
        return mDisplayExpenseChart.get() ? mMonthlyExpenseList : mMonthlyIncomeList;
    }

    private int getChartYear() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mStartDate.getTimeInMillis()
                + (mEndDate.getTimeInMillis() - mStartDate.getTimeInMillis()) / 2);
        return calendar.get(Calendar.YEAR);
    }

    private int findDailyIndex(List<DailyData> list, DailyData target) {
        if (list == null || target == null) {
            return -1;
        }
        for (int i = 0; i < list.size(); i++) {
            DailyData item = list.get(i);
            if (item != null
                    && item.getYear() == target.getYear()
                    && item.getMonth() == target.getMonth()
                    && item.getDayOfMonth() == target.getDayOfMonth()) {
                return i;
            }
        }
        return -1;
    }

    private int findMonthlyIndex(List<MonthlyData> list, int year, int month) {
        if (list == null) {
            return -1;
        }
        for (int i = 0; i < list.size(); i++) {
            MonthlyData item = list.get(i);
            if (item != null && item.getMonth() != null
                    && item.getMonth().getYear() == year
                    && item.getMonth().getMonth() == month) {
                return i;
            }
        }
        return -1;
    }

    private DailyData findDailyData(List<DailyData> list, int year, int month, int dayOfMonth) {
        if (list == null) {
            return null;
        }
        for (DailyData item : list) {
            if (item != null
                    && item.getYear() == year
                    && item.getMonth() == month
                    && item.getDayOfMonth() == dayOfMonth) {
                return item;
            }
        }
        return null;
    }

    private MonthlyData findMonthlyData(List<MonthlyData> list, int year, int month) {
        if (list == null) {
            return null;
        }
        for (MonthlyData item : list) {
            if (item != null && item.getMonth() != null
                    && item.getMonth().getYear() == year
                    && item.getMonth().getMonth() == month) {
                return item;
            }
        }
        return null;
    }

    private boolean isSameDailyData(DailyData a, DailyData b) {
        if (a == null || b == null) {
            return false;
        }
        return a.getYear() == b.getYear()
                && a.getMonth() == b.getMonth()
                && a.getDayOfMonth() == b.getDayOfMonth();
    }

    private boolean isSameMonth(Month a, Month b) {
        if (a == null || b == null) {
            return false;
        }
        return a.getYear() == b.getYear() && a.getMonth() == b.getMonth();
    }

    private void selectOrSwitchToDay(int year, int month, int dayOfMonth) {
        int currentYear = mStartDate.get(Calendar.YEAR);
        int currentMonth = mStartDate.get(Calendar.MONTH) + 1;
        if (year == currentYear && month == currentMonth) {
            if (mSelectedDailyData != null
                    && mSelectedDailyData.getYear() == year
                    && mSelectedDailyData.getMonth() == month
                    && mSelectedDailyData.getDayOfMonth() == dayOfMonth) {
                return;
            }
            DailyData day = findDailyData(getCurrentDailyList(), year, month, dayOfMonth);
            if (day != null) {
                onDailyBarSelected(day);
                int index = findDailyIndex(getCurrentDailyList(), day);
                mObservableBarHighlightIndex.setValue(index);
            }
            return;
        }
        Pair<Long, Long> monthDateRange = DateUtils.monthDateRange(year, month);
        mStartDate.setTimeInMillis(monthDateRange.first);
        mEndDate.setTimeInMillis(monthDateRange.second);
        mSelectedDailyData = new DailyData();
        mSelectedDailyData.setYear(year);
        mSelectedDailyData.setMonth(month);
        mSelectedDailyData.setDayOfMonth(dayOfMonth);
        clearData();
        refreshData();
    }

    private void selectOrSwitchToMonth(int year, int month) {
        if (year == getChartYear()) {
            if (isSameMonth(mSelectedMonth, new Month(year, month))) {
                return;
            }
            MonthlyData data = findMonthlyData(getCurrentMonthlyList(), year, month);
            Month target = data != null && data.getMonth() != null
                    ? data.getMonth() : new Month(year, month);
            MonthlyEntryData entryData = new MonthlyEntryData().setMonth(target);
            if (data != null) {
                if (mDisplayExpenseChart.get()) {
                    entryData.setExpenseAmount(data.getAmount());
                } else {
                    entryData.setIncomeAmount(data.getAmount());
                }
            }
            onMonthlyPointSelected(entryData);
            int index = findMonthlyIndex(getCurrentMonthlyList(), year, month);
            mObservableBarHighlightIndex.setValue(index);
            return;
        }
        Pair<Long, Long> yearDateRange = DateUtils.yearDateRange(year);
        mStartDate.setTimeInMillis(yearDateRange.first);
        mEndDate.setTimeInMillis(yearDateRange.second);
        mSelectedMonth = new Month(year, month);
        clearData();
        refreshData();
    }

    private void loadCategoryForSelectedDay() {
        if (mSelectedDailyData == null) {
            return;
        }
        Pair<Long, Long> dayRange = DateUtils.dayDateRange(
                mSelectedDailyData.getYear(),
                mSelectedDailyData.getMonth(),
                mSelectedDailyData.getDayOfMonth());
        boolean isExpense = mDisplayExpenseChart.get();
        if (isExpense) {
            mRepository.queryCategoryExpense(dayRange.first, dayRange.second, new Callback<List<CategoryData>, IError>() {
                @Override
                public void success(List<CategoryData> categoryData) {
                    mObservableCategoryExpenseDataList.postValue(
                            categoryData == null ? new ArrayList<>() : categoryData);
                }

                @Override
                public void failure(IError iError) {
                    mObservableCategoryExpenseDataList.postValue(new ArrayList<>());
                }
            });
        } else {
            mRepository.queryCategoryIncome(dayRange.first, dayRange.second, new Callback<List<CategoryData>, IError>() {
                @Override
                public void success(List<CategoryData> categoryData) {
                    mObservableCategoryIncomeDataList.postValue(
                            categoryData == null ? new ArrayList<>() : categoryData);
                }

                @Override
                public void failure(IError iError) {
                    mObservableCategoryIncomeDataList.postValue(new ArrayList<>());
                }
            });
        }
    }

    private void loadCategoryForSelectedMonth() {
        if (mSelectedMonth == null) {
            return;
        }
        Pair<Long, Long> monthRange = DateUtils.monthDateRange(
                mSelectedMonth.getYear(), mSelectedMonth.getMonth());
        boolean isExpense = mDisplayExpenseChart.get();
        if (isExpense) {
            mRepository.queryCategoryExpense(monthRange.first, monthRange.second, new Callback<List<CategoryData>, IError>() {
                @Override
                public void success(List<CategoryData> categoryData) {
                    mObservableCategoryExpenseDataList.postValue(
                            categoryData == null ? new ArrayList<>() : categoryData);
                }

                @Override
                public void failure(IError iError) {
                    mObservableCategoryExpenseDataList.postValue(new ArrayList<>());
                }
            });
        } else {
            mRepository.queryCategoryIncome(monthRange.first, monthRange.second, new Callback<List<CategoryData>, IError>() {
                @Override
                public void success(List<CategoryData> categoryData) {
                    mObservableCategoryIncomeDataList.postValue(
                            categoryData == null ? new ArrayList<>() : categoryData);
                }

                @Override
                public void failure(IError iError) {
                    mObservableCategoryIncomeDataList.postValue(new ArrayList<>());
                }
            });
        }
    }

    /** 显示选中日期的支出/收入汇总 */
    private void displaySelectedDayAmountTotal() {
        if (mSelectedDailyData == null) {
            return;
        }
        int year = mSelectedDailyData.getYear();
        int month = mSelectedDailyData.getMonth();
        int day = mSelectedDailyData.getDayOfMonth();
        mCurrentDateText.set(ResUtils.getString(getApplication(),
                R.string.tally_bill_of_day_format, year, month, day));

        DailyData expenseDay = findDailyData(mDailyExpenseList, year, month, day);
        DailyData incomeDay = findDailyData(mDailyIncomeList, year, month, day);
        long expenseCount = expenseDay == null ? 0 : expenseDay.getCount();
        double expenseAmount = expenseDay == null ? 0 : expenseDay.getAmount();
        long incomeCount = incomeDay == null ? 0 : incomeDay.getCount();
        double incomeAmount = incomeDay == null ? 0 : incomeDay.getAmount();

        String expenseMoney = "¥" + TallyUtils.formatDisplayMoney(expenseAmount);
        String incomeMoney = "¥" + TallyUtils.formatDisplayMoney(incomeAmount);
        mExpenseTotalAmountText.set(ResUtils.getString(getApplication(),
                R.string.tally_expense_data_format, expenseCount, expenseMoney));
        mIncomeTotalAmountText.set(ResUtils.getString(getApplication(),
                R.string.tally_income_data_format, incomeCount, incomeMoney));
    }

    /** 显示选中月份的支出/收入汇总 */
    private void displaySelectedMonthAmountTotal() {
        if (mSelectedMonth == null) {
            return;
        }
        int year = mSelectedMonth.getYear();
        int month = mSelectedMonth.getMonth();
        mCurrentDateText.set(ResUtils.getString(getApplication(),
                R.string.tally_bill_of_month_format, year, month));

        MonthlyData expenseMonth = findMonthlyData(mMonthlyExpenseList, year, month);
        MonthlyData incomeMonth = findMonthlyData(mMonthlyIncomeList, year, month);
        long expenseCount = expenseMonth == null ? 0 : expenseMonth.getCount();
        double expenseAmount = expenseMonth == null ? 0 : expenseMonth.getAmount();
        long incomeCount = incomeMonth == null ? 0 : incomeMonth.getCount();
        double incomeAmount = incomeMonth == null ? 0 : incomeMonth.getAmount();

        String expenseMoney = "¥" + TallyUtils.formatDisplayMoney(expenseAmount);
        String incomeMoney = "¥" + TallyUtils.formatDisplayMoney(incomeAmount);
        mExpenseTotalAmountText.set(ResUtils.getString(getApplication(),
                R.string.tally_expense_data_format, expenseCount, expenseMoney));
        mIncomeTotalAmountText.set(ResUtils.getString(getApplication(),
                R.string.tally_income_data_format, incomeCount, incomeMoney));
    }

    /** 显示日账单支出总额 */
    private void displayDailyExpenseAmountTotal() {
        final WrappedLong recordCount = new WrappedLong(0);
        final WrappedObject<Double> total = new WrappedObject<>(0D);
        ArrayUtils.forEach(mDailyExpenseList, (count, index, item) -> {
            total.set(total.get() + item.getAmount());
            recordCount.set(recordCount.get() + item.getCount());
        });

        int year = mStartDate.get(Calendar.YEAR);
        int month = mStartDate.get(Calendar.MONTH) + 1;
        String money = "¥" + TallyUtils.formatDisplayMoney(total.get());
        mCurrentDateText.set(ResUtils.getString(getApplication(), R.string.tally_bill_of_month_format, year, month));
        mExpenseTotalAmountText.set(ResUtils.getString(getApplication(), R.string.tally_expense_data_format, recordCount.get(), money));
    }

    /** 显示日账单收入总额 */
    private void displayDailyIncomeAmountTotal() {
        final WrappedLong recordCount = new WrappedLong(0);
        final WrappedObject<Double> total = new WrappedObject<>(0D);
        ArrayUtils.forEach(mDailyIncomeList, (count, index, item) -> {
            total.set(total.get() + item.getAmount());
            recordCount.set(recordCount.get() + item.getCount());
        });

        int year = mStartDate.get(Calendar.YEAR);
        int month = mStartDate.get(Calendar.MONTH) + 1;
        String money = "¥" + TallyUtils.formatDisplayMoney(total.get());
        mCurrentDateText.set(ResUtils.getString(getApplication(), R.string.tally_bill_of_month_format, year, month));
        mIncomeTotalAmountText.set(ResUtils.getString(getApplication(), R.string.tally_income_data_format, recordCount.get(), money));
    }

    /** 显示月账单支出总额 */
    private void displayMonthlyExpenseAmountTotal() {
        final WrappedLong recordCount = new WrappedLong(0);
        final WrappedObject<Double> total = new WrappedObject<>(0D);
        ArrayUtils.forEach(mMonthlyExpenseList, (count, index, item) -> {
            total.set(total.get() + item.getAmount());
            recordCount.set(recordCount.get() + item.getCount());
        });

        int year = getChartYear();
        String money = "¥" + TallyUtils.formatDisplayMoney(total.get());
        mCurrentDateText.set(ResUtils.getString(getApplication(), R.string.tally_bill_of_year_format, year));
        mExpenseTotalAmountText.set(ResUtils.getString(getApplication(), R.string.tally_expense_data_format, recordCount.get(), money));
    }

    /** 显示月账单收入总额 */
    private void displayMonthlyIncomeAmountTotal() {
        final WrappedLong recordCount = new WrappedLong(0);
        final WrappedObject<Double> total = new WrappedObject<>(0D);
        ArrayUtils.forEach(mMonthlyIncomeList, (count, index, item) -> {
            total.set(total.get() + item.getAmount());
            recordCount.set(recordCount.get() + item.getCount());
        });

        int year = getChartYear();
        String money = "¥" + TallyUtils.formatDisplayMoney(total.get());
        mCurrentDateText.set(ResUtils.getString(getApplication(), R.string.tally_bill_of_year_format, year));
        mIncomeTotalAmountText.set(ResUtils.getString(getApplication(), R.string.tally_income_data_format, recordCount.get(), money));
    }

    /**
     * 补全起止日期内日记录数据，若有自然日没有记录，以空数据补全
     *
     * @param startDate     开始日期
     * @param endDate       结束日期
     * @param dailyDataList 源数据。
     * @return 补全后的数据集合
     */
    private List<DailyData> completeEmptyDailyData(long startDate, long endDate, List<DailyData> dailyDataList) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(startDate);
        int startYear = calendar.get(Calendar.YEAR);
        int startMonth = calendar.get(Calendar.MONTH) + 1;
        int startDay = calendar.get(Calendar.DAY_OF_MONTH);

        calendar.setTimeInMillis(endDate);
        int endYear = calendar.get(Calendar.YEAR);
        int endMonth = calendar.get(Calendar.MONTH) + 1;
        int endDay = calendar.get(Calendar.DAY_OF_MONTH);

        // 计算总天数，不准确。大致天数为了给新集合设置初始值
        int totalDayCount = (int) ((endDate - startDate) / TimeUtils.DAY_MILLSECONDS + 2);
        // 最终补全未记录自然日的数据集合
        List<DailyData> result = new ArrayList<>(totalDayCount);

        // 遍历 起止日期的每个自然日，若自然日没有记录，以空数据补全
        // dayCursor 为查询到数据集合的遍历游标，每从集合中取一个数据到最终集合中，+1
        int dayCursor = 0;
        for (int year = startYear; year <= endYear; year++) {
            int monthStart = startYear == endYear ? startMonth : 1;
            int monthEnd = year == endYear ? endMonth : 12;
            for (int month = monthStart; month <= monthEnd; month++) {
                int monthMaxDayCount = TimeUtils.getDaysTotalOfMonth(year, month);
                for (int day = 1; day <= monthMaxDayCount; day++) {
                    DailyData nextDailyData = dailyDataList != null && dailyDataList.size() > dayCursor ? dailyDataList.get(dayCursor) : null;
                    if (nextDailyData != null
                            && nextDailyData.getYear() == year
                            && nextDailyData.getMonth() == month
                            && nextDailyData.getDayOfMonth() == day) {
                        result.add(nextDailyData);
                        dayCursor++;
                    } else {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month - 1);
                        calendar.set(Calendar.DAY_OF_MONTH, day);
                        DailyData dailyData = new DailyData();
                        dailyData.setTimeMillis(calendar.getTimeInMillis());
                        dailyData.setYear(year);
                        dailyData.setMonth(month);
                        dailyData.setDayOfMonth(day);
                        dailyData.setAmount(0);
                        result.add(dailyData);
                    }
                }
            }
        }

        return result;
    }

    /**
     * 补全起止日期内月记录数据，若有自然月没有记录，以空数据补全
     *
     * @param startDate       开始日期
     * @param endDate         结束日期
     * @param monthlyDataList 源数据。
     * @return 补全后的数据集合
     */
    private List<MonthlyData> completeEmptyMonthlyData(long startDate, long endDate, List<MonthlyData> monthlyDataList) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(startDate);
        int startYear = calendar.get(Calendar.YEAR);
        int startMonth = calendar.get(Calendar.MONTH) + 1;

        calendar.setTimeInMillis(endDate);
        int endYear = calendar.get(Calendar.YEAR);
        int endMonth = calendar.get(Calendar.MONTH) + 1;

        // 最终补全未记录自然月的数据集合
        List<MonthlyData> result = new ArrayList<>();

        // 遍历 起止日期的每个自然月，若自然月没有记录，以空数据补全
        // dayCursor 为查询到数据集合的遍历游标，每从集合中取一个数据到最终集合中，+1
        int monthCursor = 0;
        for (int year = startYear; year <= endYear; year++) {
            int monthStart = startYear == endYear ? startMonth : 1;
            for (int month = monthStart; month <= 12; month++) {
                MonthlyData nextMonthlyData = monthlyDataList != null && monthlyDataList.size() > monthCursor ? monthlyDataList.get(monthCursor) : null;
                if (nextMonthlyData != null
                        && nextMonthlyData.getMonth() != null
                        && nextMonthlyData.getMonth().getYear() == year
                        && nextMonthlyData.getMonth().getMonth() == month) {
                    result.add(nextMonthlyData);
                    monthCursor++;
                } else {
                    MonthlyData dailyData = new MonthlyData();
                    dailyData.setMonth(new Month(year, month));
                    dailyData.setAmount(0);
                    result.add(dailyData);
                }
            }
        }

        return result;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onCreate(LifecycleOwner owner) {
        Intent intent = ((Activity) owner).getIntent();
        int year = intent.getIntExtra(TallyChartActivity.EXTRA_YEAR, -1);
        int month = intent.getIntExtra(TallyChartActivity.EXTRA_MONTH, -1);

        // 初始化起始时间
        mStartDate = Calendar.getInstance();
        mEndDate = Calendar.getInstance();
        // 默认起始时间：当月
        int currentYear = mStartDate.get(Calendar.YEAR);
        int currentMonth = mStartDate.get(Calendar.MONTH) + 1;
        // 如果指定月份，使用指定的月份为起始时间
        if (year > 0 && month > 0) {
            currentYear = year;
            currentMonth = month;
        }
        Pair<Long, Long> currentMonthRange = DateUtils.monthDateRange(currentYear, currentMonth);

        // 默认显示时间为当前月
        mStartDate.setTimeInMillis(currentMonthRange.first);
        mEndDate.setTimeInMillis(currentMonthRange.second);

        // 初始化数据
        init();
        refreshData();
    }
}
