package com.example.userasef.parentcontrolappchild;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.userasef.parentcontrolappchild.Screen1_Page.Screen1_Fragment;
import com.example.userasef.parentcontrolappchild.utils.ActivityUtil;

public class MainActivity extends AppCompatActivity {

//    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 10;
//    private static final int MY_PERMISSIONS_REQUEST_PROCESS_OUTGOING_CALLS = 20;
//    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 30;
//    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 30;
//    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_NUMBERS = 40;
//    private static final int MY_PERMISSIONS_REQUEST_RECEIVE_SMS = 50;
    private static final int MY_PERMISSIONS_ALLTOGETHER = 123;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("TAGO", "hopar");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d("TAGO", "Version is greater that M. Requiring permissions.");
            kayfo();
        } else {
            Log.d("TAGO", "Version is lower that M");
        }

        ActivityUtil.pushFragment(Screen1_Fragment.newInstance(), getSupportFragmentManager(), R.id.fragment_container_main, true);
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private void kayfo() {
        // request all permissions at once
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.PROCESS_OUTGOING_CALLS,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.READ_SMS,
                        Manifest.permission.INTERNET,
                        Manifest.permission.GET_ACCOUNTS,
                        Manifest.permission.RECEIVE_SMS

                },
                MY_PERMISSIONS_ALLTOGETHER);

//        if (getApplicationContext().checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
//                != PackageManager.PERMISSION_GRANTED) {
//            // Permission has not been granted, therefore prompt the user to grant permission
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.READ_PHONE_STATE},
//                    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
//        }
//
//        if (getApplicationContext().checkSelfPermission(Manifest.permission.PROCESS_OUTGOING_CALLS)
//                != PackageManager.PERMISSION_GRANTED) {
//            // Permission has not been granted, therefore prompt the user to grant permission
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.PROCESS_OUTGOING_CALLS},
//                    MY_PERMISSIONS_REQUEST_PROCESS_OUTGOING_CALLS);
//        }
//
//
//        if(getApplication().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED){
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
//        }
//
//        if(getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED){
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
//        }
//
//        if(getApplicationContext().checkSelfPermission(Manifest.permission.READ_PHONE_NUMBERS)
//                != PackageManager.PERMISSION_GRANTED){
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.READ_PHONE_STATE},
//                    MY_PERMISSIONS_REQUEST_READ_PHONE_NUMBERS);
//        }
//
//        if(getApplicationContext().checkSelfPermission(Manifest.permission.RECEIVE_SMS)
//                != PackageManager.PERMISSION_GRANTED){
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.RECEIVE_SMS},
//                    MY_PERMISSIONS_REQUEST_RECEIVE_SMS);
//        }
        // 2182203046
    }
}
