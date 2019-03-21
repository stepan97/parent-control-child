package com.example.userasef.parentcontrolappchild.network;

import com.example.userasef.parentcontrolappchild.controller.DataController;
import com.example.userasef.parentcontrolappchild.utils.ApiEndpoints;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChildClient {
    private static Retrofit retrofit = null;

    public static Retrofit getClient(){
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();
                        if(DataController.getInstance().getUser() != null){
                            request = request.newBuilder().addHeader("x-auth-token", DataController.getInstance().getUser().getAccessToken()).build();
                        }
                        Response response = chain.proceed(request);

                        return response;
                    }
                })
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(ApiEndpoints.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit;
    }
}
