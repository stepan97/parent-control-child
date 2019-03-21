package com.example.userasef.parentcontrolappchild.kayfo;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.example.userasef.parentcontrolappchild.R;
import com.example.userasef.parentcontrolappchild.data.ResponseModel;
import com.example.userasef.parentcontrolappchild.data.payload.ChildData;
import com.example.userasef.parentcontrolappchild.data.payload.MyCallLog;
import com.example.userasef.parentcontrolappchild.data.payload.MySmsLog;
import com.example.userasef.parentcontrolappchild.database.MyDatabase;
import com.example.userasef.parentcontrolappchild.database.dao.MyDao;
import com.example.userasef.parentcontrolappchild.network.ChildClient;
import com.example.userasef.parentcontrolappchild.network.IChildService;

import java.util.ArrayList;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.userasef.parentcontrolappchild.utils.MyDateUtils.convertStringToMyDate;
import static com.example.userasef.parentcontrolappchild.utils.MyDateUtils.getDateHourAgo;

public class MySmsReceiver extends BroadcastReceiver {
    private static IChildService service = ChildClient.getClient().create(IChildService.class);

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())){
            smsReceived(context);
//            Bundle bundle = intent.getExtras();           //---get the SMS message passed in---
//            SmsMessage[] msgs = null;
//            String msg_from;
//            if (bundle != null){
//                //---retrieve the SMS message received---
//                try{
//                    Object[] pdus = (Object[]) bundle.get("pdus");
//                    if(pdus == null) return;
//
//                    msgs = new SmsMessage[pdus.length];
//                    for(int i=0; i<msgs.length; i++){
//                        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
//                        msg_from = msgs[i].getOriginatingAddress();
////                        String msgBody = msgs[i].getMessageBody();
//
////                        Date date = new Date(msgs[i].getTimestampMillis());
////                        int status = msgs[i].getStatus();
////                        Log.d("TAGO", "TYPE: " + status);
////
////                        switch(status) {
////                            case Telephony.Sms.STATUS_NONE:
////                                Log.d("TAGO", "STATUS: NONE");
////                                break;
////                            case Telephony.Sms.STATUS_FAILED:
////                                Log.d("TAGO", "STATUS: FAILED.");
////                                break;
////                            case Telephony.Sms.STATUS_COMPLETE:
////                                Log.d("TAGO", "STATUS: COMPLETE.");
////                                break;
////                            case Telephony.Sms.STATUS_PENDING:
////                                Log.d("TAGO", "STATUS PENDING.");
////                                break;
////                        }
//
////                        String type = getSmsType(status, context);
//
//                        String name = getContactDisplayNameByNumber(msg_from, context);
////                        postNewMessageToServer(new MySmsLog(name, msg_from, date, ));
//                    }
//                }catch(Exception e){
//                    Log.d("TAGO", e.getMessage());
//                }
//            }
        }
    }

    private void smsReceived(Context context){
        Uri smsUri = Uri.parse("content://sms");

        // get sms for today
        Cursor c = context.getContentResolver().query(smsUri, null, "date>=?", new String[]{Long.toString(getDateHourAgo())}, null);
        String phNumber = null;

        if(c == null){
            Log.d("TAGO", "c==null esim xi");
            return;
        }

        try {
            if(c.moveToFirst()){
                String type = c.getString(c.getColumnIndex("type"));
                String body = c.getString(c.getColumnIndex("body"));
                String smsDate = c.getString(c.getColumnIndex("date"));
                phNumber = c.getString(c.getColumnIndex("address"));   //this is phone number rather than address

                String contactName = getContactDisplayNameByNumber(phNumber, context);

                Date date = convertStringToMyDate(smsDate);

                int typeCode = Integer.parseInt(type);
                String direction = "";
                //get the right direction
                switch (typeCode) {
                    case Telephony.Sms.MESSAGE_TYPE_INBOX:
                        direction = context.getString(R.string.incoming);
                        break;

                    case Telephony.Sms.MESSAGE_TYPE_OUTBOX:
                        direction = context.getString(R.string.outgoing);
                        break;

                    case Telephony.Sms.MESSAGE_TYPE_SENT:
                        direction = context.getString(R.string.sent);
                        break;

                    default:
                        direction = context.getString(R.string.unknown);
                        break;
                }

                postNewMessageToServer(new MySmsLog(contactName, phNumber, date, direction, body), context);
            }
            c.close();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            Log.d("TAGO", "Null pointer exception: " + ex);
        }

    }

    private void postNewMessageToServer(final MySmsLog message, final Context context){
        ChildData childData = new ChildData();
        ArrayList<MySmsLog> list = new ArrayList<>();
        list.add(message);
        childData.setSmsLogs(list);

        service.uploadData(childData).enqueue(new Callback<ResponseModel<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ResponseModel<Void>> call, @NonNull Response<ResponseModel<Void>> response) {
                if(response.body() == null){
                    Log.d("TAGO", "Response body is null");
                    saveToLocalDb(message, context);
                    return;
                }

                if(response.body().getErrors() != null){
                    Log.d("TAGO", "ERROR uploading sms: " + response.body().getErrors());
                    saveToLocalDb(message, context);
                    return;
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseModel<Void>> call, @NonNull Throwable t) {
                Log.d("TAGO", "Could not upload sms.");
                saveToLocalDb(message, context);
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private void saveToLocalDb(MySmsLog sms, final Context context){
        final ChildData childData = new ChildData();
        ArrayList<MySmsLog> smsLogs = new ArrayList<MySmsLog>();
        smsLogs.add(sms);
        childData.setSmsLogs(smsLogs);

        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                MyDao dao = MyDatabase.getDatabase(context).myDao();
                dao.insertChildDataInTransaction(childData);
                return null;
            }
        }.execute();
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
}
