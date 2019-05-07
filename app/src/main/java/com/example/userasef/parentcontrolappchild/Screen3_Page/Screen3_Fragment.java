package com.example.userasef.parentcontrolappchild.Screen3_Page;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.userasef.parentcontrolappchild.MainActivity;
import com.example.userasef.parentcontrolappchild.R;
import com.example.userasef.parentcontrolappchild.kayfo.SmsObserver;
import com.example.userasef.parentcontrolappchild.services.MyService;
import com.example.userasef.parentcontrolappchild.view.Loader;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class Screen3_Fragment extends Fragment implements IScreen3_Contract.View
{

    private Button submit_btn;
    private Loader loader;
    private IScreen3_Contract.Presenter mPresenter;

    public static Screen3_Fragment newInstance(){
        Bundle args = new Bundle();

        Screen3_Fragment fragment = new Screen3_Fragment();
        fragment.setArguments(args);
        return fragment;
    }

    public Screen3_Fragment(){
        // default constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.screen3, container, false);

        initView(view);
        initListeners();
        mPresenter = new Screen3_Presenter(this, getContext());

        return view;
    }

    private void initView(View view){
        submit_btn = view.findViewById(R.id.submit_btn);
        loader = view.findViewById(R.id.loader);
    }

    private void initListeners(){
        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.submit_btn){
                    hideApp();
//                    mPresenter.sendFirebaseToken();
                }
            }
        });
    }

    private void hideApp(){


//        Toast.makeText(getContext(), "App Hidden", Toast.LENGTH_SHORT).show();
        Log.d("TAGO", "App hidden");

        getActivity().startService(new Intent(getActivity(), MyService.class));



        // make app invisible
//        PackageManager packageManager = getActivity().getPackageManager();
//
//        ComponentName componentName = new ComponentName(getActivity(), MainActivity.class);
//
//        packageManager.setComponentEnabledSetting(componentName,
//                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
//
//        getActivity().finish();
    }

    @Override
    public void showErrorMessage(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setLoaderVisibility(int visibility) {
        loader.setVisibility(visibility);
    }

    @Override
    public void goToNextPage() {
        hideApp();
    }
}
