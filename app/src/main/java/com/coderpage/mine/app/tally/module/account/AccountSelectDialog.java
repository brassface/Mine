package com.coderpage.mine.app.tally.module.account;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.coderpage.mine.R;
import com.coderpage.mine.app.tally.persistence.model.Account;
import com.coderpage.mine.dialog.AccountSelectDialogBinding;
import com.coderpage.mine.dialog.ItemAccountSelectBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zzy. 2026-09-10 10:11
 * @since 0.6.2
 *
 * 选择账户弹窗
 */
public class AccountSelectDialog extends Dialog {

    private long mSelectedAccountId;
    private Listener mListener;
    private AccountSelectDialogBinding mBinding;
    private final List<Account> mAccountList = new ArrayList<>();
    private AccountAdapter mAdapter;

    public AccountSelectDialog(Activity activity) {
        super(activity, R.style.Widget_Dialog_BottomSheet);
        initView(activity);
    }

    private void initView(Activity activity) {
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(activity),
                R.layout.tally_dialog_account_select, null, false);
        mAdapter = new AccountAdapter();
        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        mBinding.recyclerView.setAdapter(mAdapter);
        mBinding.ivClose.setOnClickListener(v -> dismiss());
        mBinding.tvManage.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onManageClick(this);
            }
        });

        View contentView = mBinding.getRoot();
        int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;
        int maxHeight = (int) (activity.getResources().getDisplayMetrics().heightPixels * 0.6f);
        contentView.measure(
                View.MeasureSpec.makeMeasureSpec(screenWidth, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(maxHeight, View.MeasureSpec.AT_MOST));
        setContentView(contentView);
        initWindow(Math.min(Math.max(contentView.getMeasuredHeight(), maxHeight / 2), maxHeight));
    }

    private void initWindow(int height) {
        Window window = getWindow();
        if (window == null) {
            return;
        }
        window.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.width = window.getWindowManager().getDefaultDisplay().getWidth();
        attributes.height = height;
        window.setAttributes(attributes);
    }

    public AccountSelectDialog setSelectedAccountId(long accountId) {
        mSelectedAccountId = accountId;
        return this;
    }

    public AccountSelectDialog setAccountList(List<Account> accountList) {
        mAccountList.clear();
        if (accountList != null) {
            mAccountList.addAll(accountList);
        }
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        return this;
    }

    public AccountSelectDialog setListener(Listener listener) {
        mListener = listener;
        return this;
    }

    public interface Listener {
        void onAccountSelected(DialogInterface dialog, Account account);

        void onManageClick(DialogInterface dialog);
    }

    private class AccountAdapter extends RecyclerView.Adapter<AccountViewHolder> {

        @Override
        public AccountViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new AccountViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                    R.layout.tally_dialog_account_select_item, parent, false));
        }

        @Override
        public void onBindViewHolder(AccountViewHolder holder, int position) {
            holder.bind(mAccountList.get(position));
        }

        @Override
        public int getItemCount() {
            return mAccountList.size();
        }
    }

    private class AccountViewHolder extends RecyclerView.ViewHolder {

        private final ItemAccountSelectBinding mBinding;

        AccountViewHolder(ItemAccountSelectBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        void bind(Account account) {
            mBinding.setData(account);
            mBinding.setSelected(account.getId() == mSelectedAccountId);
            mBinding.getRoot().setOnClickListener(v -> {
                if (mListener != null) {
                    mListener.onAccountSelected(AccountSelectDialog.this, account);
                }
            });
            mBinding.executePendingBindings();
        }
    }
}
