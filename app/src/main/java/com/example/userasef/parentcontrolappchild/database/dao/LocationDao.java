package com.example.userasef.parentcontrolappchild.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.TypeConverters;

import com.example.userasef.parentcontrolappchild.data.payload.ForbiddenLocation;
import com.example.userasef.parentcontrolappchild.data.payload.MyLatLng;
import com.example.userasef.parentcontrolappchild.database.TypeConverters.DateConverter;

import java.util.Date;
import java.util.List;

@Dao
@TypeConverters(DateConverter.class)
public interface LocationDao {
    @Insert
    void insert(MyLatLng location);

    @Insert
    void insertAll(List<MyLatLng> list);

    @Query("SELECT * FROM location_table WHERE date>=:date")
    List<MyLatLng> getAllForWeek(Date date);

    @Query("DELETE FROM location_table")
    void deleteAll();

    @Query("DELETE FROM location_table WHERE date>=:date")
    void deleteOldOnes(Date date);

    @Query("SELECT * FROM forbidden_locations")
    List<ForbiddenLocation> getForbiddenLocations();
}
