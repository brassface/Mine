package com.coderpage.mine.app.tally.persistence.sql.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.coderpage.mine.app.tally.persistence.sql.entity.AccountEntity;

import java.util.List;

/**
 * @author zzy. 2026-09-10 10:11
 * @since 0.6.2
 *
 * 账户表操作
 */
@Dao
public interface AccountDao {

    @Query("select * from account order by account_is_default desc, account_order asc, account_id asc")
    List<AccountEntity> queryAll();

    @Query("select * from account where account_id = :id")
    AccountEntity queryById(long id);

    @Query("select * from account where account_is_default = 1 limit 1")
    AccountEntity queryDefault();

    @Query("select count(*) from account")
    int count();

    @Query("select count(*) from record where record_account_id = :accountId")
    int countRecords(long accountId);

    @Query("select ifnull(sum(case when record_type = 1 then record_amount "
            + "when record_type = 0 then -record_amount else 0 end), 0) "
            + "from record where record_account_id = :accountId")
    double queryBalanceDelta(long accountId);

    @Insert
    long insert(AccountEntity account);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long[] insert(AccountEntity... accounts);

    @Update
    int update(AccountEntity account);

    @Query("update account set account_is_default = 0")
    void clearDefault();

    @Query("update account set account_is_default = 1 where account_id = :accountId")
    void setDefault(long accountId);

    @Delete
    void delete(AccountEntity account);

    @Query("update record set record_account_id = :accountId where record_account_id = 0")
    int bindOrphanRecords(long accountId);
}
