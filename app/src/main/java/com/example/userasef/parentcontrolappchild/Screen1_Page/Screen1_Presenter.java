package com.example.userasef.parentcontrolappchild.Screen1_Page;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.example.userasef.parentcontrolappchild.R;
import com.example.userasef.parentcontrolappchild.controller.DataController;
import com.example.userasef.parentcontrolappchild.data.ResponseModel;
import com.example.userasef.parentcontrolappchild.data.payload.ChildPayload;
import com.example.userasef.parentcontrolappchild.data.response.User;
import com.example.userasef.parentcontrolappchild.network.ChildClient;
import com.example.userasef.parentcontrolappchild.network.IChildService;
import com.example.userasef.parentcontrolappchild.utils.Constants;
import com.example.userasef.parentcontrolappchild.utils.PreferencesUtils;
import com.google.gson.Gson;

import java.net.ConnectException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Screen1_Presenter implements IScreen1_Contract.Presenter {

    private IScreen1_Contract.View mView;
    private Context context;
    private static IChildService service;

    public Screen1_Presenter(IScreen1_Contract.View mView, Context context){
        this.mView = mView;
        this.context = context;
        service = ChildClient.getClient().create(IChildService.class);
    }

    @Override
    public void sendActivationCode(@NonNull ChildPayload childPayload) {

        mView.setLoaderVisibility(View.VISIBLE);

        if(service == null)
            service = ChildClient.getClient().create(IChildService.class);

        service.sendActivationCode(childPayload).enqueue(new Callback<ResponseModel<User>>() {
            @Override
            public void onResponse(@NonNull Call<ResponseModel<User>> call,@NonNull Response<ResponseModel<User>> response) {

                if(response.body().getErrors() != null){
                    mView.showErrorMessage(response.body().getErrors());
                    return;
                }

                User user = response.body().getData();
                Gson gson = new Gson();
                Log.d("TAGO", "accessToken: " + user.getAccessToken());
                PreferencesUtils.putString(context, Constants.USER_GLOBAL, gson.toJson(user));
                DataController.getInstance().setUser(user);
                mView.goToNextPage();

                mView.setLoaderVisibility(View.GONE);
            }

            @Override
            public void onFailure(@NonNull Call<ResponseModel<User>> call,@NonNull Throwable t) {
                if(t instanceof ConnectException){
                    mView.showErrorMessage(context.getString(R.string.no_internet));
                }

                mView.setLoaderVisibility(View.GONE);
            }
        });
    }
}
