package com.example.userasef.parentcontrolappchild.roomPersistence;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = UserModel.class, version = 1)
public abstract class UserDatabase extends RoomDatabase {
    public static final String DATABASE_NAME = "my-db-name";

    public abstract UserDao userDao();

    private static UserDatabase mInstance;
    public static UserDatabase getInstance(Context context){
        if(mInstance == null){
            mInstance = Room.databaseBuilder(context, UserDatabase.class, DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }

        return mInstance;
    }


}
