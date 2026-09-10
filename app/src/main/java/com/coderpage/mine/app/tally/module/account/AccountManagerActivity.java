package com.coderpage.mine.app.tally.module.account;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.coderpage.base.utils.UIUtils;
import com.coderpage.mine.R;
import com.coderpage.mine.app.tally.common.router.TallyRouter;
import com.coderpage.mine.app.tally.persistence.model.Account;
import com.coderpage.mine.module.account.AccountManagerActivityBinding;
import com.coderpage.mine.module.account.ItemAccountManagerBinding;
import com.coderpage.mine.ui.BaseActivity;
import com.coderpage.mine.ui.widget.recyclerview.ItemMarginDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zzy. 2026-09-10 10:11
 * @since 0.6.2
 *
 * 资产详情页
 */
@Route(path = TallyRouter.ACCOUNT)
public class AccountManagerActivity extends BaseActivity {

    private AccountManagerActivityBinding mBinding;
    private AccountManagerViewModel mViewModel;
    private AccountListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.tally_module_account_activity_manager);
        mViewModel = ViewModelProviders.of(this).get(AccountManagerViewModel.class);
        getLifecycle().addObserver(mViewModel);
        initView();
        subscribeUi();
    }

    private void initView() {
        setToolbarAsBack(v -> onBackPressed());
        setTitle(R.string.tally_toolbar_title_account);

        RecyclerView recyclerView = mBinding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(self(), LinearLayoutManager.VERTICAL, false));
        mAdapter = new AccountListAdapter();
        recyclerView.setAdapter(mAdapter);

        int padding4 = UIUtils.dp2px(self(), 4);
        int padding16 = UIUtils.dp2px(self(), 16);
        int padding80 = UIUtils.dp2px(self(), 80);
        ItemMarginDecoration itemMarginDecoration = new ItemMarginDecoration(
                padding16, padding4, padding16, padding4);
        itemMarginDecoration.setFirstItemOffset(padding16, padding16, padding16, padding4);
        itemMarginDecoration.setLastItemOffset(padding16, padding4, padding16, padding80);
        recyclerView.addItemDecoration(itemMarginDecoration);
    }

    private void subscribeUi() {
        mBinding.setVm(mViewModel);
        mBinding.setActivity(self());
        mViewModel.getAccountList().observe(this, list -> mAdapter.setDataList(list));
        mViewModel.getViewReliedTask().observe(this, task -> {
            if (task != null) {
                task.execute(self());
            }
        });
    }

    private class AccountListAdapter extends RecyclerView.Adapter<AccountViewHolder> {

        private final List<Account> mDataList = new ArrayList<>();

        void setDataList(List<Account> list) {
            mDataList.clear();
            if (list != null) {
                mDataList.addAll(list);
            }
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return mDataList.size();
        }

        @Override
        public AccountViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new AccountViewHolder(DataBindingUtil.inflate(LayoutInflater.from(self()),
                    R.layout.tally_module_account_item_manager, parent, false));
        }

        @Override
        public void onBindViewHolder(AccountViewHolder holder, int position) {
            holder.bind(mDataList.get(position));
        }
    }

    private class AccountViewHolder extends RecyclerView.ViewHolder {

        private final ItemAccountManagerBinding mBinding;

        AccountViewHolder(ItemAccountManagerBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        void bind(Account account) {
            mBinding.setData(account);
            mBinding.setVm(mViewModel);
            mBinding.setActivity(self());
            mBinding.executePendingBindings();
        }
    }
}
