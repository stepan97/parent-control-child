package com.example.userasef.parentcontrolappchild.roomPersistence;

import io.reactivex.Flowable;

public class UserRepository implements IUserDataSource {

    private IUserDataSource mLocalDataSource;

    public static UserRepository mInstance;

    public UserRepository(IUserDataSource mLocalDataSource){
        this.mLocalDataSource = mLocalDataSource;
    }

    public static UserRepository getmInstance(IUserDataSource mLocalDataSource){
        if(mInstance == null){
            mInstance = new UserRepository(mLocalDataSource);
        }

        return mInstance;
    }

    @Override
    public Flowable<UserModel> getAllUsers() {
        return mLocalDataSource.getAllUsers();
    }

    @Override
    public void insertUser(UserModel... users) {
        mLocalDataSource.insertUser(users);
    }
}
