package com.coderpage.mine.app.tally.persistence.sql.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * @author zzy. 2026-09-10 10:11
 * @since 0.6.2
 *
 * 资产账户
 */
@Entity(tableName = "account", indices = {@Index(value = {"account_sync_id"}, unique = true)})
public class AccountEntity {

    /** 账户 ID */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "account_id")
    private long id;

    /** 账户名称 */
    @NonNull
    @ColumnInfo(name = "account_name")
    private String name = "";

    /** 初始余额 */
    @ColumnInfo(name = "account_initial_balance")
    private double initialBalance;

    /** 是否默认账户：1 是，0 否 */
    @ColumnInfo(name = "account_is_default")
    private int isDefault;

    /** 排序 */
    @ColumnInfo(name = "account_order")
    private int order;

    /** 同步 ID */
    @NonNull
    @ColumnInfo(name = "account_sync_id")
    private String syncId = "";

    /** 创建时间 */
    @ColumnInfo(name = "account_create_time")
    private long createTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
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

    public boolean isDefaultAccount() {
        return isDefault == 1;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @NonNull
    public String getSyncId() {
        return syncId;
    }

    public void setSyncId(@NonNull String syncId) {
        this.syncId = syncId;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}
