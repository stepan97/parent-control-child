package com.example.userasef.parentcontrolappchild.database.TypeConverters;


import android.arch.persistence.room.TypeConverter;

import java.util.Date;

public class DateConverter {
    @TypeConverter
    public static long toMilliseconds(Date date){
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static Date fromMilliseconds(Long value){
        return value == null ? null : new Date(value);
    }
}
