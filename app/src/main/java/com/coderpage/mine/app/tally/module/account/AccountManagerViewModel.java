package com.coderpage.mine.app.tally.module.account;

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.OnLifecycleEvent;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.coderpage.base.utils.ResUtils;
import com.coderpage.framework.BaseViewModel;
import com.coderpage.framework.ViewReliedTask;
import com.coderpage.mine.R;
import com.coderpage.mine.app.tally.eventbus.EventAccountAdd;
import com.coderpage.mine.app.tally.eventbus.EventAccountDelete;
import com.coderpage.mine.app.tally.eventbus.EventAccountUpdate;
import com.coderpage.mine.app.tally.persistence.model.Account;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * @author zzy. 2026-09-10 10:11
 * @since 0.6.2
 *
 * 资产详情 ViewModel
 */
public class AccountManagerViewModel extends BaseViewModel implements LifecycleObserver {

    private final AccountRepository mRepository;

    private final MutableLiveData<List<Account>> mAccountList = new MutableLiveData<>();
    private final MutableLiveData<ViewReliedTask<Activity>> mViewReliedTask = new MutableLiveData<>();

    public AccountManagerViewModel(Application application) {
        super(application);
        mRepository = new AccountRepository();
    }

    LiveData<List<Account>> getAccountList() {
        return mAccountList;
    }

    LiveData<ViewReliedTask<Activity>> getViewReliedTask() {
        return mViewReliedTask;
    }

    public void onAddAccountClick(Activity activity) {
        new AccountEditDialog(activity)
                .setListener((dialog, name, balance) -> {
                    mRepository.addAccount(name, balance, result -> {
                        if (result.isOk()) {
                            EventBus.getDefault().post(new EventAccountAdd(result.data()));
                            dialog.dismiss();
                        } else if (result.error() != null) {
                            Toast.makeText(getApplication(), result.error().msg(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .show();
    }

    public void onSetDefaultClick(Account account) {
        if (account == null || account.isDefaultAccount()) {
            return;
        }
        mRepository.setDefault(account.getId(), result -> {
            if (result.isOk()) {
                EventBus.getDefault().post(new EventAccountUpdate(account));
            } else if (result.error() != null) {
                Toast.makeText(getApplication(), result.error().msg(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onDeleteClick(Activity activity, Account account) {
        if (account == null) {
            return;
        }
        new AlertDialog.Builder(activity)
                .setTitle(R.string.dialog_title_delete_confirm)
                .setMessage(ResUtils.getString(activity, R.string.tally_account_delete_confirm_msg, account.getName()))
                .setNegativeButton(R.string.dialog_btn_cancel, null)
                .setPositiveButton(R.string.dialog_btn_delete, (dialog, which) ->
                        mRepository.deleteAccount(account.getId(), result -> {
                            if (result.isOk()) {
                                EventBus.getDefault().post(new EventAccountDelete(account.getId()));
                            } else if (result.error() != null) {
                                Toast.makeText(getApplication(), result.error().msg(), Toast.LENGTH_SHORT).show();
                            }
                        }))
                .show();
    }

    private void refreshData() {
        mRepository.queryAll(mAccountList::setValue);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onCreate(LifecycleOwner owner) {
        refreshData();
        EventBus.getDefault().register(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy(LifecycleOwner owner) {
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventAccountAdd(EventAccountAdd event) {
        refreshData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventAccountUpdate(EventAccountUpdate event) {
        refreshData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventAccountDelete(EventAccountDelete event) {
        refreshData();
    }
}
