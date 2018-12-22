package asliborneo.route.service;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.Objects;

import asliborneo.route.Customer_Call;

import static android.support.constraint.Constraints.TAG;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        LatLng Customer_location=new Gson().fromJson(remoteMessage.getNotification().getBody(),LatLng.class);
        Intent intent=new Intent(getBaseContext(), Customer_Call.class);
        intent.putExtra("lat",Customer_location.latitude);
        intent.putExtra("lng",Customer_location.longitude);
        intent.putExtra("customer",remoteMessage.getNotification().getTitle());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK  |Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);


        startActivity(intent);

    }


}