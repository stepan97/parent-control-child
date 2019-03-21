package com.example.userasef.parentcontrolappchild.utils;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MyDateUtils {
    /**
     * @return Milliseconds representing today with 0 hour/minute etc
     */
    public static long getTodayWithZeros() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTimeInMillis();
    }

    public static long getDateHourAgo(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY) - 1);

        return cal.getTimeInMillis();
    }

    public static Date convertStringToMyDate(String dateString) {

        if (dateString == null || dateString.equals("")) {
            dateString = Long.toString(new Date().getTime());
        }

        long miliseconds = Long.parseLong(dateString);

        Date date = new Date(miliseconds);
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm", Locale.getDefault());

        String resultDateString = formatter.format(date);

        Date resultDate = null;

        try {
            resultDate = formatter.parse(resultDateString);
        } catch (ParseException ex) {
            Log.d("TAGO", "Date parsing exception. resultString: " + resultDateString);
            ex.printStackTrace();
        }

        return resultDate;
    }
}
