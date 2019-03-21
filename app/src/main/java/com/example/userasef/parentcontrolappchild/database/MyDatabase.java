package com.example.userasef.parentcontrolappchild.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.example.userasef.parentcontrolappchild.data.payload.ForbiddenLocation;
import com.example.userasef.parentcontrolappchild.data.payload.MyCallLog;
import com.example.userasef.parentcontrolappchild.data.payload.MyLatLng;
import com.example.userasef.parentcontrolappchild.data.payload.MySmsLog;
import com.example.userasef.parentcontrolappchild.database.dao.CallLogDao;
import com.example.userasef.parentcontrolappchild.database.dao.LocationDao;
import com.example.userasef.parentcontrolappchild.database.dao.MyDao;
import com.example.userasef.parentcontrolappchild.database.dao.SmsLogDao;

import java.util.Date;

@Database(entities = {MyCallLog.class, MySmsLog.class, MyLatLng.class, ForbiddenLocation.class}, version = 1)
public abstract class MyDatabase extends RoomDatabase {
    private static MyDatabase INSTANCE;
    private static String DATABASE_NAME = "my_database";

    public static MyDatabase getDatabase(final Context context){
        if(INSTANCE == null){
            synchronized (MyDatabase.class){
                if(INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            MyDatabase.class, DATABASE_NAME)
//                            .addCallback(new RoomDatabase.Callback(){
//                                @Override
//                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
//                                    super.onCreate(db);
//                                    new PopulateDbAsync(INSTANCE).execute();
//                                }
//                            })
                            .build();
                }
            }
        }

        return INSTANCE;
    }

    public abstract CallLogDao callLogDao();
    public abstract SmsLogDao smsLogDao();
    public abstract LocationDao locationDao();
    public abstract MyDao myDao();

}
