package com.example.userasef.parentcontrolappchild.data.payload;

public class ChildPayload {
    private String name;
    private String accessCode;

    public ChildPayload() {
    }

    public ChildPayload(String name, String accessCode) {
        this.name = name;
        this.accessCode = accessCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccessCode() {
        return accessCode;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }
}
