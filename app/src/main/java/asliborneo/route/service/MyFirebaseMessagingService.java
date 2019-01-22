package asliborneo.route.service;

import android.content.Intent;
import android.util.Log;

import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.Map;
import java.util.Objects;

import asliborneo.route.Customer_Call;
import asliborneo.route.Model.Token;
import asliborneo.route.R;

import static android.support.constraint.Constraints.TAG;
import static java.lang.String.format;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
       UpdateserverToken(s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getData() != null) {

            Map<String, String> data = remoteMessage.getData();
            String customer_id = data.get("customer");
            String lat = data.get("lat");
            String lng = data.get("lng");




            Intent intent = new Intent(getBaseContext(), Customer_Call.class);
            intent.putExtra("lat", lat);
            intent.putExtra("lng", lng);

            intent.putExtra("customer",customer_id);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);


            startActivity(intent);

        }
    }
    private void UpdateserverToken(final String refreshedToken) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        final DatabaseReference tokens = db.getReference("Tokens");
        final Token token = new Token(refreshedToken);

        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(Account account) {
            tokens.child(account.getId())
                    .setValue(token);
            }

            @Override
            public void onError(AccountKitError accountKitError) {
        Log.d("ERROR ACCOUNTKIT", ""+accountKitError.getUserFacingMessage());
            }
        });
    }


}