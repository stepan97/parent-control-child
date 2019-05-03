package com.example.userasef.parentcontrolappchild.network;

import com.example.userasef.parentcontrolappchild.data.ResponseModel;
import com.example.userasef.parentcontrolappchild.data.payload.ChildData;
import com.example.userasef.parentcontrolappchild.data.payload.ChildPayload;
import com.example.userasef.parentcontrolappchild.data.response.User;
import com.example.userasef.parentcontrolappchild.utils.ApiEndpoints;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface IChildService {
    @POST(ApiEndpoints.CHILDREN_LOGIN)
    Call<ResponseModel<User>> sendActivationCode(@Body ChildPayload childPayload);

    @POST(ApiEndpoints.CHILDREN_UPLOAD_CHILD_DATA)
    Call<ResponseModel<Void>> uploadData(@Body ChildData data);

    @POST(ApiEndpoints.SAVE_FIREBASE_TOKEN)
    Call<ResponseModel<Void>> saveFirebaseToken(@Body String token);
}
