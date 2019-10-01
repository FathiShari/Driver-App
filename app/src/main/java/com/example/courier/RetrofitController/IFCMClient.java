package com.example.courier.RetrofitController;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class IFCMClient {
    private static Retrofit retrofit = null;

    public static Retrofit getClient(String baseUrl){
        if(retrofit == null){

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }
}
