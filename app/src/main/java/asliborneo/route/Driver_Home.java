package asliborneo.route;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.maps.android.SphericalUtil;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;
import asliborneo.route.Interfaces.locationListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import asliborneo.route.Messages.Errors;
import asliborneo.route.Messages.Message;
import asliborneo.route.Model.RouteDriver;
import asliborneo.route.Model.Token;
import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Driver_Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,OnMapReadyCallback {


    int PICK_IMAGE_REQUEST=9999;
    private GoogleMap mMap;
    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    Location location=null;
    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    DatabaseReference drivers;
    GoogleSignInAccount account;
    Marker mCurrent;
    MaterialAnimatedSwitch location_switch;
    SupportMapFragment mapFragment;

    private List<LatLng> polyLineList;
    private  Marker carMarker;
    private float v;
    private double lat,lng;
    Handler handler;
    private LatLng startPosition,endPosition,currentPosition;
    private int index,next;

    private PlaceAutocompleteFragment places;
    AutocompleteFilter typefilter;
    private String destination;
    private PolylineOptions polylineOptions,blackPolylineOptions;
    private Polyline blackPolyline;
    private com.google.android.gms.maps.model.Polyline greyPolyline;

    private IGoogleMAPApi mService;

    DatabaseReference onlineRef, currentUserRef;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    private static final String TAG = "Driver_Home";
    GeoFire geoFire;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;


    Runnable drawPathRunnable = new Runnable() {
        @Override
        public void run() {
            if (index < polyLineList.size() - 1) {
                index++;
                next = index + 1;
            }
            if (index < polyLineList.size() - 1) {
                startPosition = polyLineList.get(index);
                endPosition = polyLineList.get(next);


            }

            final ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(3000);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    v = valueAnimator.getAnimatedFraction();
                    lng = v * endPosition.longitude + (1 - v) * startPosition.longitude;
                    lat = v * endPosition.latitude + (1 - v) * startPosition.latitude;
                    LatLng newPos = new LatLng(lat, lng);
                    carMarker.setPosition(newPos);
                    carMarker.setAnchor(0.5f, 0.5f);
                    carMarker.setRotation(getBearing(startPosition, newPos));
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().
                            target(newPos).zoom(15.5f).build()
                    ));


                }
            });

            valueAnimator.start();
            handler.postDelayed(this,3000);

        }
    };


    private float getBearing(LatLng startPosition, LatLng endPosition) {
        double lat = Math.abs(startPosition.latitude - endPosition.latitude);
        double lng= Math.abs(startPosition.longitude - endPosition.longitude);

        if (startPosition.latitude < endPosition.latitude && startPosition.longitude < endPosition.longitude)
            return (float)(Math.toDegrees(Math.atan(lng/lat)));
        else if(startPosition.latitude >= endPosition.latitude && startPosition.longitude <endPosition.longitude)
            return (float) ((90-Math.toDegrees(Math.atan(lng/lat)))+90);
        else if (startPosition.latitude >= endPosition.latitude && startPosition.longitude >= endPosition.longitude)
            return (float) (Math.toDegrees(Math.atan(lng/lat))+180);
        else if(startPosition.latitude < endPosition.latitude && startPosition.longitude >= endPosition.longitude)
            return (float) ((90-Math.toDegrees(Math.atan(lng/lat)))+270);

        return -1;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        verifyGoogleAccount();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        View navigation_header_view=navigationView.getHeaderView(0);
        CircleImageView avatar= navigation_header_view.findViewById(R.id.avatar);
        TextView name= navigation_header_view.findViewById(R.id.driver_name);
        TextView rating= navigation_header_view.findViewById(R.id.rating);
        if (Commons.current_routeDriver !=null) {
            rating.setText(Commons.current_routeDriver.getRates());
            name.setText(Commons.current_routeDriver.getName());
            if (!TextUtils.isEmpty(Commons.current_routeDriver.getAvatarUrl())) {
                Picasso.with(Driver_Home.this).load(Commons.current_routeDriver.getAvatarUrl()).into(avatar);
            }
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId  = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }
        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
            }
        }
        firebaseStorage=FirebaseStorage.getInstance();
        storageReference=firebaseStorage.getReference();

        places = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.placetxt);

        places.setHint("Enter Your Location");
        location_switch = findViewById(R.id.location_switch);
        typefilter=new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .setTypeFilter(3)
                .build();
        drivers = FirebaseDatabase.getInstance().getReference("Drivers");


        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
            }
        }

        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(Account account) {
                onlineRef= FirebaseDatabase.getInstance().getReference().child(".info/connected");
                currentUserRef=FirebaseDatabase.getInstance().getReference("Drivers").child(account.getId());
                onlineRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        currentUserRef.onDisconnect().removeValue();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onError(AccountKitError accountKitError) {

            }
        });



        location_switch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(boolean isOnline) {
                if (isOnline) {
                    FirebaseDatabase.getInstance().goOnline();
                    buildLocationCallback();
                    buildLocationRequest();
                    mMap.setMyLocationEnabled(true);

                    fusedLocationProviderClient.requestLocationUpdates(mLocationRequest,locationCallback,Looper.myLooper());
                    geoFire = new GeoFire(drivers);

                    displayLocation();
                    Snackbar.make(mapFragment.getView(), "You are Online", Snackbar.LENGTH_SHORT).show();
                }else
                {
                    if (!location_switch.isChecked())
                        FirebaseDatabase.getInstance().goOffline();
                    mMap.clear();
                    stopLocationUpdates();
                    mMap.setMyLocationEnabled(false);


                    if (handler !=null)


                        handler.removeCallbacks(drawPathRunnable);

                    Snackbar.make(mapFragment.getView(), "You are Offline", Snackbar.LENGTH_SHORT).show();
                }
            }
        });


        polyLineList = new ArrayList<>();


        places.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                if(location_switch.isChecked()){

                    destination=place.getAddress().toString();
                    destination=destination.replace(" ","+");
                    mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title("Search Users this area").icon(BitmapDescriptorFactory.fromResource(R.drawable.des)));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15.0f));
                    getDirection();
                }else{
                    Message.messageError(getApplicationContext(), Errors.WITHOUT_LOCATION);
                }

            }

            @Override
            public void onError(Status status) {

            }
        });
        update_firebase_token();
        setupLocation();

        mService = Commons.getGoogleService();
    }


    private void loadUser(){
        FirebaseDatabase.getInstance().getReference(Commons.Registered_driver)
                .child(Commons.userID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Commons.current_routeDriver=dataSnapshot.getValue(RouteDriver.class);

                        loadDriverInformation();
                        onlineRef=FirebaseDatabase.getInstance().getReference().child(".info/connected");
                        currentUserRef=FirebaseDatabase.getInstance().getReference(Commons.driver_location).child(Commons.current_routeDriver.getCarType())
                                .child(Commons.userID);
                        onlineRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                currentUserRef.onDisconnect().removeValue();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadDriverInformation(){
        FirebaseDatabase.getInstance().getReference(Commons.Registered_driver)
                .child(Commons.userID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Commons.current_routeDriver = dataSnapshot.getValue(RouteDriver.class);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void verifyGoogleAccount() {
        GoogleSignInOptions gso=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        mGoogleApiClient=new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        OptionalPendingResult<GoogleSignInResult> opr=Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()){
            GoogleSignInResult result= opr.get();
            handleSignInResult(result);
        }else {
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }
    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            account = result.getSignInAccount();
            Commons.userID=account.getId();
            loadUser();
        }else{
            Commons.userID=FirebaseAuth.getInstance().getCurrentUser().getUid();
            loadUser();
        }
    }


    private void stopLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(Driver_Home.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // locationManager.removeUpdates(this);
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }


    private void getDirection()
    {
        currentPosition = new LatLng(Commons.mLastLocation.getLatitude(),Commons.mLastLocation.getLongitude());
        String requestApi = null;

        requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                "mode=driving&" +
                "transit_routing_preference=less_driving&" +
                "origin=" + currentPosition.latitude + "," + currentPosition.longitude + "&" +
                "destination=" +destination + "&" +
                "key=" + getResources().getString(R.string.map_server_api);
        Log.d("DIRECTION",requestApi);

        mService.getPath(requestApi).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                try {
                    JSONObject jsonObject = new JSONObject(response.body().toString());
                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                    for(int i=0; i<jsonArray.length(); i++)

                    {
                        JSONObject route = jsonArray.getJSONObject(i);
                        JSONObject poly = route.getJSONObject("overview_polyline");
                        String polyline = poly.getString("points");
                        polyLineList = decodePoly(polyline);
                    }

                    LatLngBounds.Builder builder=new LatLngBounds.Builder();
                    if(response.body() !=null )
                        for(LatLng latLng:polyLineList)
                            builder.include(latLng);
                    LatLngBounds bounds=builder.build();

                    CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,2);
                    mMap.animateCamera(mCameraUpdate);

                    polylineOptions = new PolylineOptions();
                    polylineOptions.color(Color.CYAN);
                    polylineOptions.width(10);
                    polylineOptions.startCap(new SquareCap());
                    polylineOptions.endCap(new SquareCap());
                    polylineOptions.jointType(JointType.ROUND);
                    polylineOptions.addAll(polyLineList);
                    greyPolyline = mMap.addPolyline(polylineOptions);

                    blackPolylineOptions = new PolylineOptions();
                    blackPolylineOptions.color(Color.CYAN);
                    blackPolylineOptions.width(10);
                    blackPolylineOptions.startCap(new SquareCap());
                    blackPolylineOptions.endCap(new SquareCap());
                    blackPolylineOptions.jointType(JointType.ROUND);
                    blackPolyline = mMap.addPolyline(blackPolylineOptions);

                    mMap.addMarker(new MarkerOptions().position(polyLineList.get(polyLineList.size()-1))
                            .title("Pickup Location"));

                    ValueAnimator polylineAnimator = ValueAnimator.ofInt(0,100);
                    polylineAnimator.setDuration(2000);
                    polylineAnimator.setInterpolator(new LinearInterpolator());
                    polylineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            List<LatLng> points = greyPolyline.getPoints();
                            int percentValue = (int) valueAnimator.getAnimatedValue();
                            int size = points.size();
                            int newPoints = (int) (size * (percentValue/100.0f));
                            List<LatLng> p = points.subList(0,newPoints);
                            blackPolyline.setPoints(p);

                        }
                    });
                    polylineAnimator.start();
                    carMarker = mMap.addMarker(new MarkerOptions().position(currentPosition)
                            .flat(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));

                    handler = new Handler();
                    index = -1;
                    next=1;
                    handler.postDelayed(drawPathRunnable,3000);



                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(Driver_Home.this, ""+t.getMessage(),Toast.LENGTH_LONG).show();
            }
        });

    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    private void update_firebase_token() {
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
                        Toast.makeText(Driver_Home.this,""+e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(AccountKitError accountKitError) {
                Log.d("ERROR ACCOUNTKIT",""+ accountKitError.getUserFacingMessage());
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    buildLocationCallback();
                    buildLocationRequest();
                    enableMyLocation();
                    if (location_switch.isChecked())
                        displayLocation();
                }
        }
    }
    private void setupLocation() {
        if (ActivityCompat.checkSelfPermission(Driver_Home.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Driver_Home.this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);

        } else {
            buildLocationRequest();
            buildLocationCallback();
            enableMyLocation();
            if (location_switch.isChecked()) {
                drivers= FirebaseDatabase.getInstance().getReference(Commons.Registered_driver).child(Commons.driver_location).child(Commons.current_routeDriver.getCarType());
                geoFire=new GeoFire(drivers);

                displayLocation();
            }
        }

    }

    private void buildLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);

    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback()
        {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                for (Location location:locationResult.getLocations())
                {
                    Commons.mLastLocation = location;
                }
                displayLocation();
            }
        };

    }


    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(Driver_Home.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (Commons.mLastLocation != null) {
                    if (location_switch.isChecked()) {
                        final double longitude = Commons.mLastLocation.getLongitude();
                        final double latitude = Commons.mLastLocation.getLatitude();
                        LatLng center = new LatLng(latitude, longitude);
                        LatLng northside = SphericalUtil.computeOffset(center, 100000, 0);
                        LatLng southside = SphericalUtil.computeOffset(center, 100000, 180);
                        LatLngBounds bounds = LatLngBounds.builder()
                                .include(northside)
                                .include(southside)
                                .build();
                        places.setBoundsBias(bounds);
                        places.setFilter(typefilter);
                        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                            @Override
                            public void onSuccess(Account account) {
                                geoFire.setLocation(account.getId(), new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                                    @Override
                                    public void onComplete(String key, DatabaseError error) {
                                        if (mCurrent != null) {
                                            mCurrent.remove();
                                        }
                                        LatLng currentLocation = new LatLng(latitude, longitude);
                                        mCurrent = mMap.addMarker(new MarkerOptions().position(currentLocation));
                                      mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15.0f));
                                        // rotate_marker(mcurrent, -360, mMap);*/
                                    }
                                });
                            }

                            @Override
                            public void onError(AccountKitError accountKitError) {

                            }
                        });

                    }
                } else {
                    Log.d("ERROR", "Cannot get your location");
                }
            }
        });
    }



        private void rotate_marker(final Marker mcurrent, final float i, GoogleMap mMap) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final float start_rotation = mcurrent.getRotation();
        final long duration = 1500;
        final Interpolator interpolator = new LinearInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elasped = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elasped / duration);
                float rot = t * i + (1 - t) * start_rotation;
                mcurrent.setRotation(-rot > 180 ? rot / 2 : rot);
                if (t < 1.0)
                    handler.postDelayed(this, 16);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap=googleMap;
        try {
            boolean issucess = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(Driver_Home.this, R.raw.uber_style_map));
            if (!issucess)
                Toast.makeText(Driver_Home.this, "Error setting Map Style", Toast.LENGTH_LONG).show();
        }catch(Resources.NotFoundException ex){ex.printStackTrace();}
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(true);
        mMap.setBuildingsEnabled(true);
        mMap.setIndoorEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        buildLocationRequest();
        buildLocationCallback();


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.driver_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_update_cartype) {

            showDialogUpdateCarType();

        } else if (id == R.id.nav_signout) {
            Sign_Out();


        } else if (id==R.id.nav_update_profile) {
            show_update_profile_dialog();
        }
        else if (id==R.id.nav_update_makepayment) {
            showMakePayment();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(Driver_Home.this, MY_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }


    private void showMakePayment() {
        android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(Driver_Home.this);
        alertDialog.setTitle("UPDATE VEHICLE TYPE");
        LayoutInflater inflater = this.getLayoutInflater();
        View carType = inflater.inflate(R.layout.custom_popup, null);
        final RadioButton rbUberX=carType.findViewById(R.id.default_cartype);
        final RadioButton rbUberBlack=carType.findViewById(R.id.teksi_cartype);
        final RadioButton cash=carType.findViewById(R.id.cash);
        if (geoFire !=null)
            if(Commons.current_routeDriver.getMakePayment().equals("TOPUP E-WALLET (minimum RM10)"))
                rbUberX.setChecked(true);
            else if(Commons.current_routeDriver.getMakePayment().equals("MONTHLY SUBSCRIPTION (RM300)"))
                rbUberBlack.setChecked(true);
            else if(Commons.current_routeDriver.getMakePayment().equals("MONTHLY SUBSCRIPTION (RM300)"))
                cash.setChecked(true);
        alertDialog.setView(carType);
        alertDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(Account account) {
                        Map<String, Object> updateInfo=new HashMap<>();
                        if(rbUberX.isChecked())
                            updateInfo.put("makePayment", rbUberX.getText().toString());
                        else if(rbUberBlack.isChecked())
                            updateInfo.put("makePayment", rbUberBlack.getText().toString());

                        DatabaseReference driverInformation = FirebaseDatabase.getInstance().getReference(Commons.Registered_driver);
                        driverInformation.child(account.getId())
                                .updateChildren(updateInfo)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if(task.isSuccessful())
                                            Toast.makeText(Driver_Home.this,"Payment success!",Toast.LENGTH_SHORT).show();
                                        else
                                            Toast.makeText(Driver_Home.this,"Payment failed!",Toast.LENGTH_SHORT).show();

                                    }
                                });
                        driverInformation.child(account.getId())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Commons.current_routeDriver=dataSnapshot.getValue(RouteDriver.class);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                    }

                    @Override
                    public void onError(AccountKitError accountKitError) {

                    }
                });


            }
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }


    private void showDialogUpdateCarType() {
        android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(Driver_Home.this);
        alertDialog.setTitle("UPDATE VEHICLE TYPE");
        LayoutInflater inflater = this.getLayoutInflater();
        View carType = inflater.inflate(R.layout.layout_update_cartype, null);
        final RadioButton rbUberX=carType.findViewById(R.id.economy);
        final RadioButton rbUberBlack=carType.findViewById(R.id.luxury);
        if (geoFire !=null)
        if(Commons.current_routeDriver.getCarType().equals("Economy"))
            rbUberX.setChecked(true);
        else if(Commons.current_routeDriver.getCarType().equals("Luxury"))
            rbUberBlack.setChecked(true);

        alertDialog.setView(carType);
        alertDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                @Override
                public void onSuccess(Account account) {
                    Map<String, Object> updateInfo=new HashMap<>();
                    if(rbUberX.isChecked())
                        updateInfo.put("carType", rbUberX.getText().toString());
                    else if(rbUberBlack.isChecked())
                        updateInfo.put("carType", rbUberBlack.getText().toString());

                    DatabaseReference driverInformation = FirebaseDatabase.getInstance().getReference(Commons.Registered_driver);
                    driverInformation.child(account.getId())
                            .updateChildren(updateInfo)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful())
                                        Toast.makeText(Driver_Home.this,"Information Updated!",Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(Driver_Home.this,"Information Update Failed!",Toast.LENGTH_SHORT).show();

                                }
                            });
                    driverInformation.child(account.getId())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Commons.current_routeDriver=dataSnapshot.getValue(RouteDriver.class);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                }

                @Override
                public void onError(AccountKitError accountKitError) {

                }
            });


            }
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }

    private void show_update_profile_dialog() {
        AlertDialog.Builder update_profile_dialog=new AlertDialog.Builder(Driver_Home.this);
        update_profile_dialog.setTitle("Update Profile");
        update_profile_dialog.setMessage("Please Fill all Information");
        View v=LayoutInflater.from(Driver_Home.this).inflate(R.layout.update_profile_layout,null);
        final MaterialEditText name= v.findViewById(R.id.nametxt);
        final MaterialEditText phone= v.findViewById(R.id.phonetxt);
        ImageView image_upload= v.findViewById(R.id.image_upload);
        image_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choose_image();
            }
        });
        update_profile_dialog.setView(v);
        update_profile_dialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final AlertDialog waiting_dialog=new SpotsDialog(Driver_Home.this);
                waiting_dialog.show();

                if(!TextUtils.isEmpty(name.getText().toString())&&!TextUtils.isEmpty(phone.getText().toString())) {
                    AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                        @Override
                        public void onSuccess(Account account) {
                            Map<String,Object> namephoneupdate=new HashMap<>();

                            namephoneupdate.put("name", name.getText().toString());
                            namephoneupdate.put("phone", phone.getText().toString());

                            DatabaseReference driver_information_reference=FirebaseDatabase.getInstance().getReference(Commons.Registered_driver);
                            driver_information_reference.child(account.getId()).updateChildren(namephoneupdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        waiting_dialog.dismiss();
                                        Toast.makeText(Driver_Home.this,"Name and Phone are updated",Toast.LENGTH_LONG).show();
                                    }else{
                                        waiting_dialog.dismiss();
                                        Toast.makeText(Driver_Home.this,"Update Failed",Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }

                        @Override
                        public void onError(AccountKitError accountKitError) {

                        }
                    });
                }else{
                    Toast.makeText(Driver_Home.this,"Please Provide Required Information",Toast.LENGTH_LONG).show();
                }
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();

    }

    private void choose_image() {
        Intent intent =new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture for Profile"),PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==PICK_IMAGE_REQUEST&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null) {
            Uri saveuri=data.getData();
            if(saveuri!=null){
                final ProgressDialog dialog=new ProgressDialog(Driver_Home.this);
                dialog.setMessage("Uploading.....");
                dialog.setCancelable(false);
                dialog.show();
                String image_id= UUID.randomUUID().toString();
                final StorageReference image_folder=storageReference.child("images/"+image_id);
                image_folder.putFile(saveuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        dialog.dismiss();
                        Toast.makeText(Driver_Home.this,"Uploaded!",Toast.LENGTH_LONG).show();
                        image_folder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(final Uri uri) {
                                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                                    @Override
                                    public void onSuccess(Account account) {
                                        Map<String,Object> avatar_update=new HashMap<>();
                                        avatar_update.put("avatarUrl",uri.toString());
                                        DatabaseReference driver_information_reference=FirebaseDatabase.getInstance().getReference(Commons.Registered_driver);
                                        driver_information_reference.child(account.getId()).updateChildren(avatar_update).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(Driver_Home.this,"Uploaded!",Toast.LENGTH_LONG).show();
                                                }else{
                                                    Toast.makeText(Driver_Home.this,"Upload Failed",Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    }

                                    @Override
                                    public void onError(AccountKitError accountKitError) {

                                    }
                                });
                            }
                        });
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress=(100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                        dialog.setMessage("Uploading "+progress+"%");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Driver_Home.this,e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }



    private void Sign_Out() {

        android.support.v7.app.AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.LOLLIPOP)
            builder = new android.support.v7.app.AlertDialog.Builder(this,android.R.style.Theme_Material_Dialog_Alert);

        else
            builder = new android.support.v7.app.AlertDialog.Builder(this);

        builder.setMessage("Are you sure ?")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AccountKit.logOut();
                        Intent intent = new Intent(Driver_Home.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

}