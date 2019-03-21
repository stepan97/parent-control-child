package com.example.userasef.parentcontrolappchild.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.BaseColumns;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.example.userasef.parentcontrolappchild.R;
import com.example.userasef.parentcontrolappchild.controller.DataController;
import com.example.userasef.parentcontrolappchild.data.ResponseModel;
import com.example.userasef.parentcontrolappchild.data.payload.ChildData;
import com.example.userasef.parentcontrolappchild.data.payload.MyCallLog;
import com.example.userasef.parentcontrolappchild.data.payload.MyLatLng;
import com.example.userasef.parentcontrolappchild.data.payload.MySmsLog;
import com.example.userasef.parentcontrolappchild.data.response.User;
import com.example.userasef.parentcontrolappchild.database.MyDatabase;
import com.example.userasef.parentcontrolappchild.database.dao.CallLogDao;
import com.example.userasef.parentcontrolappchild.database.dao.LocationDao;
import com.example.userasef.parentcontrolappchild.database.dao.SmsLogDao;
import com.example.userasef.parentcontrolappchild.network.ChildClient;
import com.example.userasef.parentcontrolappchild.network.IChildService;
import com.example.userasef.parentcontrolappchild.utils.Constants;
import com.example.userasef.parentcontrolappchild.utils.PreferencesUtils;
import com.google.gson.Gson;

import java.net.ConnectException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// todo: write all code again using onPostExecute

public class UpdateService extends Service {
    private static final long TIME_ = 60 * 60 * 1000; // 1 hour
    //    private NestService nestService;
    public static final int LOCATION_UPDATE_MIN_DISTANCE = 500; // 500 meters
    public static final int LOCATION_UPDATE_MIN_TIME = 60 * 60 * 1000; // 1 hour
    private LocationManager mLocationManager;
    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private ChildData childData;
    private ArrayList<MyCallLog> callLogsFromDb;
    private ArrayList<MySmsLog> smsLogsFromDb;
    private ArrayList<MyLatLng> locationsFromDb;

    private boolean callLogsChecked = false;
    private boolean smsLogsChecked = false;
    private boolean locationsChecked = false;

    private boolean callLogsDeleted = false;
    private boolean smsLogsDeleted = false;
    private boolean locationsDeleted = false;

    private IChildService childService;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("RESPONSE", "CREATED");
        childService = ChildClient.getClient().create(IChildService.class);

        mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        childData = new ChildData();
        callLogsFromDb = new ArrayList<>();
        smsLogsFromDb = new ArrayList<>();
        locationsFromDb = new ArrayList<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("TAGO", "Service started");

        if(childService == null){
            childService = ChildClient.getClient().create(IChildService.class);
        }

        sendPing();

        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePI = PendingIntent.getService(
                getApplicationContext(), 1, restartServiceIntent,
                PendingIntent.FLAG_ONE_SHOT);

