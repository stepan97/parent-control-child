package com.example.userasef.parentcontrolappchild.kayfo;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.util.Log;

import com.example.userasef.parentcontrolappchild.R;
import com.example.userasef.parentcontrolappchild.data.payload.MySmsLog;

import java.util.Date;

import static com.example.userasef.parentcontrolappchild.utils.MyDateUtils.convertStringToMyDate;
import static com.example.userasef.parentcontrolappchild.utils.MyDateUtils.getDateHourAgo;

public class SmsObserver extends ContentObserver {
    private Context mContext;
    private static final Uri SMS_STATUS_URI = Uri.parse("content://sms");

    public SmsObserver(Handler handler, Context ctx) {
        super(handler);
        mContext = ctx;
    }
    
    public boolean deliverSelfNotifications() {
        return true;
    }

    public void onChange(boolean selfChange) {
        try{
            Log.e("Info","Notification on SMS observer");
            Cursor c = mContext.getContentResolver().query(SMS_STATUS_URI, null, "date>=?", new String[]{Long.toString(getDateHourAgo())}, null);
            if(c.moveToFirst()){
                String type = c.getString(c.getColumnIndex("type"));
                String body = c.getString(c.getColumnIndex("body"));
                String smsDate = c.getString(c.getColumnIndex("date"));
                String phNumber = c.getString(c.getColumnIndex("address"));   //this is phone number rather than address

                String contactName = getContactDisplayNameByNumber(phNumber);

                Date date = convertStringToMyDate(smsDate);

                int typeCode = Integer.parseInt(type);
                String direction = "";
                //get the right direction
                switch (typeCode) {
                    case Telephony.Sms.MESSAGE_TYPE_INBOX:
                        direction = mContext.getString(R.string.incoming);
                        break;

                    case Telephony.Sms.MESSAGE_TYPE_OUTBOX:
                        direction = mContext.getString(R.string.outgoing);
                        break;

                    case Telephony.Sms.MESSAGE_TYPE_SENT:
                        direction = mContext.getString(R.string.sent);
                        break;

                    default:
                        direction = mContext.getString(R.string.unknown);
                        break;
                }

                postNewMessageToServer(new MySmsLog(contactName, phNumber, date, direction, body));
            }
            c.close();
        }
        catch(Exception sggh){
            Log.e("Error", "Error on onChange : "+sggh.toString());
        }
        super.onChange(selfChange);
    }

    private void postNewMessageToServer(MySmsLog mySmsLog) {
        Log.d("TAGO", "UPloaded sms to server.");
    }

    /**
     * @param number phone number
     * @return the name matched with the phone number
     */
    private String getContactDisplayNameByNumber(String number) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String name = mContext.getString(R.string.no_name);

        ContentResolver contentResolver = mContext.getContentResolver();
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
