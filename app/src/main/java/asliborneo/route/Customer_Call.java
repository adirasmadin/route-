package asliborneo.route;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import asliborneo.route.Model.Notification;
import asliborneo.route.Model.Token;
import asliborneo.route.Model.fcm_response;
import asliborneo.route.Model.sender;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static asliborneo.route.Driver_Home.mlastlocation;

public class Customer_Call extends AppCompatActivity {
    TextView txt_time, txt_distance, txt_Address;
    Button cancel_btn,accept_btn;
    MediaPlayer mediaPlayer;
    private PolylineOptions polylineOptions, blackpolylineoptions;
    private Polyline blackpolyline, greypolyline;
    private ArrayList<LatLng> polylinelist;
    String Customer_id;
    double lat,lng;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_call);
        txt_time = findViewById(R.id.txt_time);
        txt_Address = findViewById(R.id.txt_Address);
        txt_distance = findViewById(R.id.txt_distance);
        polylinelist = new ArrayList<>();
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
        mediaPlayer = MediaPlayer.create(this, R.raw.ringtone);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
        if (getIntent() != null) {
            lat = getIntent().getDoubleExtra("lat", -1.0);
            lng = getIntent().getDoubleExtra("lng", -1.0);
            Customer_id=getIntent().getStringExtra("customer");
            getDirection(lat, lng);
        }
    }

    private void cancel_booking(String customer_id) {
        Token token=new Token(customer_id);
        Notification notification=new Notification("Notice!","Driver has canceled your Request");
        sender sender=new sender(notification,token.getToken());
        FCMService fcmService =RetrofitClient.getClient().create(FCMService.class);
        Call<fcm_response> call=fcmService.send_message(sender);
        call.enqueue(new Callback<fcm_response>() {
            @Override
            public void onResponse(Call<fcm_response> call, Response<fcm_response> response) {
                if(response.body().success==1){
                    Toast.makeText(Customer_Call.this,"Canceled",Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<fcm_response> call, Throwable t) {
                Log.e("fcm_err",t.getMessage());
            }
        });
    }

    private void getDirection(double lat,double lng) {
        IGoogleAPI service=RetrofitClient.get_direction_client().create(IGoogleAPI.class);
        Call<Directions> call=service.getPath("driving","less_driving" ,mlastlocation.getLatitude()+","+mlastlocation.getLongitude(),lat+","+lng,"AIzaSyBtssbTB-BQGwYZaCNgS9GScZLW13TQK-U");
        call.enqueue(new Callback<Directions>() {
            @Override
            public void onResponse(Call<Directions> call, Response<Directions> response) {
                if(response.body()!=null){
                    if( response.body().routes.size()>0)
                        if(response.body().routes.get(0).legs.size()>0) {
                            txt_distance.setText(response.body().routes.get(0).legs.get(0).distance.text);
                            txt_Address.setText(response.body().routes.get(0).legs.get(0).end_address);
                            txt_time.setText(response.body().routes.get(0).legs.get(0).duration.text);
                        }
                }else{
                    Toast.makeText(Customer_Call.this,"Error in fetching location",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Directions> call, Throwable t) {
                Log.e("direction_error",t.getMessage());
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
}