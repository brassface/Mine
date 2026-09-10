package com.coderpage.mine.app.tally.module.backup;

import android.support.annotation.Keep;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author zzy. 2026-09-10 10:11
 * @since 0.6.2
 *
 * 备份文件中的账户数据
 */
@Keep
public class BackupModelAccount {

    @JSONField(name = "id")
    private long id;

    @JSONField(name = "name")
    private String name;

    @JSONField(name = "initial_balance")
    private double initialBalance;

    @JSONField(name = "is_default")
    private int isDefault;

    @JSONField(name = "order")
    private int order;

    @JSONField(name = "sync_id")
    private String syncId;

    @JSONField(name = "create_time")
    private long createTime;

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

    public int getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(int isDefault) {
        this.isDefault = isDefault;
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
}
