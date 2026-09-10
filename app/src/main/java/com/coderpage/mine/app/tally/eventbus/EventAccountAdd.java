package com.coderpage.mine.app.tally.eventbus;

import com.coderpage.mine.app.tally.persistence.model.Account;

/**
 * @author zzy. 2026-09-10 10:11
 * @since 0.6.2
 *
 * 账户新增事件
 */
public class EventAccountAdd {
    private final Account mAccount;

    public EventAccountAdd(Account account) {
        mAccount = account;
    }

    public Account getAccount() {
        return mAccount;
    }
}
