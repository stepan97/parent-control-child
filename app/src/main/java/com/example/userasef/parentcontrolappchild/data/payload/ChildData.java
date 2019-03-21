package com.example.userasef.parentcontrolappchild.data.payload;

import android.arch.persistence.room.Relation;

import java.util.ArrayList;

public class ChildData{
    private int id;
    @Relation(parentColumn = "id", entityColumn = "userId")
    private ArrayList<MyCallLog> callLogs;
    @Relation(parentColumn = "id", entityColumn = "userId")
    private ArrayList<MySmsLog> smsLogs;
    @Relation(parentColumn = "id", entityColumn = "userId")
    private ArrayList<MyLatLng> locations;

    public ChildData() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ArrayList<MyCallLog> getCallLogs() {
        return callLogs == null ? new ArrayList<MyCallLog>() : callLogs;
    }

    public void setCallLogs(ArrayList<MyCallLog> callLogs) {
        this.callLogs = callLogs;
    }

    public ArrayList<MySmsLog> getSmsLogs() {
        return smsLogs == null ? new ArrayList<MySmsLog>() : smsLogs;
    }

    public void setSmsLogs(ArrayList<MySmsLog> smsLogs) {
        this.smsLogs = smsLogs;
    }

    public ArrayList<MyLatLng> getLocations() {
        return locations == null ? new ArrayList<MyLatLng>() : locations;
    }

    public void setLocations(ArrayList<MyLatLng> locations) {
        this.locations = locations;
    }
}
