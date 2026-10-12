package com.coderpage.mine.app.tally.eventbus;

/**
 * @author zzy. 2026-09-10 10:11
 * @since 0.6.2
 *
 * 账户删除事件
 */
public class EventAccountDelete {
    private final long mAccountId;

    public EventAccountDelete(long accountId) {
        mAccountId = accountId;
    }

    public long getAccountId() {
        return mAccountId;
    }
}
