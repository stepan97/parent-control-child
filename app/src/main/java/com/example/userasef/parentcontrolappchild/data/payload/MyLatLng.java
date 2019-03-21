package com.example.userasef.parentcontrolappchild.data.payload;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.userasef.parentcontrolappchild.database.TypeConverters.DateConverter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Entity(tableName = "location_table")
@TypeConverters({DateConverter.class})
public class MyLatLng{
    @PrimaryKey(autoGenerate = true)
    private int id;
    private double latitude;
    private double longitude;
    private Date date;
    private boolean forbidden;

    public MyLatLng() {

    }

    @Ignore
    public MyLatLng(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Ignore
    public MyLatLng(double latitude, double longitude, Date date) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
    }

    @Ignore
    public MyLatLng(double latitude, double longitude, Date date, boolean forbidden) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
        this.forbidden = forbidden;
    }

    public int getId(){return id;}

    public void setId(int id){this.id = id;}

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getDateAndTime(){
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy hh:mm", Locale.getDefault());
        return dateFormatter.format(date);
    }

    public void setDate(Date date){
        this.date = date;
    }

    public Date getDate(){return this.date;}

    public String getTime(){
        SimpleDateFormat formatter = new SimpleDateFormat("hh : mm", Locale.getDefault());
        return formatter.format(date);
    }

    public boolean getForbidden() {
        return forbidden;
    }

    public void setForbidden(boolean value){
        this.forbidden = value;
    }

    @Ignore
    @NonNull
    @Override
    public String toString() {
        String s = "";

        //     private double latitude;
        //    private double longitude;
        //    private Date date;

        s += "Lat: " + latitude + ", ";
        s += "Long: " + longitude + ", ";

        if(date != null)
            s += "Date: " + date;

        return s;
    }

    @Ignore
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        MyLatLng loc = (MyLatLng) obj;

        return latitude == loc.getLatitude() &&
                longitude == loc.getLongitude();
    }

    @Ignore
    @Override
    public int hashCode() {
        final int prime = 32;
        int result = 1;

        result = prime * result + Double.valueOf(latitude).hashCode();
        result = prime * result + Double.valueOf(longitude).hashCode();

        return result;
    }


}
