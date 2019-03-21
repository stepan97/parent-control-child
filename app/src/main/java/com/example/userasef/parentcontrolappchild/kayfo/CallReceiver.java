package com.example.userasef.parentcontrolappchild.kayfo;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.userasef.parentcontrolappchild.R;
import com.example.userasef.parentcontrolappchild.data.ResponseModel;
import com.example.userasef.parentcontrolappchild.data.payload.ChildData;
import com.example.userasef.parentcontrolappchild.data.payload.MyCallLog;
import com.example.userasef.parentcontrolappchild.database.MyDatabase;
import com.example.userasef.parentcontrolappchild.database.dao.MyDao;
import com.example.userasef.parentcontrolappchild.network.ChildClient;
import com.example.userasef.parentcontrolappchild.network.IChildService;

import java.util.ArrayList;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CallReceiver extends PhonecallReceiver {
    private static IChildService service = ChildClient.getClient().create(IChildService.class);

    @Override
    protected void onIncomingCallReceived(Context ctx, String number, Date start) {
        Log.d("TAGO", "INCOMING CALL RECEIVED: " + number);
    }

    @Override
    protected void onIncomingCallAnswered(Context ctx, String number, Date start) {
        Log.d("TAGO", "INCOMING CALL ANSWERED: " + number);
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
//        list.add(new MyCallLog(name, num, duration, direction, date));
        MyCallLog call = new MyCallLog(getContactDisplayNameByNumber(number, ctx), number, calculateDuration(start, end), ctx.getString(R.string.incoming), start);

        Log.d("TAGO", "INCOMING CALL ENDED: MyCallLog: ");
        Log.d("TAGO", call.toString());

        uploadToServer(call, ctx);
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        Log.d("TAGO", "OUTGOING CALL STARTED.");
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        MyCallLog call = new MyCallLog(getContactDisplayNameByNumber(number, ctx), number, calculateDuration(start, end), ctx.getString(R.string.outgoing), start);

        Log.d("TAGO", "OUTGOING CALL ENDED: MyCallLog: ");
        Log.d("TAGO", call.toString());
        uploadToServer(call, ctx);
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
        MyCallLog call = new MyCallLog(getContactDisplayNameByNumber(number, ctx), number, "0", ctx.getString(R.string.missed), start);

        Log.d("TAGO", "MISSED CALL: MyCallLog: ");
        Log.d("TAGO", call.toString());
        uploadToServer(call, ctx);
    }

    private String calculateDuration(Date start, Date end){
        long diffInMillies = end.getTime() - start.getTime();
        return "" + (diffInMillies / 1000 / 60);
    }

    /**
     * @param number phone number
     * @return the name matched with the phone number
     */
    private String getContactDisplayNameByNumber(String number, Context context) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String name = context.getString(R.string.no_name);

        ContentResolver contentResolver = context.getContentResolver();
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

    private void uploadToServer(final MyCallLog myCallLog, final Context context){
        ChildData childData = new ChildData();
        ArrayList<MyCallLog> callLogs = new ArrayList<MyCallLog>();
        callLogs.add(myCallLog);
        childData.setCallLogs(callLogs);

        service.uploadData(childData).enqueue(new Callback<ResponseModel<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ResponseModel<Void>> call, @NonNull Response<ResponseModel<Void>> response) {
                Log.d("TAGO", "RESPONSE RECEIVED AFTER uploading call log...");

                if(response.body() == null){
                    Log.d("TAGO", "RESPONSE BODY IS NULL.");
                    saveToLocalDb(myCallLog, context);
                    return;
                }

                if(response.body().getErrors() == null){
                    Log.d("TAGO", "UPLOADED Successfully");
                }else {
                    Log.d("TAGO", "UPLOAD FAILED: " + response.body().getErrors());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseModel<Void>> call, @NonNull Throwable t) {
                Log.d("TAGO", "COULD NOT UPLOAD CALLS TO SERVER... Trying to save in local db.");
                saveToLocalDb(myCallLog, context);
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private void saveToLocalDb(MyCallLog call, final Context context){
        final ChildData childData = new ChildData();
        ArrayList<MyCallLog> callLogs = new ArrayList<MyCallLog>();
        callLogs.add(call);
        childData.setCallLogs(callLogs);

        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                MyDao dao = MyDatabase.getDatabase(context).myDao();
                dao.insertChildDataInTransaction(childData);

                return null;
            }
        }.execute();
    }

}
