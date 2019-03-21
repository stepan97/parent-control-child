package com.example.userasef.parentcontrolappchild.roomPersistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import io.reactivex.Flowable;

@Dao
public interface UserDao {
    @Query("SELECT * FROM users")
    Flowable<UserModel> getAllUsers();

    @Insert
    void insertUser(UserModel... users);
}
