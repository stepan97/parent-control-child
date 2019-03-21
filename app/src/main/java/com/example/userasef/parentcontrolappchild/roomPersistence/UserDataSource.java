package com.example.userasef.parentcontrolappchild.roomPersistence;

import io.reactivex.Flowable;

public class UserDataSource implements IUserDataSource {

    private UserDao userDao;
    private static UserDataSource mInstance;

    public UserDataSource(UserDao userDao){
        this.userDao = userDao;
    }

    public static UserDataSource getInstance(UserDao userDao){
        if(mInstance == null){
            mInstance = new UserDataSource(userDao);
        }

        return mInstance;
    }

    @Override
    public Flowable<UserModel> getAllUsers() {
        return userDao.getAllUsers();
    }

    @Override
    public void insertUser(UserModel... users) {
        userDao.insertUser(users);
    }
}
