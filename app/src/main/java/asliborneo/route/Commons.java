package asliborneo.route;

import android.location.Location;

import asliborneo.route.Model.RouteDriver;

public class Commons {
    public static Location mLastLocation;
    public static String Current_Token;
    public static final String driver_location="Drivers";
    public static final String Registered_driver="DriverInformation";
    public static final String Registered_Riders="RidersInformation";
    public static final String pickUpRequest_tbl="PickUpRequest";
    public static final String tokenTable ="Tokens";
    public static double base_fare=2.50;
    private static double time_rate=0.25;
    private static double distance_rate=0.50;
    public static RouteDriver current_routeDriver =null;
    public static final String user_field="usr";
    public static final String password_field="pwd";
    public static final String googleAPIUrl ="https://maps.googleapis.com";
    public static String fcmURL = "https://fcm.googleapis.com/";

    public static double price_formula(double km,double min){
        return base_fare+(distance_rate*km)+(time_rate*min);
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