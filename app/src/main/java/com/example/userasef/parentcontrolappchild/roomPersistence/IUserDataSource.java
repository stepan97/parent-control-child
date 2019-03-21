package com.example.userasef.parentcontrolappchild.roomPersistence;

import io.reactivex.Flowable;

public interface IUserDataSource {
    Flowable<UserModel> getAllUsers();
    void insertUser(UserModel... users);
}
