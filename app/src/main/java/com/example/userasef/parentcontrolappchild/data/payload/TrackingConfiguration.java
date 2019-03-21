package com.example.userasef.parentcontrolappchild.data.payload;

import android.support.annotation.NonNull;

public class TrackingConfiguration {
    private boolean location;
    private boolean call_log;
    private boolean sms_log;

    public TrackingConfiguration() {
    }

    public TrackingConfiguration(boolean location, boolean call_log, boolean sms_log) {
        this.location = location;
        this.call_log = call_log;
        this.sms_log = sms_log;
    }

    public boolean isLocation() {
        return location;
    }

    public void setLocation(boolean location) {
        this.location = location;
    }

    public boolean isCall_log() {
        return call_log;
    }

    public void setCall_log(boolean call_log) {
        this.call_log = call_log;
    }

    public boolean isSms_log() {
        return sms_log;
    }

    public void setSms_log(boolean sms_log) {
        this.sms_log = sms_log;
    }

    @NonNull
    @Override
    public String toString() {
        return "CallLogs: " + call_log + ", SmsLog: " + sms_log + ", Location: " + location;
    }
}
