package com.example.userasef.parentcontrolappchild.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.BaseColumns;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.example.userasef.parentcontrolappchild.R;
import com.example.userasef.parentcontrolappchild.controller.DataController;
import com.example.userasef.parentcontrolappchild.data.ResponseModel;
import com.example.userasef.parentcontrolappchild.data.payload.ChildData;
import com.example.userasef.parentcontrolappchild.data.payload.ForbiddenLocation;
import com.example.userasef.parentcontrolappchild.data.payload.MyCallLog;
import com.example.userasef.parentcontrolappchild.data.payload.MyLatLng;
import com.example.userasef.parentcontrolappchild.data.payload.MySmsLog;
import com.example.userasef.parentcontrolappchild.data.payload.TrackingConfiguration;
import com.example.userasef.parentcontrolappchild.database.MyDatabase;
import com.example.userasef.parentcontrolappchild.database.dao.MyDao;
import com.example.userasef.parentcontrolappchild.kayfo.SmsObserver;
import com.example.userasef.parentcontrolappchild.network.ChildClient;
import com.example.userasef.parentcontrolappchild.network.IChildService;
import com.example.userasef.parentcontrolappchild.utils.Constants;
import com.example.userasef.parentcontrolappchild.utils.NetworkUtil;
import com.example.userasef.parentcontrolappchild.utils.PreferencesUtils;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.userasef.parentcontrolappchild.utils.MyDateUtils.convertStringToMyDate;
import static com.example.userasef.parentcontrolappchild.utils.MyDateUtils.getDateHourAgo;

public class MyService extends Service {
    private static final String TAG = "TAGO";

    // socket events
    private static final String CHILD_IN_FORBIDDEN_LOCATION = "forbidden_location";
    private static final String PARENT_ADDED_NEW_FORBIDDEN_LOCATION = "new_forbidden_location";
    private static final String NEW_CALLS_FROM_CHILD = "new_calls";

    private static final long TIME_ = 30 * 60 * 1000; // 0.5 hour
    public static final int LOCATION_UPDATE_MIN_DISTANCE = 1; // 100 meters
    public static final int LOCATION_UPDATE_MIN_TIME = 1000; //60 * 60 * 1000; // 1 hour
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    //    private IChildService childService;
    // when UPLOAD_COUNT==3, then upload, to be sure that all 3 data has been saved: calls, sms, locations
    private ChildData childData;
    private static IChildService service;
    private static Socket mSocket;
    {
        try {
//            IO.Options options = new IO.Options();
            String token = DataController.getInstance().getUser().getAccessToken();
            Log.d("TAGO", "TOKEN: " + token);
//            options.query = token;

            mSocket = IO.socket("https://protected-plateau-74640.herokuapp.com/?query=" + token);
//            mSocket = IO.socket("http://localhost:3000/");
        } catch (URISyntaxException e) {
            Log.d(TAG, "ERROR:" + e.getMessage());
            e.printStackTrace();
        }
    }

    private Emitter.Listener onNewForbiddenLocation = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.d("TAGO", "PARENT ADDED NEW FORBIDDEN LOCATION: " + args[0]);
//            Gson gson = new Gson();
//            JSONObject data = (JSONObject) args[0];
//            ForbiddenLocation myLatLng = gson.fromJson(data.toString(), ForbiddenLocation.class);
//
//            MyDao dao = MyDatabase.getDatabase(getApplicationContext()).myDao();
//            dao.insertForbiddenLocation(myLatLng);
        }
    };

    private Emitter.Listener onConnection = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.d("TAGO", "SOCKET CONNECTED");
