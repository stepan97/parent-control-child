package com.example.userasef.parentcontrolappchild.data.response;

import com.google.gson.annotations.Expose;

public class User {

    @Expose
    private String id;
    @Expose
    private String name;
    @Expose
    private String accessToken;

    public User(){}

    public User(String id, String name, String accessToken){
        this.id = id;
        this.name = name;
        this.accessToken = accessToken;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
