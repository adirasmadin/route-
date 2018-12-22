package asliborneo.route;

import android.animation.ValueAnimator;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import asliborneo.route.Model.FCMResponse;
import asliborneo.route.Model.Notification;
import asliborneo.route.Model.Token;
import asliborneo.route.Model.Sender;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static asliborneo.route.Commons.fcmURL;


public class Customer_Call extends AppCompatActivity {
    TextView txtTime, txtDistance, txtAddress;
    Button cancel_btn,accept_btn;
    MediaPlayer mediaPlayer;

    FCMService mFCMService;
    IGoogleMAPApi mService;

    String Customer_id;
    double lat,lng;
    private static final String TAG = "Customer_Call";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_call);
        txtTime = findViewById(R.id.txt_time);
        txtAddress = findViewById(R.id.txt_address);
        txtDistance = findViewById(R.id.txt_distance);

        mediaPlayer = MediaPlayer.create(this, R.raw.ringtone);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();




        cancel_btn= findViewById(R.id.cancel_btn);
        accept_btn= findViewById(R.id.accept_btn);
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TextUtils.isEmpty(Customer_id)){
                    cancel_booking(Customer_id);
                }
            }
        });
        accept_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Customer_Call.this,DriverTracking.class);
                intent.putExtra("lat",lat);
                intent.putExtra("lng",lng);
                intent.putExtra("customer",Customer_id);
                startActivity(intent);
                finish();
            }
        });


        mService = Commons.getGoogleService();
        mFCMService = Commons.getFCMService();




        if (getIntent() != null) {


            lat = getIntent().getDoubleExtra("lat", -1.0);
            lng = getIntent().getDoubleExtra("lng", -1.0);
            Customer_id=getIntent().getStringExtra("customer");
            getDirection(lat,lng);
        }

    }

    private void getDirection(double lat, double lng)
    {

        String requestApi = null;

        requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                "mode=driving&" +
                "transit_routing_preference=less_driving&" +
                "origin=" + Commons.mLastLocation.getLatitude() + "," + Commons.mLastLocation.getLongitude() + "&" +
                "destination=" +lat + ","+ lng + "&" +
                "key=" + getResources().getString(R.string.google_direction_api);
        Log.d("DIRECTION",requestApi);

        mService.getPath(requestApi).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.body() != null) {

                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        JSONArray routes = jsonObject.getJSONArray("routes");

                        JSONObject object = routes.getJSONObject(0);

                        JSONArray legs = object.getJSONArray("legs");

                        JSONObject legsObject = legs.getJSONObject(0);

                        JSONObject distance = legsObject.getJSONObject("distance");
                        txtDistance.setText(distance.getString("text"));

                        JSONObject time = legsObject.getJSONObject("duration");
                        txtTime.setText(time.getString("text"));

                        String address = legsObject.getString("end_address");
                        txtAddress.setText(address);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
            Toast.makeText(Customer_Call.this, ""+t.getMessage(),Toast.LENGTH_LONG).show();
            }
        });

    }

    private void cancel_booking(String customer_id) {
        Token token=new Token(customer_id);
        Notification notification=new Notification("Notice!","Driver has canceled your Request");
        Sender sender=new Sender(notification,token.getToken());

        mFCMService.sendMessage(sender).
        enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if ( response.body().success == 1) {
                    Toast.makeText(Customer_Call.this, "Cancelled", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Customer_Call.this,Driver_Home.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {
                Log.e("fcm_err",t.getMessage());
            }
        });
    }



    @Override
    protected void onStop() {
        mediaPlayer.release();
        super.onStop();
    }

    @Override
    protected void onPause() {
        mediaPlayer.release();
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mediaPlayer.start();

    }

}