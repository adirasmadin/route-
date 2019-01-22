package asliborneo.route;

import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;

import com.facebook.accountkit.AccountKit;

import asliborneo.route.Model.RouteDriver;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;

import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class Commons {
    public static final int PICK_IMAGE_REQUEST = 999;
    public static Location mLastLocation;
    public static String Current_Token;
    public static final String driver_location="Drivers";
    public static final String Registered_driver="DriverInformation";
    public static final String Registered_Riders="RidersInformation";
    public static final String pickUpRequest_tbl="PickUpRequest";
    public static final String tokenTable ="Tokens";
    public static double bal= 0;
    public static double km,min;
    public static double time_rate=0.25;
    public static double distance_rate=0.50;
    public static double base_fare = 2.50;
    public static double routefee = 2;
    public static Double currentLat;
    public static Double currentLng;

    public static String userID ="";


    public static RouteDriver current_routeDriver =null;
    public static final String user_field="usr";
    public static final String password_field="pwd";
    public static final String googleAPIUrl ="https://maps.googleapis.com";
    public static String fcmURL = "https://fcm.googleapis.com/";






    public static double price_formula(double km,double min,double routefee){
        return   (base_fare + ((distance_rate*km)+(time_rate*min)) -(routefee*(0.2)));
    }
    public static IGoogleMAPApi getGoogleService()
    {
        return GoogleMAPApi.getClient(googleAPIUrl).create(IGoogleMAPApi.class);
    }
    public static FCMService getFCMService()
    {
        return FCMClient.getClient(fcmURL).create(FCMService.class);
    }


 IGoogleAPI getGoogleAPI()
    {
        return RetrofitClient.getClient(fcmURL).create(IGoogleAPI.class);
    }
}