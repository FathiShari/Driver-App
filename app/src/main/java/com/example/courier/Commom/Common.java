package com.example.courier.Commom;

import android.location.Location;

import com.example.courier.RetrofitController.IFCMClient;
import com.example.courier.RetrofitController.IFCMService;
import com.example.courier.RetrofitController.IGoogleApi;
import com.example.courier.RetrofitController.RetrofitCient;
import com.example.courier.model.User;

public class Common {

    public static final String driver_table_info = "DriverInformation";
    public static final String rider_table_info = "RiderInformation";
    public static final String diver_location_table = "DriverLocation";
    public static final String pickup_request_table = "PickupRequestRider";
    public static final String driver_vehicles_table = "VehicleInformation";
    public static final String token_table = "Tokeninfo";

    public  static Location mLastLocation = null;
    public static User currentUser;

    public static final String baseURl= "https://maps.googleapis.com";
    public static final String fcmURl= "https://fcm.googleapis.com/";


    public static IGoogleApi getAPI(){

        return RetrofitCient.getClient(baseURl).create(IGoogleApi.class);
    }

    public static IFCMService getfcmService(){

        return IFCMClient.getClient(fcmURl).create(IFCMService.class);
    }
}