        //Restart the service once it has been killed android
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 100, restartServicePI);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sendPing() {
        Intent restartService = new Intent(getApplicationContext(),
                this.getClass());
        restartService.setPackage(getPackageName());
        PendingIntent restartServicePI = PendingIntent.getService(
                getApplicationContext(), 1, restartService,
                PendingIntent.FLAG_ONE_SHOT);


        // RESPONSIBILITIES
        // 1. get data from db
        // 2. get data from call logs and add to childData
        // 3. get data from sms logs and add to childData
        // 4. get current location and add to childData
        // 5. sync
        // 6. upload or save to db

        // as these 3 methods use async tasks, they take care of calling other functions
        getCallLogsFromDb();
        getSmsLogsFromDb();
        getLocationsFromDb();

        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + TIME_, restartServicePI);
    }

    /**
     * Gets call logs from phone, then call logs form db, syncs them
     */
    private void getCallLogsFromPhone() {
        Log.d("TAGO", "started getCallLogsFromPhone");
        ArrayList<MyCallLog> list = new ArrayList<>();

        // get today's calls
        Cursor c = getContentResolver().query(CallLog.Calls.CONTENT_URI, null, CallLog.Calls.DATE + ">=?", new String[]{Long.toString(getTodayWithZeros())}, CallLog.Calls.DATE);

        if (c == null)
            return;

        try {
            if (!c.moveToLast()) {
                Log.d("TAGO", "NO ELEMENT getCallsFromPhone");
            }

            for (int i = 0; i < c.getCount(); i++) {
                String num = c.getString(c.getColumnIndex(CallLog.Calls.NUMBER));// for  number
                String name = c.getString(c.getColumnIndex(CallLog.Calls.CACHED_NAME));// for name
                String duration = c.getString(c.getColumnIndex(CallLog.Calls.DURATION));// for duration
                String callDate = c.getString(c.getColumnIndex(CallLog.Calls.DATE)); // for date
                int type = Integer.parseInt(c.getString(c.getColumnIndex(CallLog.Calls.TYPE)));// for call type, Incoming or out going.

                Date date = convertStringToMyDate(callDate);

                String direction;

                switch (type) {
                    case CallLog.Calls.INCOMING_TYPE:
                        direction = getApplicationContext().getString(R.string.incoming);
                        break;
                    case CallLog.Calls.OUTGOING_TYPE:
                        direction = getApplicationContext().getString(R.string.outgoing);
                        break;
                    case CallLog.Calls.REJECTED_TYPE:
                        direction = getApplicationContext().getString(R.string.rejected);
                        break;
                    case CallLog.Calls.MISSED_TYPE:
                        direction = getApplicationContext().getString(R.string.missed);
                        break;
                    default:
                        direction = getApplicationContext().getString(R.string.unknown);
                }

                list.add(new MyCallLog(name, num, duration, direction, date));

                c.moveToPrevious();
            }

            this.childData.setCallLogs(list);
            c.close();

        } catch (NullPointerException ex) {
            ex.printStackTrace();
            Log.d("TAGO", "EXCEPTION in getCallLogsFromPhone");
        }

        Log.d("TAGO", "finishied getCallLogsFromPhone");
    }

    private void getSMSLogsFromPhone() {
        Log.d("TAGO", "started getSmsLogsFromPhone");

        ArrayList<MySmsLog> list = new ArrayList<>();

        Uri smsUri = Uri.parse("content://sms");

        // get sms for today
        Cursor c = getContentResolver().query(smsUri, null, "date>=?", new String[]{Long.toString(getTodayWithZeros())}, null);
        String phNumber = null;

        if(c == null){
            return;
        }

        try {

            if (!c.moveToFirst()) {
                Log.d("TAGO", "NO ELEMENT getSmsFromPhone");
            }

            for (int i = 0; i < c.getCount(); i++) {
                String type = c.getString(c.getColumnIndex("type"));
                String body = c.getString(c.getColumnIndex("body"));
                String smsDate = c.getString(c.getColumnIndex("date"));
                phNumber = c.getString(c.getColumnIndex("address"));   //this is phone number rather than address

                String contactName = getContactDisplayNameByNumber(phNumber);

                Date date = convertStringToMyDate(smsDate);

                int typeCode = Integer.parseInt(type);
                String direction = "";
                //get the right direction
                switch (typeCode) {
                    case Telephony.Sms.MESSAGE_TYPE_INBOX:
                        direction = getApplicationContext().getString(R.string.incoming);
                        break;

                    case Telephony.Sms.MESSAGE_TYPE_OUTBOX:
                        direction = getApplicationContext().getString(R.string.outgoing);
                        break;

                    case Telephony.Sms.MESSAGE_TYPE_SENT:
                        direction = getApplicationContext().getString(R.string.sent);
                        break;

                    default:
                        direction = getApplicationContext().getString(R.string.unknown);
                        break;
                }
                list.add(new MySmsLog(contactName, phNumber, date, direction, body));

                c.moveToNext();
            }

            childData.setSmsLogs(list);
            c.close();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }

        Log.d("TAGO", "finished getSmsLogsFromPhone");
    }

    /**
     * Gets location from network if awailable, or from gps if available.
     * Otherwise requests permissions
     * Then upload all ChildData (including call logs and sms logs) or saves to local db
     */
    private void getCurrentLocation() {
        ArrayList<MyLatLng> list = new ArrayList<>();

        boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Location location = null;
        if (!(isGPSEnabled || isNetworkEnabled)) {
            Log.d("TAGO", "GPS or NETWORK is UNAVAILABLE");
            return;
        }

        if (isNetworkEnabled) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // As i cannot request permissions in a service, i will just send notification to parent
                // informing that his/her child has disabled permissions

                return;
            }
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    LOCATION_UPDATE_MIN_TIME, LOCATION_UPDATE_MIN_DISTANCE, mLocationListener);

            location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (location != null) {
                Date date = new Date();
                list.add(new MyLatLng(location.getLatitude(), location.getLongitude(), date, false));
                this.childData.setLocations(list);
                checkForbiddenLocations(location);
                return;
            }
        }

        if (isGPSEnabled) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    LOCATION_UPDATE_MIN_TIME, LOCATION_UPDATE_MIN_DISTANCE, mLocationListener);
            location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        if (location != null) {
            Date date = new Date();
            list.add(new MyLatLng(location.getLatitude(), location.getLongitude(), date, false));
            this.childData.setLocations(list);
            checkForbiddenLocations(location);
        }
    }

    /**
     * @param number phone number
     * @return the name matched with the phone number
     */
    private String getContactDisplayNameByNumber(String number) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String name = getApplicationContext().getString(R.string.no_name);

        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        Cursor contactLookup = contentResolver.query(uri, new String[]{BaseColumns._ID, ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

        try {
            if (contactLookup != null && contactLookup.getCount() > 0) {
                contactLookup.moveToFirst();
                name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                //String contactId = contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
            }
        } finally {
            if (contactLookup != null) {
                contactLookup.close();
            }
        }
        return name;
    }

    /**
     * Deletes data from db
     * Uploads ChildData to server or saves to local db if no internet
     */
    @SuppressLint("StaticFieldLeak")
    private void uploadOrSaveData() {
        if(!(callLogsDeleted && smsLogsDeleted && locationsDeleted))
            return;

        callLogsDeleted = false;
        smsLogsDeleted = false;
        locationsDeleted = false;

        // if there is internet
        String authToken = null;
        DataController.getInstance().init(getApplicationContext());
        authToken = DataController.getInstance().getUser().getAccessToken();
        if(authToken == null)
        {
            String userStr = PreferencesUtils.getString(getApplicationContext(), Constants.USER_GLOBAL, null);
            if(userStr != null)
            {
                Gson gson = new Gson();
                authToken = gson.fromJson(userStr, User.class).getAccessToken();
            }
        }

        // upload
        uploadDataToServer();

        deleteOldOnes();
    }

    // todo: vonc vor es methodn child-i mech petq chid
    private Date convertStringToMyDate(String dateString) {

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

    /**
     * @return Milliseconds representing today with 0 hour/minute etc
     */
    private long getTodayWithZeros() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTimeInMillis();
    }

    /**
     * Subtracts a week from current they, returns zeroed value
     *
     * @param date Date to subtract a week from
     * @return Date a week before given date
     */
    private Date minusWeekFromDate(Date date) {
        Calendar cal = Calendar.getInstance();

        cal.setTime(date);

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        cal.add(Calendar.WEEK_OF_YEAR, -1);
        cal.add(Calendar.DAY_OF_YEAR, -1);

        return new Date(cal.getTimeInMillis());
    }




    /**************** Database Operations *****************/

    /**
     * If all data is retrieved from data (calls, sms, locations)
     * calls methods for getting data from phone
     */
    private void retrievedDataFromDb() {
        Log.d("TAGO", "calls: " + callLogsChecked + ", sms: " + smsLogsChecked + ", locations: " + locationsChecked);
        if (callLogsChecked && smsLogsChecked && locationsChecked) {

            callLogsChecked = false;
            smsLogsChecked = false;
            locationsChecked = false;

            getCallLogsFromPhone();
            getSMSLogsFromPhone();
            getCurrentLocation();

            printDataFromChildData();

            // adds all data to childData
            checkIfSynced();

            deleteAllDataFromDb();

//            uploadOrSaveData();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void insertCallLogsToDb(final ArrayList<MyCallLog> list) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                CallLogDao callLogDao = MyDatabase.getDatabase(getApplicationContext()).callLogDao();
                callLogDao.insertAll(list);
                return null;
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private void getCallLogsFromDb() {
        Log.d("TAGO", "started getCallLogsFromDb");
        new AsyncTask<Void, Void, ArrayList<MyCallLog>>() {
            @Override
            protected ArrayList<MyCallLog> doInBackground(Void... voids) {
                CallLogDao callLogDao = MyDatabase.getDatabase(getApplicationContext()).callLogDao();
                return (ArrayList<MyCallLog>) callLogDao.getAllForWeek(new Date(getTodayWithZeros()));
            }

            @Override
            protected void onPostExecute(ArrayList<MyCallLog> myCallLogs) {
                // set list

                if (myCallLogs == null) {
                    Log.d("TAGO", "Call log list is NULL (getCallLogsFromDb) onPostExecute");
                    callLogsChecked = true;
                    retrievedDataFromDb();
                    return;
                }

                callLogsFromDb = new ArrayList<>(myCallLogs);

                Log.d("TAGO", "CALL LOGS FROM DB");
                for (int i = 0; i < myCallLogs.size(); i++) {
                    Log.d("TAGO", myCallLogs.get(i).toString());
                }

                Log.d("TAGO", "finished getCallLogsFromDb");
                callLogsChecked = true;
                retrievedDataFromDb();
            }
        }.execute();
    }


    @SuppressLint("StaticFieldLeak")
    private void insertSmsLogsToDb(final ArrayList<MySmsLog> list) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                SmsLogDao smsLogDao = MyDatabase.getDatabase(getApplicationContext()).smsLogDao();
                smsLogDao.insertAll(list);
                return null;
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private void getSmsLogsFromDb() {
        Log.d("TAGO", "started getSmsLogsFromDb");
        new AsyncTask<Void, Void, ArrayList<MySmsLog>>() {
            @Override
            protected ArrayList<MySmsLog> doInBackground(Void... voids) {
                SmsLogDao smsLogDao = MyDatabase.getDatabase(getApplicationContext()).smsLogDao();
                return (ArrayList<MySmsLog>) smsLogDao.getAllForWeek(new Date(getTodayWithZeros()));
            }

            @Override
            protected void onPostExecute(ArrayList<MySmsLog> mySmsLogs) {
                // set list
                if (mySmsLogs == null) {
                    Log.d("TAGO", "Sms log list is NULL (getSmsLogsFromDb) onPostExecute");
                    smsLogsChecked = true;
                    retrievedDataFromDb();
                    return;
                }

                smsLogsFromDb = new ArrayList<>(mySmsLogs);

                Log.d("TAGO", "SMS LOG FROM DB");
                for (int i = 0; i < mySmsLogs.size(); i++) {
                    Log.d("TAGO", mySmsLogs.get(i).toString());
                }

                Log.d("TAGO", "finished getSmsLogsFromDb");
                smsLogsChecked = true;
                retrievedDataFromDb();
            }
        }.execute();
    }


    @SuppressLint("StaticFieldLeak")
    private void insertLocationsToDb(final ArrayList<MyLatLng> list) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                LocationDao locationDao = MyDatabase.getDatabase(getApplicationContext()).locationDao();
                locationDao.insertAll(list);
                return null;
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private void getLocationsFromDb() {
        Log.d("TAGO", "started getLocationsFromDb");
        new AsyncTask<Void, Void, ArrayList<MyLatLng>>() {
            @Override
            protected ArrayList<MyLatLng> doInBackground(Void... voids) {
                LocationDao locationDao = MyDatabase.getDatabase(getApplicationContext()).locationDao();
                return (ArrayList<MyLatLng>) locationDao.getAllForWeek(new Date(getTodayWithZeros()));
            }

            @Override
            protected void onPostExecute(ArrayList<MyLatLng> myLocations) {
                // set list
                if (myLocations == null) {
                    Log.d("TAGO", "Location log list is NULL (getLocationsFromDb) onPostExecute");
                    locationsChecked = true;
                    retrievedDataFromDb();
                    return;
                }

                locationsFromDb = new ArrayList<>(myLocations);

                Log.d("TAGO", "LOCATIONS FROM DB");
                for (int i = 0; i < myLocations.size(); i++) {
                    Log.d("TAGO", myLocations.get(i).toString());
                }

                Log.d("TAGO", "fninished getLocationsFromDb");
                locationsChecked = true;
                retrievedDataFromDb();
            }
        }.execute();
    }

    /**
     * Checks if childData contains all elements from db,
     * adds new elements to childData
     */
    private void checkIfSynced() {
        Log.d("TAGO", "started checkIfSynced");


        for (int i = 0; i < callLogsFromDb.size(); i++) {
            if(!childData.getCallLogs().contains(callLogsFromDb.get(i))){
                childData.getCallLogs().add(callLogsFromDb.get(i));
            }
        }
        callLogsFromDb.clear();


        for (int i = 0; i < smsLogsFromDb.size(); i++) {
            if(!childData.getSmsLogs().contains(smsLogsFromDb.get(i))){
                childData.getSmsLogs().add(smsLogsFromDb.get(i));
            }
        }
        smsLogsFromDb.clear();


        for (int i = 0; i < locationsFromDb.size(); i++) {
            if(!childData.getLocations().contains(locationsFromDb.get(i))){
                childData.getLocations().add(locationsFromDb.get(i));
            }
        }
        locationsFromDb.clear();
    }

    private void printDataFromChildData(){
        Log.d("TAGO", "CALL LOGS FROM CHILD DATA");
        for (int i = 0; i < childData.getCallLogs().size(); i++) {
            Log.d("TAGO", childData.getCallLogs().get(i).toString());
        }

        Log.d("TAGO", "SMS LOGS FROM CHILD DATA");
        for (int i = 0; i < childData.getSmsLogs().size(); i++) {
            Log.d("TAGO", childData.getSmsLogs().get(i).toString());
        }

        Log.d("TAGO", "LOCATIONS FROM CHILD DATA");
        for (int i = 0; i < childData.getLocations().size(); i++) {
            Log.d("TAGO", childData.getLocations().get(i).toString());
        }
    }

    /**
     * Deletes All Data from db, calls function to upload
     */
    @SuppressLint("StaticFieldLeak")
    private void deleteAllDataFromDb(){
        Log.d("TAGO", "started deleteAllDataFromDb");
        final CallLogDao callLogDao = MyDatabase.getDatabase(getApplicationContext()).callLogDao();
        final SmsLogDao smsLogDao = MyDatabase.getDatabase(getApplicationContext()).smsLogDao();
        final LocationDao locationDao = MyDatabase.getDatabase(getApplicationContext()).locationDao();

        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                callLogDao.deleteAll();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                callLogsDeleted = true;
                uploadOrSaveData();
            }
        }.execute();

        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                smsLogDao.deleteAll();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                smsLogsDeleted = true;
                uploadOrSaveData();
            }
        }.execute();

        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                locationDao.deleteAll();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                locationsDeleted = true;
                uploadOrSaveData();
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private void deleteOldOnes(){
        final CallLogDao callLogDao = MyDatabase.getDatabase(getApplicationContext()).callLogDao();
        final SmsLogDao smsLogDao = MyDatabase.getDatabase(getApplicationContext()).smsLogDao();
        final LocationDao locationDao = MyDatabase.getDatabase(getApplicationContext()).locationDao();

        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                callLogDao.deleteOldOnes(minusWeekFromDate(new Date()));
                smsLogDao.deleteOldOnes(minusWeekFromDate(new Date()));
                locationDao.deleteOldOnes(minusWeekFromDate(new Date()));
                return null;
            }
        }.execute();
    }

    private void uploadDataToServer(){
        childService.uploadData(childData).enqueue(new Callback<ResponseModel<Void>>() {
            @Override
            public void onResponse(Call<ResponseModel<Void>> call, Response<ResponseModel<Void>> response) {
                if(!response.isSuccessful()){
                    // todo
                    return;
                }

                childData = new ChildData();
            }

            @Override
            public void onFailure(Call<ResponseModel<Void>> call, Throwable t) {
                if(t instanceof ConnectException){
                    Log.d("TAGO", "ConnectException: " + t.toString());
                }

                Log.d("TAGO", "NO INTERNET. So I need to save all to db...");

                // save to local db
                if(childData.getCallLogs().size() != 0)
                    insertCallLogsToDb(childData.getCallLogs());
                if(childData.getSmsLogs().size() != 0)
                    insertSmsLogsToDb(childData.getSmsLogs());
                if(childData.getLocations().size() != 0)
                    insertLocationsToDb(childData.getLocations());
            }
        });
    }


   @SuppressLint("StaticFieldLeak")
   private void checkForbiddenLocations(Location currentLocation){
        // get forbidden locations from db.
       // if current location is near one of them more than 100 meters, show notification
       double distance = 101;

       MyLatLng location = childData.getLocations().get(childData.getLocations().size() - 1);

       ArrayList<MyLatLng> forbiddenList = new ArrayList<>(locationsFromDb);

       for (int i = 0; i < locationsFromDb.size(); i++) {
           if(locationsFromDb.get(i).getForbidden()){
               Location location2 = new Location("");
               location2.setLatitude(locationsFromDb.get(i).getLatitude());
               location2.setLongitude(locationsFromDb.get(i).getLongitude());
               if(distanceIsLessThanAllowed(currentLocation, location2))
               {
//                   showForbiddenNotification();
                   break;
               }
           }
       }

       // iterate through locationsFromDb and get only those that are forbidden save to list
       // foreach item in list check if distance between it and currentLocation is < 100 meters
       // if yes show notification
   }

   private boolean distanceIsLessThanAllowed(Location location1, Location location2){
       return (int)location1.distanceTo(location2) < 100;
   }


}
