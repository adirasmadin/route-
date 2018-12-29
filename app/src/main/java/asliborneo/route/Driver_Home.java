package asliborneo.route;

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
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
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
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.maps.android.SphericalUtil;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import asliborneo.route.Model.Token;
import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Driver_Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,OnMapReadyCallback, com.google.android.gms.location.LocationListener,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {


    int PICK_IMAGE_REQUEST=9999;
    private GoogleMap mMap;
    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleapiclient;

    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    DatabaseReference drivers;
    GeoFire geofire;
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
        if (Commons.current_user !=null) {
            rating.setText(Commons.current_user.getRates());
            name.setText(Commons.current_user.getName());
            if (!TextUtils.isEmpty(Commons.current_user.getAvatarurl())) {
                Picasso.with(Driver_Home.this).load(Commons.current_user.getAvatarurl()).into(avatar);
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
        places = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.placetxt);
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

        location_switch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(boolean isOnline) {
                if (isOnline) {
                    FirebaseDatabase.getInstance().goOnline();
                    startLocationUpdates();
                    displayLocation();
                    Snackbar.make(mapFragment.getView(), "You are Online", Snackbar.LENGTH_SHORT).show();
                }else
                {

                    stopLocationUpdates();
                    FirebaseDatabase.getInstance().goOffline();
                    if(mCurrent!=null)
                        mCurrent.remove();
                   mMap.clear();
                    handler.removeCallbacks(drawPathRunnable);
                  
                    Snackbar.make(mapFragment.getView(), "You are Offline", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        
        polyLineList = new ArrayList<>();
        drivers = FirebaseDatabase.getInstance().getReference(Commons.driver_location);
        geofire = new GeoFire(drivers);
     
        places.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                if(location_switch.isChecked()){

                    destination=place.getAddress().toString();
                    destination=destination.replace(" ","+");
                        mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title("Search Users this area").icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker)));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15.0f));
                    getDirection();
                }else{
                    Toast.makeText(Driver_Home.this,"Please Change your status to Online",Toast.LENGTH_LONG).show();
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
       AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
           @Override
           public void onSuccess(Account account) {
               FirebaseDatabase db=FirebaseDatabase.getInstance();
               DatabaseReference tokens=db.getReference(Commons.tokenTable);
               Token token=new Token(FirebaseInstanceId.getInstance().getToken());
               tokens.child(account.getId()).setValue(token);
           }

           @Override
           public void onError(AccountKitError accountKitError) {

           }
       });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (location_switch.isChecked())
                        displayLocation();
                }
        }
    }
    private void setupLocation() {
        if (ActivityCompat.checkSelfPermission(Driver_Home.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Driver_Home.this, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);

        } else {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
                if (location_switch.isChecked())
                    displayLocation();
            }
        }
    }
    private void stopLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(Driver_Home.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // locationManager.removeUpdates(this);
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleapiclient,this);
    }
    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(Driver_Home.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Commons.mLastLocation= LocationServices.FusedLocationApi.getLastLocation(mGoogleapiclient);
        if (Commons.mLastLocation != null) {
            if (location_switch.isChecked()) {
                final double longitude = Commons.mLastLocation.getLongitude();
                final double latitude = Commons.mLastLocation.getLatitude();
                LatLng center=new LatLng(latitude,longitude);
                LatLng northside= SphericalUtil.computeOffset(center,100000,0);
                LatLng southside= SphericalUtil.computeOffset(center,100000,180);
                LatLngBounds bounds=LatLngBounds.builder()
                        .include(northside)
                        .include(southside)
                        .build();
                places.setBoundsBias(bounds);
                places.setFilter(typefilter);

              AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                  @Override
                  public void onSuccess(Account account) {
                      geofire.setLocation(account.getId(), new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                          @Override
                          public void onComplete(String key, DatabaseError error) {
                              if (mCurrent != null) {
                                  mCurrent.remove();
                              }
                              mCurrent=mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("You").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
                              mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude),15.0f));
                              // rotate_marker(mcurrent, -360, mMap);*/
                          }
                      });
                  }

                  @Override
                  public void onError(AccountKitError accountKitError) {

                  }
              });
            }
        }
        else
        {
            Log.d("ERROR","Cannot get your location");
        }
    }
    private void createLocationRequest(){
        mLocationRequest=new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setInterval(UPDATE_INTERVAL);

    }
    private void buildGoogleApiClient(){
        mGoogleapiclient=new GoogleApiClient.Builder(Driver_Home.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleapiclient.connect();
    }


    private boolean checkPlayServices()
    {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode !=ConnectionResult.SUCCESS)
        {
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICES_RESOLUTION_REQUEST).show();

            else
            {
                Toast.makeText(this, "This device is not supported", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(Driver_Home.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleapiclient,mLocationRequest,this);
        // locationManager.requestLocationUpdates(Provider,20000,0,this);
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
        mMap.setTrafficEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);

    }
    @Override
    public void onLocationChanged(Location location) {
        Commons.mLastLocation =location;
        displayLocation();
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }
    @Override
    public void onConnectionSuspended(int i) {
    mGoogleapiclient.connect();
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("location_error",connectionResult.getErrorMessage());
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

        if (id == R.id.nav_Trip_history) {

        } else if (id == R.id.nav_way_bill) {

        } else if (id == R.id.nav_help) {

        } else if (id == R.id.nav_setting) {

        } else if (id == R.id.nav_signout) {
            Sign_Out();

        }else if(id== R.id.nav_change_password){
            show_change_password_dialog();
        } else if (id==R.id.nav_update_profile) {
            show_update_profile_dialog();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
                       DatabaseReference driver_information_reference=FirebaseDatabase.getInstance().getReference("DriverInformation");
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
                                      avatar_update.put("avatarurl",uri.toString());
                                      DatabaseReference driver_information_reference=FirebaseDatabase.getInstance().getReference("DriverInformation");
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

    private void show_change_password_dialog() {
        AlertDialog.Builder change_password_dialog=new AlertDialog.Builder(Driver_Home.this);
        change_password_dialog.setTitle("Change Password");
        change_password_dialog.setMessage("Please fill all information");
        View v=LayoutInflater.from(Driver_Home.this).inflate(R.layout.change_password_layout,null);
        final MaterialEditText new_password= v.findViewById(R.id.new_password_txt);
        final MaterialEditText old_password= v.findViewById(R.id.old_password_txt);
        final MaterialEditText repeat_new_password= v.findViewById(R.id.repeat_new_password_txt);
        change_password_dialog.setView(v);
        change_password_dialog.setPositiveButton("Change Password", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final AlertDialog waiting_dialog=new SpotsDialog(Driver_Home.this);
                waiting_dialog.show();
                if(new_password.getText().toString().equals(repeat_new_password.getText().toString())){
                    String email=FirebaseAuth.getInstance().getCurrentUser().getEmail();
                    AuthCredential credinal= EmailAuthProvider.getCredential(email,old_password.getText().toString());
                    FirebaseAuth.getInstance().getCurrentUser().reauthenticate(credinal).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                FirebaseAuth.getInstance().getCurrentUser().updatePassword(repeat_new_password.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                           AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                                               @Override
                                               public void onSuccess(Account account) {
                                                   Map<String,Object> password=new HashMap<>();
                                                   password.put("password",repeat_new_password.getText().toString());
                                                   DatabaseReference driver_information_reference=FirebaseDatabase.getInstance().getReference("DriverInformation");
                                                   driver_information_reference.child(account.getId()).updateChildren(password).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                       @Override
                                                       public void onComplete(@NonNull Task<Void> task) {
                                                           if (task.isSuccessful()){
                                                               waiting_dialog.dismiss();
                                                               Toast.makeText(Driver_Home.this,"Password has changed",Toast.LENGTH_LONG).show();
                                                           }else{
                                                               waiting_dialog.dismiss();
                                                               Toast.makeText(Driver_Home.this,"Password was cchanged but not updated in Database",Toast.LENGTH_LONG).show();
                                                           }
                                                       }
                                                   });
                                               }

                                               @Override
                                               public void onError(AccountKitError accountKitError) {

                                               }
                                           });

                                        }else{
                                            waiting_dialog.dismiss();
                                            Toast.makeText(Driver_Home.this,"Password has not Changed due to some Error",Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }else{
                                waiting_dialog.dismiss();
                                Toast.makeText(Driver_Home.this,"Old Password is incorrect",Toast.LENGTH_LONG).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });

                }else{
                    waiting_dialog.dismiss();
                    Toast.makeText(Driver_Home.this,"Passwords do not match",Toast.LENGTH_LONG).show();

                }
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }

    private void Sign_Out() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.LOLLIPOP)
            builder = new AlertDialog.Builder(this,android.R.style.Theme_Material_Dialog_Alert);

        else
            builder = new AlertDialog.Builder(this);

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
