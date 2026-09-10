package com.coderpage.mine.app.tally.eventbus;

import com.coderpage.mine.app.tally.persistence.model.Account;

/**
 * @author zzy. 2026-09-10 10:11
 * @since 0.6.2
 *
 * 账户更新事件
 */
public class EventAccountUpdate {
    private final Account mAccount;

    public EventAccountUpdate(Account account) {
        mAccount = account;
    }

    public Account getAccount() {
        return mAccount;
    }
}
