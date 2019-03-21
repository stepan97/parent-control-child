package com.example.userasef.parentcontrolappchild.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.TypeConverters;

import com.example.userasef.parentcontrolappchild.data.payload.MyCallLog;
import com.example.userasef.parentcontrolappchild.database.TypeConverters.DateConverter;

import java.util.Date;
import java.util.List;

@Dao
@TypeConverters(DateConverter.class)
public interface CallLogDao {
    @Insert
    void insert(MyCallLog callLog);

    @Insert
    void insertAll(List<MyCallLog> list);

    @Query("SELECT * FROM call_log_table WHERE date>=:date")
    List<MyCallLog> getAllForWeek(Date date);

    @Query("DELETE FROM call_log_table")
    void deleteAll();

    @Query("DELETE FROM call_log_table WHERE date>=:date")
    void deleteOldOnes(Date date);
}
