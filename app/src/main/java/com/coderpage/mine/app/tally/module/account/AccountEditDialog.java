package com.coderpage.mine.app.tally.module.account;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.coderpage.base.utils.CommonUtils;
import com.coderpage.mine.R;
import com.coderpage.mine.dialog.AccountEditDialogBinding;

/**
 * @author zzy. 2026-09-10 10:11
 * @since 0.6.2
 *
 * 新增账户弹窗
 */
public class AccountEditDialog extends Dialog {

    private Listener mListener;
    private AccountEditDialogBinding mBinding;

    public AccountEditDialog(Activity activity) {
        super(activity, R.style.Widget_Dialog_BottomSheet);
        initView(activity);
    }

    private void initView(Activity activity) {
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(activity),
                R.layout.tally_dialog_account_edit, null, false);

        mBinding.etName.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && getWindow() != null) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        });

        mBinding.ivClose.setOnClickListener(v -> dismiss());
        mBinding.tvConfirm.setOnClickListener(v -> {
            String name = mBinding.etName.getText() == null ? "" : mBinding.etName.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(activity, R.string.tally_account_name_empty, Toast.LENGTH_SHORT).show();
                return;
            }
            String balanceStr = mBinding.etBalance.getText() == null
                    ? "" : mBinding.etBalance.getText().toString().trim();
            double balance = TextUtils.isEmpty(balanceStr) ? 0 : CommonUtils.string2float(balanceStr, 0);
            if (mListener != null) {
                mListener.onConfirm(this, name, balance);
            }
        });

        View contentView = mBinding.getRoot();
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        setContentView(contentView);
        initWindow(contentView.getMeasuredHeight());
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

    @Override
    public void show() {
        super.show();
        mBinding.etName.setFocusable(true);
        mBinding.etName.requestFocus();
    }

    public AccountEditDialog setListener(Listener listener) {
        mListener = listener;
        return this;
    }

    public interface Listener {
        void onConfirm(DialogInterface dialog, String name, double initialBalance);
    }
}
