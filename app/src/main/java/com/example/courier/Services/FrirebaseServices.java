package com.example.courier.Services;

import android.content.Intent;
import android.util.Log;

import com.example.courier.Commom.Common;
import com.example.courier.CustomerCall;
import com.example.courier.model.Token;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import static android.support.constraint.Constraints.TAG;

public class FrirebaseServices extends FirebaseMessagingService {
    @Override
    public void onNewToken(String s) {

        super.onNewToken(s);
        Log.d(TAG, "Refreshed token: " + s);

        updateTokenToServer(s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        LatLng customer_location = new Gson().fromJson(remoteMessage.getNotification().getBody(),LatLng.class);

        Intent intent = new Intent(getBaseContext(), CustomerCall.class);
        intent.putExtra("lat",customer_location.latitude);
        intent.putExtra("lng",customer_location.longitude);
        intent.putExtra("customer",remoteMessage.getNotification().getTitle());
        startActivity(intent);

    }

    private void updateTokenToServer(String refreshedToken) {
        FirebaseDatabase db=  FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference(Common.token_table);

        Token token = new Token(refreshedToken);
        if(FirebaseAuth.getInstance().getCurrentUser() != null)
            tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
            .setValue(token);
    }
}
