package com.example.userasef.parentcontrolappchild.database.dao;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.TypeConverters;

import com.example.userasef.parentcontrolappchild.data.payload.MySmsLog;
import com.example.userasef.parentcontrolappchild.database.TypeConverters.DateConverter;

import java.util.Date;
import java.util.List;

@Dao
@TypeConverters(DateConverter.class)
public interface SmsLogDao {
    @Insert
    void insert(MySmsLog smsLog);

    @Insert
    void insertAll(List<MySmsLog> list);

    @Query("SELECT * FROM sms_log_table WHERE date>=:date")
    List<MySmsLog> getAllForWeek(Date date);

    @Query("DELETE FROM sms_log_table")
    void deleteAll();

    @Query("DELETE FROM sms_log_table WHERE date>=:date")
    void deleteOldOnes(Date date);
}
