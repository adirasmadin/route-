package asliborneo.route;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import asliborneo.route.Model.DataMessage;
import asliborneo.route.Model.FCMResponse;
import asliborneo.route.Model.RouteDriver;
import asliborneo.route.Model.Token;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DriverTracking extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient.OnConnectionFailedListener,GoogleApiClient.ConnectionCallbacks,LocationListener,GoogleMap.OnMyLocationClickListener,GoogleMap.OnMyLocationButtonClickListener {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private  static final int PLAY_SERVICE_RESOLUTION_REQUEST = 7001;

    private static GoogleMap mMap;
    String riderLat,riderLng;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleapiClient;
    private Circle rider_marker;
    private Marker driver_marker;
    private Polyline direction;
    GeoFire geoFire;
    Button start_trip_btn;
    Location pick_up_location;
    IGoogleMAPApi mService;
    FCMService mFCMService;
    String customer_id;



    private static int UPDATE_INTERVAL =5000;
    private static int FASTEST_INTERVAL=3000;
    private static int DISPLACEMENT = 10;




    Marker destination_location_marker;
    private static final String TAG = "DriverTracking";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_tracking);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        start_trip_btn= findViewById(R.id.start_trip);
        if (getIntent()!=null){
            riderLat=getIntent().getStringExtra("lat");
            riderLng=getIntent().getStringExtra("lng");
            customer_id = getIntent().getStringExtra("customer");
        }


        mService =Commons.getGoogleService();
        mFCMService = Commons.getFCMService();


        start_trip_btn= findViewById(R.id.start_trip);
        start_trip_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseInstanceId.getInstance().getInstanceId()
                        .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                            @Override
                            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                if (!task.isSuccessful()) {
                                    Log.w(TAG, "getInstanceId failed", task.getException());
                                    return;
                                }

                                // Get new Instance ID token
                                String token = task.getResult().getToken();

                                // Log and toast
                                String msg = getString(R.string.msg_token_fmt, token);
                                Log.d(TAG, msg);
                                Toast.makeText(DriverTracking.this, msg, Toast.LENGTH_SHORT).show();
                            }
                        });
                if(start_trip_btn.getText().toString().equals("Start Trip")){
                    pick_up_location=Commons.mLastLocation;
                    start_trip_btn.setText("Drop off Here");
                }else  if(start_trip_btn.getText().toString().equals("Drop off Here")){
                    calculateCashFee(pick_up_location,Commons.mLastLocation);
                }
            }
        });

        enableMyLocation();


        setupLocation();

        DatabaseReference drivers = FirebaseDatabase.getInstance().getReference(Commons.driver_location);
        geoFire = new GeoFire(drivers);
        UpdateserverToken();
    }
    private void UpdateserverToken() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        final DatabaseReference tokens = db.getReference("Tokens");


        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(final Account account) {
                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        Token token = new Token(instanceIdResult.getToken());
                        tokens.child(account.getId())
                                .setValue(token);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("ERROR TOKEN", e.getMessage());
                        Toast.makeText(DriverTracking.this,""+e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(AccountKitError accountKitError) {
                Log.d("ERROR ACCOUNTKIT", accountKitError.getUserFacingMessage());
            }
        });
    }
    private boolean checkPlayServices()
    {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode !=ConnectionResult.SUCCESS)
        {
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICE_RESOLUTION_REQUEST).show();

            else
            {
                Toast.makeText(this, "This device is not supported", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }
    private void setupLocation() {


        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();

            displayLocation();
        }
    }

    private void send_dropoff_notification(String customer_id) {
        Token token=new Token(customer_id);
//        Notification notification=new Notification("Drop Off",customer_id);
//
//        Sender sender=new Sender(notification,token.getToken());

        Map<String ,String > content = new HashMap<>();
        content.put("title","Drop Off");
        content.put("message",customer_id);
        DataMessage dataMessage = new DataMessage(token.getToken(),content);
        mFCMService.sendMessage(dataMessage)
                .enqueue(new Callback<FCMResponse>() {
                    @Override
                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                        if(response.body() !=null)
                            if(response.body().success!=1){
                                Toast.makeText(DriverTracking.this,"Failed",Toast.LENGTH_LONG).show();
                            }

                    }

                    @Override
                    public void onFailure(Call<FCMResponse> call, Throwable t) {
                        Log.e("fcm_problem",t.toString());
                    }
                });
    }

    private void calculateCashFee(final Location pickupLocation, Location mLastLocation) {


        String requestApi = null;


        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "transit_routing_preference=less_driving&" +
                    "origin=" + pickupLocation.getLatitude() + "," + pickupLocation.getLongitude() + "&" +
                    "destination=" + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude() + "&" +
                    "key=" + getResources().getString(R.string.map_server_api);


            mService.getPath(requestApi).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {

                        JSONObject jsonObject = new JSONObject(response.body());
                        JSONArray routes = jsonObject.getJSONArray("routes");

                        JSONObject object = routes.getJSONObject(0);

                        JSONArray legs = object.getJSONArray("legs");
                        JSONObject legsObject = legs.getJSONObject(0);

                        JSONObject distance = legsObject.getJSONObject("distance");
                        String distance_text = distance.getString("text");

                        Double distance_value = Double.parseDouble(distance_text.replaceAll("[^0-9\\\\.]+", ""));

                        JSONObject time = legsObject.getJSONObject("duration");
                        String time_text = time.getString("text");
                        Integer time_value = Integer.parseInt(time_text.replaceAll("\\D+", ""));


                        send_dropoff_notification(customer_id);
                        Intent intent = new Intent(DriverTracking.this, Trip_Detail.class);
                        intent.putExtra("start_address", legsObject.getString("start_address"));
                        intent.putExtra("end_address", legsObject.getString("end_address"));
                        intent.putExtra("time", String.valueOf(time_value));
                        intent.putExtra("total",String.valueOf(Commons.price_formula(distance_value,time_value,100)));
                        intent.putExtra("distance", String.valueOf(distance_value));
                        intent.putExtra("total", Commons.price_formula(distance_value, time_value,0.2));
                        intent.putExtra("location_start", String.format("%f,%f", pickupLocation.getLatitude(), pickupLocation.getLongitude()));
                        intent.putExtra("location_end", String.format("%f,%f", Commons.mLastLocation.getLatitude(),Commons.mLastLocation.getLongitude()));

                        startActivity(intent);
                        finish();


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(DriverTracking.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void stop_location_updates() {
        if (ActivityCompat.checkSelfPermission(DriverTracking.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // locationManager.removeUpdates(this);
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleapiClient,this);
    }
    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(DriverTracking.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Commons.mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleapiClient);
        if (Commons.mLastLocation != null) {

            final double longitude = Commons.mLastLocation.getLongitude();
            final double latitude = Commons.mLastLocation.getLatitude();
            if(driver_marker!=null)
                driver_marker.remove();
            driver_marker=mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)).title("You").icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude),17.0f));
            if(direction!=null)
                direction.remove();
            getDirection();
        }
    }


    private void getDirection() {
        LatLng currentPosition = new LatLng(Commons.mLastLocation.getLatitude(),Commons.mLastLocation.getLongitude());
        String requestApi = null;

        requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                "mode=driving&" +
                "transit_routing_preference=less_driving&" +
                "origin=" + currentPosition.latitude + "," + currentPosition.longitude + "&" +
                "destination=" +riderLat + "," + riderLng + "&" +
                "key=" + getResources().getString(R.string.map_server_api);
        Log.d("DIRECTION",requestApi);

        mService.getPath(requestApi).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                try {

                    new ParserTask().execute(response.body().toString());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(DriverTracking.this, ""+t.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    private void createLocationRequest(){
        mLocationRequest=new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(10);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setInterval(5000);

    }
    private void buildGoogleApiClient(){
        mGoogleapiClient=new GoogleApiClient.Builder(DriverTracking.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleapiClient.connect();
    }
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(DriverTracking.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleapiClient,mLocationRequest,this);
        // locationManager.requestLocationUpdates(Provider,20000,0,this);
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.

        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap=googleMap;

        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setTrafficEnabled(true);
        mMap.setIndoorEnabled(true);
        mMap.setBuildingsEnabled(true);
        try {
            boolean issucess = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(DriverTracking.this, R.raw.uber_style_map));
            if (!issucess)
                Toast.makeText(DriverTracking.this, "Error setting Map Style", Toast.LENGTH_LONG).show();
        }catch(Resources.NotFoundException ex){ex.printStackTrace();}


        rider_marker=mMap.addCircle(new CircleOptions()
                .center(new LatLng(Double.parseDouble(riderLat),Double.parseDouble(riderLng)))
                .radius(50)
                .strokeColor(Color.TRANSPARENT).fillColor(0x220000FF)
                .strokeWidth(5.0f));
        destination_location_marker=mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(riderLat),Double.parseDouble(riderLng))).title("PickUp Here").icon(BitmapDescriptorFactory.fromResource(R.drawable.des)));
        geoFire=new GeoFire(FirebaseDatabase.getInstance().getReference(Commons.driver_location));
        GeoQuery geoQuery=geoFire.queryAtLocation(new GeoLocation(Double.parseDouble(riderLat),Double.parseDouble(riderLng)),0.05f);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                send_arrived_notification(getIntent().getStringExtra("customer"));
                start_trip_btn.setEnabled(true);
            getIntent();
                getDirection();

            }

            @Override
            public void onKeyExited(String key) {



            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void send_arrived_notification(String customer ) {
        Token token=new Token(customer);
//        Notification notification=new Notification(String.format("Arrived Notification"),"Hello there, i'm at somewhere near your location");
//
//        Sender sender=new Sender(notification,token.getToken());
        Map<String,String> content = new HashMap<>();
        content.put("title","Arrived Notification");

        content.put("message", "Hello there, I'm somewhere near your location");
        DataMessage dataMessage = new DataMessage(token.getToken(),content);


        mFCMService.sendMessage(dataMessage)
                .enqueue(new Callback<FCMResponse>() {
                    @Override
                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                        if(response.body() !=null)
                            if(response.body().success!=1){
                                Toast.makeText(DriverTracking.this,"Failed",Toast.LENGTH_LONG).show();
                            }else{
                                Toast.makeText(DriverTracking.this,"Success",Toast.LENGTH_LONG).show();
                            }
                        Log.e("arrival_notification",response.toString());
                    }

                    @Override
                    public void onFailure(Call<FCMResponse> call, Throwable t) {
                        Log.e("fcm_problem",t.toString());
                    }
                });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleapiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("location_error",connectionResult.getErrorMessage());
    }

    @Override
    public void onLocationChanged(Location location) {
        Commons.mLastLocation=location;
        displayLocation();
    }


    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {

    }



    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>>> {
        ProgressDialog mDialog = new ProgressDialog(DriverTracking.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected List<List<HashMap<String,String>>> doInBackground(String... strings) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(strings[0]);
                DirectionJSONParser parser = new DirectionJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String,String>>> lists) {
            mDialog.dismiss();
            ArrayList<LatLng> points = null;
            PolylineOptions polylineOptions = null;


            for (int i = 0; i < lists.size(); i++) {
                points = new ArrayList<>();
                polylineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = lists.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap< String, String> point = path.get(j);


                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }



                polylineOptions.addAll(points);
                polylineOptions.width(16);
                polylineOptions.color(Color.CYAN);
                polylineOptions.geodesic(true);


                polylineOptions.color(ContextCompat.getColor(getApplicationContext(),R.color.colorPrimary));



            }


            direction= mMap.addPolyline(polylineOptions);
        }
    }

}