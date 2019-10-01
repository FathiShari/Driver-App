package com.example.courier;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.example.courier.Commom.Common;
import com.example.courier.Helper.DirectionJSONParser;
import com.example.courier.RetrofitController.IFCMService;
import com.example.courier.RetrofitController.IGoogleApi;
import com.example.courier.model.FCMResponse;
import com.example.courier.model.Notification;
import com.example.courier.model.Sender;
import com.example.courier.model.Token;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.google.android.gms.location.LocationServices.FusedLocationApi;

public class DriverTracking extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener , LocationListener,GoogleApiClient.ConnectionCallbacks {

    private GoogleMap mMap;
    double riderLat, riderlng;
    String customerId;

    private static final int play_service_request_code = 7001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mApiCLient;

    private int update_interval = 5000;
    private int fastest_interval = 3000;
    private int displacement = 10;

    private Circle riderMarker;
    private Marker driverMarker;
    private Polyline direction;

    IGoogleApi mServices;
    IFCMService ifcmService;

    GeoFire geofire;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_tracking);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mServices = Common.getAPI();
        ifcmService = Common.getfcmService();

        if (getIntent() != null) {
            riderLat = getIntent().getDoubleExtra("lat", -1.0);
            riderlng = getIntent().getDoubleExtra("lng", -1.0);
            customerId = getIntent().getStringExtra("customerid");
        }

        setUpLocation();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        riderMarker = mMap.addCircle(new CircleOptions().center(new LatLng(riderLat, riderlng))
                .radius(50) // 50km radius
                .strokeColor(Color.BLUE)
                .fillColor(Color.GRAY)
                .strokeWidth(5.0f));

        //Create geo fire with radius 50km
        geofire = new GeoFire(FirebaseDatabase.getInstance().getReference(Common.diver_location_table));
        GeoQuery geoQuery = geofire.queryAtLocation(new GeoLocation(riderLat,riderlng),0.05f);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                //here we will send customer id to send notification
                sendArrivedNotification(customerId);
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void sendArrivedNotification(String customerId) {
        Token token = new Token(customerId);
        Notification notification =  new Notification("Arrived",String.format("The driver has arrived at your location" ,Common.currentUser.getName()));
        Sender sender = new Sender(notification,token.getToken());

        ifcmService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if(response.body().success != 1)
                    Toast.makeText(DriverTracking.this,"Failed",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });
    }

    private void startLocationUpdates() {
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != (PackageManager.PERMISSION_GRANTED)) &&
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != (PackageManager.PERMISSION_GRANTED))) {
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mApiCLient, mLocationRequest,this);
    }

    private void displayLocation() {
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != (PackageManager.PERMISSION_GRANTED)) &&
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != (PackageManager.PERMISSION_GRANTED))) {
            return;
        }

        Common.mLastLocation = FusedLocationApi.getLastLocation(mApiCLient);
        if (Common.mLastLocation != null) {

            final double latitude = Common.mLastLocation.getLatitude();
            final double longtude = Common.mLastLocation.getLongitude();

            if (driverMarker != null)
                driverMarker.remove();
            driverMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longtude))
                    .title("You")
                    .icon(BitmapDescriptorFactory.defaultMarker()));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longtude), 17.0f));

         if (direction != null)
                direction.remove();
            getDirection();

        } else {
            Log.d("Error", "Cannot get your Location");
        }

    }

    private void getDirection() {
        LatLng currentPosition = new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude());
        String requestApi = null;

        try {

            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"
                    + "mode=driving&" +
                    "transit_routing_preference=less_driving&" +
                    "origin=" + currentPosition.latitude + "," + currentPosition.longitude + "&" + "destination=" + riderLat + " " + riderlng + "&"
                    + "key=" + getResources().getString(R.string.google_maps_api);
            Log.d("CourierDebug", requestApi);

            mServices.getPath(requestApi).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {
                         new ParserTask().execute(response.body().toString());

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(DriverTracking.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(DriverTracking.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpLocation() {

        if (checkPlayServices()) {

            buildGoogleApiClient();
            createLocationequest();
            displayLocation();
        }
    }

    private void createLocationequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(update_interval);
        mLocationRequest.setFastestInterval(fastest_interval);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(displacement);
    }

    private void buildGoogleApiClient() {
        mApiCLient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this).addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mApiCLient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, play_service_request_code).show();
            else {
                Toast.makeText(this, "This Device is not Supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mApiCLient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Common.mLastLocation = location;
        displayLocation();
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        ProgressDialog mbuilder = new ProgressDialog(DriverTracking.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mbuilder.setMessage("waiting....");
            mbuilder.show();
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObect;
            List<List<HashMap<String, String>>> route = null;

            try {
                jsonObect = new JSONObject(strings[0]);
                DirectionJSONParser jsonParser = new DirectionJSONParser();
                route = jsonParser.parse(jsonObect);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return route;

        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            mbuilder.dismiss();

            ArrayList points = null;
            PolylineOptions polylineOptions = null;

            for(int i=0; i<lists.size(); i++){

                points =new ArrayList();
                polylineOptions =  new PolylineOptions();

                List<HashMap<String,String>> path = lists.get(i);

                for(int j=0; j<path.size(); j++){

                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));

                    LatLng position = new LatLng(lat,lng);
                    points.add(position);


                }

                polylineOptions.addAll(points);
                polylineOptions.width(10);
                polylineOptions.color(Color.RED);
                polylineOptions.geodesic(true);
            }

            direction = mMap.addPolyline(polylineOptions);
        }
    }
}