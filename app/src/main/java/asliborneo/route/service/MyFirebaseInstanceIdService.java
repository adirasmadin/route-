package asliborneo.route.service;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import asliborneo.route.Model.Token;

import static android.support.constraint.Constraints.TAG;

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {
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
