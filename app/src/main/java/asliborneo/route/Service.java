package asliborneo.route;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.Objects;

import asliborneo.route.Model.Token;



public class Service {
    public static class MyFirebaseIdService extends FirebaseInstanceIdService {
        @Override
        public void onTokenRefresh() {
            super.onTokenRefresh();
            String refreshedtoken= FirebaseInstanceId.getInstance().getToken();
            Updateservertoken(refreshedtoken);
        }



        private void Updateservertoken(String refreshedtoken) {
            FirebaseDatabase db=FirebaseDatabase.getInstance();
            DatabaseReference tokens=db.getReference("Tokens");
            Token token=new Token(refreshedtoken);
            if(FirebaseAuth.getInstance().getCurrentUser() !=null)
                tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);
        }
    }

    public static class MyFirebaseMessagingService extends FirebaseMessagingService {
        @Override
        public void onMessageReceived(RemoteMessage remoteMessage) {
            super.onMessageReceived(remoteMessage);
            LatLng Customer_location=new Gson().fromJson(Objects.requireNonNull(remoteMessage.getNotification()).getBody(),LatLng.class);
            Intent intent=new Intent(getBaseContext(), Customer_Call.class);
            intent.putExtra("lat",Customer_location.latitude);
            intent.putExtra("lng",Customer_location.longitude);
            intent.putExtra("customer",remoteMessage.getNotification().getTitle());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
}
