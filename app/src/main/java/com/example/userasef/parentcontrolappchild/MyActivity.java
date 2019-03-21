package com.example.userasef.parentcontrolappchild;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.userasef.parentcontrolappchild.utils.ActivityUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MyActivity extends AppCompatActivity {

    Button insertButton;
    TextView message_tv;
    TextView messages_TextView;

    FirebaseDatabase mDatabase;
    DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        initView();

        mDatabase = FirebaseDatabase.getInstance();
        myRef = mDatabase.getReference("messages");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                messages_TextView.setText(value);
                Log.d("TAGO", "MESSAGES: " + value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("TAGO", "Failed to read value.", databaseError.toException());
            }
        });
    }

    private void insertData(String message){
//        myRef.setValue(message);
        myRef.push().setValue(message);
    }

    private void initView(){
        insertButton = findViewById(R.id.insert_btn);
        message_tv = findViewById(R.id.message_tv);
        messages_TextView = findViewById(R.id.messages_text);

        insertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = message_tv.getText().toString();
                if(TextUtils.isEmpty(msg)){
                    Log.i("TAGO", "Message Text is EMPTY");
                    return;
                }

                insertData(msg);
            }
        });
    }

    @Override
    public void onBackPressed() {
        ActivityUtil.goToPreviousFragment(getSupportFragmentManager());
    }
}
