package com.example.userasef.parentcontrolappchild.data.payload;


import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.userasef.parentcontrolappchild.database.TypeConverters.DateConverter;

import java.util.Date;

@Entity(tableName = "sms_log_table")
@TypeConverters({DateConverter.class})
public class MySmsLog{

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String number;
    private Date date;
    private String type;
    private String body;

    public MySmsLog() {
    }

    @Ignore
    public MySmsLog(String name, String number, Date date, String type, String body) {
        this.name = name;
        this.number = number;
        this.date = date;
        this.type = type;
        this.body = body;
    }

    public int getId(){return id;}

    public void setId(int id){this.id = id;}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Ignore
    @NonNull
    @Override
    public String toString() {
        String s = "";

        s += "name: " + name + ", ";
        s += "number: " + number + ", ";
        s += "type: " + type + ", ";
        s += "body: " + body;

        if(date != null)
            s += "date: " + date + ", ";

        return s;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        MySmsLog sms = (MySmsLog) obj;

        return number.equals(sms.getNumber()) &&
                type.equals(sms.getType()) &&
                body.equals(sms.getBody()) &&
                date.equals(sms.getDate());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (number == null ? 0 : number.hashCode());
        result = prime * result + (type == null ? 0 : type.hashCode());
        result = prime * result + (body == null ? 0 : body.hashCode());
        result = prime * result + (date == null ? 0 : date.hashCode());
        return result;
    }
}
