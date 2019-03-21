package com.example.userasef.parentcontrolappchild.controller;

import android.content.Context;

import com.example.userasef.parentcontrolappchild.data.response.User;
import com.example.userasef.parentcontrolappchild.utils.Constants;
import com.example.userasef.parentcontrolappchild.utils.PreferencesUtils;
import com.google.gson.Gson;

public class DataController {
    private User user;
    private static final DataController ourInstance = new DataController();

    private DataController(){

    }

    public static DataController getInstance(){
        return ourInstance;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isSignIn(){
        return user != null;
    }

    public void init(Context context) {
        String userString = PreferencesUtils.getString(context, Constants.USER_GLOBAL,null);
        Gson gson = new Gson();
        if (userString != null){
            user = gson.fromJson(userString,User.class);
        }
    }
}
