package asliborneo.route;

import android.content.Intent;
import android.support.constraint.Constraints;
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

import static android.support.constraint.Constraints.TAG;


public class Service {
    public static class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {
        @Override
        public void onTokenRefresh() {
            super.onTokenRefresh();
            String refreshedtoken= FirebaseInstanceId.getInstance().getToken();
            Updateservertoken(refreshedtoken);

            Log.d(TAG, "Refreshed token: " + refreshedtoken);
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

            Log.d(TAG, "From: " + remoteMessage.getFrom());
            if (remoteMessage.getData().size() > 0) {
                Log.d(TAG, "Message data payload: " + remoteMessage.getData());

                if (/* Check if data needs to be processed by long running job */ true) {
                    // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                    scheduleJob();
                } else {
                    // Handle message within 10 seconds
                    handleNow();
                }

            }
            if (remoteMessage.getNotification() != null) {
                Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            }
            LatLng Customer_location=new Gson().fromJson(Objects.requireNonNull(remoteMessage.getNotification()).getBody(),LatLng.class);
            Intent intent=new Intent(getBaseContext(), Customer_Call.class);
            intent.putExtra("lat",Customer_location.latitude);
            intent.putExtra("lng",Customer_location.longitude);
            intent.putExtra("customer",remoteMessage.getNotification().getTitle());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        private void handleNow() {

        }

        private void scheduleJob() {

        }
    }
}
