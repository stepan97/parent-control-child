package com.example.userasef.parentcontrolappchild.Screen3_Page;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.example.userasef.parentcontrolappchild.data.ResponseModel;
import com.example.userasef.parentcontrolappchild.data.response.User;
import com.example.userasef.parentcontrolappchild.network.ChildClient;
import com.example.userasef.parentcontrolappchild.network.IChildService;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Screen3_Presenter implements IScreen3_Contract.Presenter {

    private IScreen3_Contract.View mView;
    private Context context;
    private static IChildService service;

    public Screen3_Presenter(IScreen3_Contract.View mView, Context context){
        this.mView = mView;
        this.context = context;
        service = ChildClient.getClient().create(IChildService.class);
    }

    @Override
    public void sendFirebaseToken() {
        mView.setLoaderVisibility(View.VISIBLE);


        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        String newToken = instanceIdResult.getToken();
                        Log.d("TAGO", "TOKEN: " + newToken);

                        service.saveFirebaseToken(newToken).enqueue(new Callback<ResponseModel<Void>>() {
                            @Override
                            public void onResponse(@NonNull Call<ResponseModel<Void>> call, @NonNull Response<ResponseModel<Void>> response) {
                                if(response.body().getErrors() != null){
                                    mView.showErrorMessage(response.body().getErrors());
                                    return;
                                }

                                mView.goToNextPage();
                            }

                            @Override
                            public void onFailure(@NonNull Call<ResponseModel<Void>> call, @NonNull Throwable t) {
                                mView.showErrorMessage("Something failed. Please, try again later.");
                            }
                        });
                    }
                });
    }
}
