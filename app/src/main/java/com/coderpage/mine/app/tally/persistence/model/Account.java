package com.coderpage.mine.app.tally.persistence.model;

import com.coderpage.mine.app.tally.persistence.sql.entity.AccountEntity;

/**
 * @author zzy. 2026-09-10 10:11
 * @since 0.6.2
 *
 * 账户展示模型（含计算后的余额）
 */
public class Account {

    private long id;
    private String name = "";
    private double initialBalance;
    private boolean defaultAccount;
    private int order;
    private String syncId = "";
    private long createTime;
    /** 当前余额 = 初始余额 + 收入 - 支出 */
    private double balance;

    public Account() {
    }

    public Account(AccountEntity entity, double balance) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.initialBalance = entity.getInitialBalance();
        this.defaultAccount = entity.isDefaultAccount();
        this.order = entity.getOrder();
        this.syncId = entity.getSyncId();
        this.createTime = entity.getCreateTime();
        this.balance = balance;
    }

    public AccountEntity createEntity() {
        AccountEntity entity = new AccountEntity();
        entity.setId(id);
        entity.setName(name == null ? "" : name);
        entity.setInitialBalance(initialBalance);
        entity.setIsDefault(defaultAccount ? 1 : 0);
        entity.setOrder(order);
        entity.setSyncId(syncId == null ? "" : syncId);
        entity.setCreateTime(createTime);
        return entity;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(double initialBalance) {
        this.initialBalance = initialBalance;
    }

    public boolean isDefaultAccount() {
        return defaultAccount;
    }

    public void setDefaultAccount(boolean defaultAccount) {
        this.defaultAccount = defaultAccount;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getSyncId() {
        return syncId;
    }

    public void setSyncId(String syncId) {
        this.syncId = syncId;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
