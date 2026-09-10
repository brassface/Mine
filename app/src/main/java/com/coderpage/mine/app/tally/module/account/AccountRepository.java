package com.coderpage.mine.app.tally.module.account;

import com.coderpage.base.common.IError;
import com.coderpage.base.common.NonThrowError;
import com.coderpage.base.common.Result;
import com.coderpage.base.common.SimpleCallback;
import com.coderpage.concurrency.MineExecutors;
import com.coderpage.mine.MineApp;
import com.coderpage.mine.R;
import com.coderpage.mine.app.tally.common.error.ErrorCode;
import com.coderpage.mine.app.tally.persistence.model.Account;
import com.coderpage.mine.app.tally.persistence.sql.TallyDatabase;
import com.coderpage.mine.app.tally.persistence.sql.dao.AccountDao;
import com.coderpage.mine.app.tally.persistence.sql.entity.AccountEntity;
import com.coderpage.mine.utils.AndroidUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zzy. 2026-09-10 10:11
 * @since 0.6.2
 *
 * 账户数据仓库
 */
public class AccountRepository {

    private final AccountDao mAccountDao;

    public AccountRepository() {
        mAccountDao = TallyDatabase.getInstance().accountDao();
    }

    public void queryAll(SimpleCallback<List<Account>> callback) {
        MineExecutors.ioExecutor().execute(() -> {
            List<Account> result = queryAllSync();
            MineExecutors.executeOnUiThread(() -> callback.success(result));
        });
    }

    public void queryDefault(SimpleCallback<Account> callback) {
        MineExecutors.ioExecutor().execute(() -> {
            Account account = queryDefaultSync();
            MineExecutors.executeOnUiThread(() -> callback.success(account));
        });
    }

    public void queryById(long accountId, SimpleCallback<Account> callback) {
        MineExecutors.ioExecutor().execute(() -> {
            AccountEntity entity = mAccountDao.queryById(accountId);
            Account account = entity == null ? null : toAccount(entity);
            MineExecutors.executeOnUiThread(() -> callback.success(account));
        });
    }

    public List<Account> queryAllSync() {
        ensureDefaultAccountExists();
        List<AccountEntity> entityList = mAccountDao.queryAll();
        List<Account> result = new ArrayList<>(entityList.size());
        for (AccountEntity entity : entityList) {
            result.add(toAccount(entity));
        }
        return result;
    }

    public Account queryDefaultSync() {
        ensureDefaultAccountExists();
        AccountEntity entity = mAccountDao.queryDefault();
        if (entity == null) {
            List<AccountEntity> all = mAccountDao.queryAll();
            if (!all.isEmpty()) {
                entity = all.get(0);
                mAccountDao.clearDefault();
                mAccountDao.setDefault(entity.getId());
                entity.setIsDefault(1);
            }
        }
        return entity == null ? null : toAccount(entity);
    }

    public void addAccount(String name, double initialBalance, SimpleCallback<Result<Account, IError>> callback) {
        MineExecutors.ioExecutor().execute(() -> {
            ensureDefaultAccountExists();
            AccountEntity entity = new AccountEntity();
            entity.setName(name.trim());
            entity.setInitialBalance(initialBalance);
            entity.setIsDefault(mAccountDao.count() == 0 ? 1 : 0);
            entity.setOrder(0);
            entity.setSyncId(AndroidUtils.generateUUID());
            entity.setCreateTime(System.currentTimeMillis());
            long id = mAccountDao.insert(entity);
            entity.setId(id);
            Account account = toAccount(entity);
            MineExecutors.executeOnUiThread(() -> callback.success(new Result<>(account, null)));
        });
    }

    public void setDefault(long accountId, SimpleCallback<Result<Void, IError>> callback) {
        MineExecutors.ioExecutor().execute(() -> {
            AccountEntity entity = mAccountDao.queryById(accountId);
            if (entity == null) {
                MineExecutors.executeOnUiThread(() -> callback.success(new Result<>(null,
                        new NonThrowError(ErrorCode.ILLEGAL_ARGS,
                                MineApp.getAppContext().getString(R.string.tally_account_not_found)))));
                return;
            }
            mAccountDao.clearDefault();
            mAccountDao.setDefault(accountId);
            MineExecutors.executeOnUiThread(() -> callback.success(new Result<>(null, null)));
        });
    }

    public void deleteAccount(long accountId, SimpleCallback<Result<Void, IError>> callback) {
        MineExecutors.ioExecutor().execute(() -> {
            AccountEntity entity = mAccountDao.queryById(accountId);
            if (entity == null) {
                MineExecutors.executeOnUiThread(() -> callback.success(new Result<>(null,
                        new NonThrowError(ErrorCode.ILLEGAL_ARGS,
                                MineApp.getAppContext().getString(R.string.tally_account_not_found)))));
                return;
            }
            if (mAccountDao.count() <= 1) {
                MineExecutors.executeOnUiThread(() -> callback.success(new Result<>(null,
                        new NonThrowError(ErrorCode.ILLEGAL_ARGS,
                                MineApp.getAppContext().getString(R.string.tally_account_delete_last_forbid)))));
                return;
            }
            if (mAccountDao.countRecords(accountId) > 0) {
                MineExecutors.executeOnUiThread(() -> callback.success(new Result<>(null,
                        new NonThrowError(ErrorCode.ILLEGAL_ARGS,
                                MineApp.getAppContext().getString(R.string.tally_account_delete_has_record)))));
                return;
            }
            boolean wasDefault = entity.isDefaultAccount();
            mAccountDao.delete(entity);
            if (wasDefault) {
                List<AccountEntity> remain = mAccountDao.queryAll();
                if (!remain.isEmpty()) {
                    mAccountDao.clearDefault();
                    mAccountDao.setDefault(remain.get(0).getId());
                }
            }
            MineExecutors.executeOnUiThread(() -> callback.success(new Result<>(null, null)));
        });
    }

    private Account toAccount(AccountEntity entity) {
        double balance = entity.getInitialBalance() + mAccountDao.queryBalanceDelta(entity.getId());
        return new Account(entity, balance);
    }

    private void ensureDefaultAccountExists() {
        if (mAccountDao.count() > 0) {
            return;
        }
        AccountEntity entity = new AccountEntity();
        entity.setName(MineApp.getAppContext().getString(R.string.tally_account_default_name));
        entity.setInitialBalance(0);
        entity.setIsDefault(1);
        entity.setOrder(0);
        entity.setSyncId(AndroidUtils.generateUUID());
        entity.setCreateTime(System.currentTimeMillis());
        long id = mAccountDao.insert(entity);
        mAccountDao.bindOrphanRecords(id);
    }
}
