package com.example.courier;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.courier.Commom.Common;
import com.example.courier.RetrofitController.IFCMService;
import com.example.courier.RetrofitController.IGoogleApi;
import com.example.courier.model.FCMResponse;
import com.example.courier.model.Notification;
import com.example.courier.model.Sender;
import com.example.courier.model.Token;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerCall extends AppCompatActivity {

    TextView txtTime,txtAddress,txtDistance;
    Button btnAccept,btnDecline;
    double lat,lng;

    //Alert driver
    MediaPlayer mediaPlayer;
    IGoogleApi mservices;
    IFCMService ifcmService;

    //Cnacel/Accept request
    String customerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_call);

        txtTime = (TextView)findViewById(R.id.txt_time);
        txtAddress = (TextView)findViewById(R.id.txt_address);
        txtDistance = (TextView)findViewById(R.id.txt_distance);

        btnAccept = (Button)findViewById(R.id.btn_accept);
        btnDecline = (Button)findViewById(R.id.btn_decline);

        mservices = Common.getAPI();
        ifcmService = Common.getfcmService();

        mediaPlayer = MediaPlayer.create(this,R.raw.ringtone);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        btnDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(customerId)){
                    cancelBooking(customerId);
                }
            }
        });

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomerCall.this,DriverTracking.class);

                intent.putExtra("lat",lat);
                intent.putExtra("lng",lng);
                intent.putExtra("customerid",customerId);
                startActivity(intent);
                finish();
            }
        });

        if(getIntent() != null){

             lat = getIntent().getDoubleExtra("lat",-1.0);
             lng = getIntent().getDoubleExtra("lng",-1.0);
             customerId = getIntent().getStringExtra("customer");

            getDirection(lat,lng);
        }
    }

    private void cancelBooking(String customerId) {
        Token token = new Token(customerId);
        Notification notification = new Notification("Cancel","Driver has decline your request");
        Sender sender = new Sender(notification,token.getToken());

        ifcmService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if(response.body().success == 1){
                    Toast.makeText(CustomerCall.this,"CANCELLED",Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });
    }

    private void getDirection(double lat,double lng) {

        String requestApi = null;

        try {

            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"
                    + "mode=driving&" +
                    "transit_routing_preference=less_driving&" +
                    "origin=" +Common.mLastLocation.getLatitude()+ "," + Common.mLastLocation.getLongitude() + "&" + "destination=" +lat+","+lng + "&"
                    + "key=" + getResources().getString(R.string.google_maps_api);

            Log.d("Courier", requestApi);

            mservices.getPath(requestApi).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {

                        JSONObject jsonObject = new JSONObject(response.body().toString());

                        JSONArray  routes = jsonObject.getJSONArray("routes");

                        //after getroutes,just get 1st elememt of routes
                        JSONObject object = routes.getJSONObject(0);

                        //after get elements, we need array with name "legs'
                        JSONArray legs = object.getJSONArray("legs");

                        //and get first element of legs array
                        JSONObject legsobject = legs.getJSONObject(0);

                        //Now get distance
                        JSONObject distnce = legsobject.getJSONObject("distance");
                        txtDistance.setText(distnce.getString("text"));

                        //get time
                        JSONObject time = legsobject.getJSONObject("duration");
                        txtTime.setText(time.getString("text"));

                        //get Address
                        String address = legsobject.getString("end_address");
                        txtAddress.setText(address);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(CustomerCall.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(CustomerCall.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        mediaPlayer.release();
        super.onStop();
    }

    @Override
    protected void onPause() {
        mediaPlayer.release();
        super.onPause();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        mediaPlayer.start();
    }
}