//            Gson gson = new Gson();
//            JSONObject data = (JSONObject) args[0];
//            ForbiddenLocation myLatLng = gson.fromJson(data.toString(), ForbiddenLocation.class);
//
//            MyDao dao = MyDatabase.getDatabase(getApplicationContext()).myDao();
//            dao.insertForbiddenLocation(myLatLng);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        mSocket.on("connection", onConnection);
        mSocket.on(PARENT_ADDED_NEW_FORBIDDEN_LOCATION, onNewForbiddenLocation);

        mSocket.connect();

        childData = new ChildData();
        mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("TAGO", "Location changed" + location.getLatitude() + " " + location.getLongitude());
//                Toast.makeText(MyService.this, "Location changed.", Toast.LENGTH_SHORT).show();
                ArrayList<MyLatLng> list = new ArrayList<>();
                list.add(new MyLatLng(location.getLatitude(), location.getLongitude(), new Date()));
                childData.setLocations(list);
                uploadDataToServer();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d("TAGO", "Location status changed: " + provider + ", status: " + status);
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d("TAGO", "Provider enabled: " + provider);
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d("TAGO", "Provider disabled: " + provider);
            }
        };

        service = ChildClient.getClient().create(IChildService.class);

        boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (isNetworkEnabled)
            requestLocation(LocationManager.NETWORK_PROVIDER);
        else
            requestLocation(LocationManager.GPS_PROVIDER);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("TAGO", "Service started");

        if (service == null) {
            service = ChildClient.getClient().create(IChildService.class);
        }
        if (childData == null) {
            childData = new ChildData();
        }

        if(DataController.getInstance().getUser() == null){
            DataController.getInstance().init(getApplicationContext());
        }

        performMyActions();

        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePI = PendingIntent.getService(
                getApplicationContext(), 1, restartServiceIntent,
                PendingIntent.FLAG_ONE_SHOT);

        mSocket.disconnect();
        mSocket.off("connection", onConnection);

        //Restart the service once it has been killed android
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 100, restartServicePI);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void performMyActions() {
        Intent restartService = new Intent(getApplicationContext(),
                this.getClass());
        restartService.setPackage(getPackageName());
        PendingIntent restartServicePI = PendingIntent.getService(
                getApplicationContext(), 1, restartService,
                PendingIntent.FLAG_ONE_SHOT);

        // call my functions
//        getCurrentLocation();
//        getCallsFromPhone();
//        getSmsFromPhone();

//        saveChildDataToLocalDbAndUploadToServer();

        getChildDataFromLocalDb();

        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + TIME_, restartServicePI);
    }

    private void requestLocation(String provider) {

//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            AlertDialog.Builder alertDialog=new AlertDialog.Builder(getApplicationContext());
//            alertDialog.setTitle("Enable Location");
//            alertDialog.setMessage("Your locations setting is not enabled. Please enabled it in settings menu or Your parents will be mad at You !!.");
//            alertDialog.setPositiveButton("Location Settings", new DialogInterface.OnClickListener(){
//                public void onClick(DialogInterface dialog, int which){
//                    Intent intent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                    startActivity(intent);
//                }
//            });
//            AlertDialog alert=alertDialog.create();
//            alert.show();
//        }

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            Log.d("TAGO", "FINE LOCATION permission is not granted...");
            return;
        }

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            Log.d("TAGO", "COARSE LOCATION permission is not granted...");
            return;
        }

        mLocationManager.requestLocationUpdates(provider, LOCATION_UPDATE_MIN_TIME, LOCATION_UPDATE_MIN_DISTANCE, mLocationListener);
    }

    /**
     * Gets current location if INTERNET or GPS are available.
     * Otherwise it must require for internet/gps permissions,
     * informing the user that their parents will be mad at them.
     * Populates childData's locations arraylist
     * If location is Forbidden, shows notification and MUST send one to parent.
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
                childData.setLocations(list);
                checkForbiddenLocations(location);
                // todo: uncomment above, delete below
//                kayfoInsertToForbiddenTableForTesting(location);
                return;
            }
        }

//        if (isGPSEnabled) {
//            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
//                    LOCATION_UPDATE_MIN_TIME, LOCATION_UPDATE_MIN_DISTANCE, mLocationListener);
//            location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//        }

        if (location != null) {
            Date date = new Date();
            list.add(new MyLatLng(location.getLatitude(), location.getLongitude(), date, false));
            childData.setLocations(list);
//            checkForbiddenLocations(location);
            // todo: uncomment above, delete below
            kayfoInsertToForbiddenTableForTesting(location);
        }
    }

    /**
     * Gets calls for past hour from phone, populates childData's call_logs arraylist
     */
    private void getCallsFromPhone(){
        Log.d("TAGO", "started getCallLogsFromPhone");
        ArrayList<MyCallLog> list = new ArrayList<>();

        // get today's calls
        Cursor c = getContentResolver().query(CallLog.Calls.CONTENT_URI, null, CallLog.Calls.DATE + ">=?", new String[]{Long.toString(getDateHourAgo())}, CallLog.Calls.DATE);

        if (c == null)
            return;

        try {
            if (!c.moveToLast()) {
                Log.d("TAGO", "NO CALLS for past hour getCallsFromPhone");
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

            childData.setCallLogs(list);
            c.close();

        } catch (NullPointerException ex) {
            ex.printStackTrace();
            Log.d("TAGO", "EXCEPTION in getCallLogsFromPhone");
        }

        Log.d("TAGO", "finishied getCallLogsFromPhone");
    }

    /**
     * Gets sms for past hour from phone, populates childData's sms_logs arraylist
     */
    private void getSmsFromPhone(){
        Log.d("TAGO", "started getSmsLogsFromPhone");

        ArrayList<MySmsLog> list = new ArrayList<>();

        Uri smsUri = Uri.parse("content://sms");

        // get sms for today
        Cursor c = getContentResolver().query(smsUri, null, "date>=?", new String[]{Long.toString(getDateHourAgo())}, null);
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

    private void saveChildDataToLocalDb(){
        MyDao dao = MyDatabase.getDatabase(getApplicationContext()).myDao();
        dao.insertChildDataInTransaction(childData);
    }

    @SuppressLint("StaticFieldLeak")
    private void deleteDataFromLocalDb(){
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                MyDao dao = MyDatabase.getDatabase(getApplicationContext()).myDao();
                dao.deleteChildDataInTransaction();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                uploadDataToServer();
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private void getChildDataFromLocalDb(){
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                MyDao dao = MyDatabase.getDatabase(getApplicationContext()).myDao();
                childData = dao.getChildDataInTransaction();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
//                uploadDataToServer();
                if(!NetworkUtil.getConnectivityStatus(getApplicationContext()))
                    return;
                deleteDataFromLocalDb();
            }
        }.execute();
    }

    /**
     * Checks if all calls, sms and locations have been retrieved and uploads to server
     */
    @SuppressLint("StaticFieldLeak")
    private void uploadDataToServer(){
        if(!NetworkUtil.getConnectivityStatus(getApplicationContext()))
            return;

        service.uploadData(childData).enqueue(new Callback<ResponseModel<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ResponseModel<Void>> call,@NonNull Response<ResponseModel<Void>> response) {

                if(response.body() != null) {
                    if (response.body().getErrors() != null) {
                        childData = null;
                        Log.d("TAGO", "network error: " + response.body().getErrors());
                        return;
                    }

//                    deleteDataFromLocalDb();
                    childData = null;
                }else{
                    Log.d("TAGO", "NETWORK ERROR, TRY AGAIN LATER");
                    saveChildDataToLocalDb();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseModel<Void>> call,@NonNull Throwable t) {
                childData = null;
            }
        });

//        new AsyncTask<Void, Void, Void>(){
//            @Override
//            protected Void doInBackground(Void... voids) {
//                Log.d("TAGO", "UPLOADING CHILD DATA TO SERVER...");
//                // network logic
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Void aVoid) {
//                Log.d("TAGO", "UPLOADED CHILD DATA TO SERVER. NOW PRINTING AND THEN DELETE");
//
//                Log.i("TAGO", "CALLS");
//                for (int i = 0; i < childData.getCallLogs().size(); i++) {
//                    Log.d("TAGO", childData.getCallLogs().get(i).toString());
//                }
//
//                Log.i("TAGO", "SMS");
//                for (int i = 0; i < childData.getSmsLogs().size(); i++) {
//                    Log.d("TAGO", childData.getSmsLogs().get(i).toString());
//                }
//
//                Log.i("TAGO", "LOCATIONS");
//                for (int i = 0; i < childData.getLocations().size(); i++) {
//                    Log.d("TAGO", childData.getLocations().get(i).toString());
//                }
//
//
//                deleteDataFromLocalDb();
//            }
//        }.execute();
    }

    /**
     * Saves childData to local db, and then calls upload function
     */
    @SuppressLint("StaticFieldLeak")
    private void saveChildDataToLocalDbAndUploadToServer() {

        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                saveChildDataToLocalDb();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                childData = null;
                getChildDataFromLocalDb();
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private void kayfoInsertToForbiddenTableForTesting(final Location current){
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                // 40.1681332,44.2005943
                MyDao dao = MyDatabase.getDatabase(getApplicationContext()).myDao();
                dao.insertForbiddenLocation(new ForbiddenLocation(current.getLatitude(), current.getLongitude()));
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                checkForbiddenLocations(current);
            }
        }.execute();
    }

    /**
     * If given location is near some forbidden location, shows notification
     * @param currentLocation Current location of the user
     */
    @SuppressLint("StaticFieldLeak")
    private void checkForbiddenLocations(final Location currentLocation){
        // get forbidden locations from db.
        // if current location is near one of them more than 100 meters, show notification
        final int allowedDistance = 201;

        new AsyncTask<Void, Void, ArrayList<ForbiddenLocation>>(){
            @Override
            protected ArrayList<ForbiddenLocation> doInBackground(Void... voids) {
                MyDao locationDao = MyDatabase.getDatabase(getApplicationContext()).myDao();
                return (ArrayList<ForbiddenLocation>) locationDao.getForbiddenLocations();
            }

            @Override
            protected void onPostExecute(ArrayList<ForbiddenLocation> forbiddenList) {
                ForbiddenLocation myLocation = new ForbiddenLocation();
                myLocation.setLatitude(currentLocation.getLatitude());
                myLocation.setLongitude(currentLocation.getLongitude());

                for (int i = 0; i < forbiddenList.size(); i++) {
                    Location location2 = new Location("");
                    location2.setLatitude(forbiddenList.get(i).getLatitude());
                    location2.setLongitude(forbiddenList.get(i).getLongitude());
                    if(distanceIsLessThanAllowed(currentLocation, location2, allowedDistance))
                    {
//                showNotificationToUser();
//                sendNotificationToParent();
                        MyNotification.showNotification(getApplicationContext(), null, null);
                        break;
                    }
                }
            }
        }.execute();

    }

    private boolean distanceIsLessThanAllowed(Location location1, Location location2, int distance){
        return ((int)location1.distanceTo(location2) < distance);
    }

    private void getSentSmsFromPhone(){
        // gets sent sms from phone, calls uploadSentSmsData
        Log.d("TAGO", "started getSentSmsFromPhone");

        ArrayList<MySmsLog> list = new ArrayList<>();

        Uri smsUri = Uri.parse("content://sms/sent");
//        Telephony.Sms.Sent

        // get sms for today
        Cursor c = getContentResolver().query(smsUri, null, "date>=?", new String[]{Long.toString(getDateHourAgo())}, null);
        String phNumber = null;

        if(c == null){
            Log.d("TAGO", "c is null for sent sms...");
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

    private void uploadSentSmsData() {
        // uploads data to server,
        // success - do nothing
        // failure - save to local db
    }


}
