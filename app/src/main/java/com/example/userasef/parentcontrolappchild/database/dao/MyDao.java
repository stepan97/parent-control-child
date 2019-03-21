package com.example.userasef.parentcontrolappchild.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;
import android.arch.persistence.room.TypeConverters;
import android.util.Log;

import com.example.userasef.parentcontrolappchild.data.payload.ChildData;
import com.example.userasef.parentcontrolappchild.data.payload.ForbiddenLocation;
import com.example.userasef.parentcontrolappchild.data.payload.MyCallLog;
import com.example.userasef.parentcontrolappchild.data.payload.MyLatLng;
import com.example.userasef.parentcontrolappchild.data.payload.MySmsLog;
import com.example.userasef.parentcontrolappchild.database.TypeConverters.DateConverter;
import com.example.userasef.parentcontrolappchild.utils.MyDateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Dao
@TypeConverters(DateConverter.class)
public abstract class MyDao {
    /**  Call Log  **/
    @Insert
    public abstract void insertCallLog(MyCallLog callLog);

    @Insert
    public abstract void insertAllCallLogs(List<MyCallLog> list);

    @Query("SELECT * FROM call_log_table WHERE date>=:date")
    public abstract List<MyCallLog> getAllCallLogsForWeek(Date date);

    @Query("DELETE FROM call_log_table")
    public abstract void deleteAllCallLogs();

    @Query("DELETE FROM call_log_table WHERE date<:date")
    public abstract void deleteOldCallLogs(Date date);

    /**  Sms Log  **/
    @Insert
    public abstract void insertSmsLog(MySmsLog smsLog);

    @Insert
    public abstract void insertAllSmsLogs(List<MySmsLog> list);

    @Query("SELECT * FROM sms_log_table WHERE date>=:date")
    public abstract List<MySmsLog> getAllForSmsLogsWeek(Date date);

    @Query("DELETE FROM sms_log_table")
    public abstract void deleteAllSmsLogs();

    @Query("DELETE FROM sms_log_table WHERE date<:date")
    public abstract void deleteOldSmsLogs(Date date);

    /**  LOCATIONS  **/
    @Insert
    public abstract void insertLocation(MyLatLng location);

    @Insert
    public abstract void insertAllLocations(List<MyLatLng> list);

    @Query("SELECT * FROM location_table WHERE date>=:date")
    public abstract List<MyLatLng> getAllLocationsForWeek(Date date);

    @Query("DELETE FROM location_table")
    public abstract void deleteAllLocations();

    @Query("DELETE FROM location_table WHERE date>=:date")
    public abstract void deleteOldLocations(Date date);

    @Query("SELECT * FROM forbidden_locations")
    public abstract List<ForbiddenLocation> getForbiddenLocations();

    @Transaction
    public ChildData getChildDataInTransaction(){
        ChildData data = new ChildData();

        Date today = new Date(MyDateUtils.getTodayWithZeros());

        data.setCallLogs((ArrayList<MyCallLog>)getAllCallLogsForWeek(today));
        data.setSmsLogs((ArrayList<MySmsLog>) getAllForSmsLogsWeek(today));
        data.setLocations((ArrayList<MyLatLng>) getAllLocationsForWeek(today));

        return data;
    }

    @Transaction
    public void insertChildDataInTransaction(ChildData data){
        if(data == null){
            Log.d("TAGO", "ChildData is NULL in MyDao.java");
        }

        if(data.getCallLogs() != null && data.getCallLogs().size() > 0)
            insertAllCallLogs(data.getCallLogs());
        if(data.getSmsLogs() != null && data.getSmsLogs().size() > 0)
            insertAllSmsLogs(data.getSmsLogs());
        if(data.getLocations() != null && data.getLocations().size() > 0)
            insertAllLocations(data.getLocations());
    }

    @Transaction
    public void deleteChildDataInTransaction(){
        deleteAllCallLogs();
        deleteAllSmsLogs();
        deleteAllLocations();
    }


    /**     Forbidden Locations     **/
    @Insert
    public abstract void insertForbiddenLocation(ForbiddenLocation location);

    @Insert
    public abstract void insertAllForbiddenLocations(List<ForbiddenLocation> locations);

    @Query("SELECT * FROM forbidden_locations")
    public abstract List<ForbiddenLocation> getAllForbiddenLocations();

    @Query("DELETE FROM forbidden_locations WHERE latitude==:givenLatitude AND longitude==:givenLongitude")
    public abstract void deleteForbiddenLocation(double givenLatitude, double givenLongitude);

    @Query("DELETE FROM forbidden_locations")
    public abstract void deleteAllForbiddenLocations();
}
