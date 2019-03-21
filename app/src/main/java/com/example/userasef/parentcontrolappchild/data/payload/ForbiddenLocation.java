package com.example.userasef.parentcontrolappchild.data.payload;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

@Entity(tableName = "forbidden_locations")
public class ForbiddenLocation {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private double latitude;
    private double longitude;

    public ForbiddenLocation() {
    }

    @Ignore
    public ForbiddenLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    @Ignore
    @NonNull
    @Override
    public String toString() {
        String s = "";

        s += "Lat: " + latitude + ", ";
        s += "Long: " + longitude + ", ";

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
        final int prime = 33;
        int result = 1;

        result = prime * result + Double.valueOf(latitude).hashCode();
        result = prime * result + Double.valueOf(longitude).hashCode();

        return result;
    }
}
