package dev.eder.oreogeofence.geofences;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar;

import java.util.Random;

import dev.eder.oreogeofence.R;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;

    private static final int MY_PERMISSION_REQUEST_CODE = 56;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 97;


    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVA = 3000;
    private static int DISPLACEMENT = 10;

    private static final String TAG = MapsActivity.class.getSimpleName();

    DatabaseReference databaseReference;

    GeoFire geoFire;

    Marker mCurrent;

    VerticalSeekBar mSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        databaseReference = FirebaseDatabase.getInstance().getReference("MyLocation");
        geoFire = new GeoFire(databaseReference);
        mSeekBar = findViewById(R.id.verticalSeekBar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mMap.animateCamera(CameraUpdateFactory.zoomTo(progress),2000,null);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        setUpLocation();
    }

    private void setUpLocation() {
         if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) !=
                 PackageManager.PERMISSION_GRANTED &&
                 ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) !=
                         PackageManager.PERMISSION_GRANTED){
             ActivityCompat.requestPermissions(this,new String[]{
                     Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION
             },MY_PERMISSION_REQUEST_CODE);
         }else{
             if (checkPlayServices()){
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();
             }
         }
    }

    private void displayLocation() {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED){
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation!=null){
            final double lat = mLastLocation.getLatitude();
            final double longitude = mLastLocation.getLongitude();

            geoFire.setLocation("You", new GeoLocation(lat, longitude), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    //Add marker
                    if (mCurrent != null)
                        mCurrent.remove(); // Remove old marker
                    mCurrent = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(lat,longitude))
                    .title("You"));
                    //Move camera to position
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,longitude),12.0f));

                }
            });

            Log.e(TAG, "displayLocation: "+String.format("Your location was changed: %f / %f",lat,longitude));


        }else
            Log.e(TAG, "displayLocation: Cant get your location" );
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVA);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);

    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode!= ConnectionResult.SUCCESS){
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICES_RESOLUTION_REQUEST).show();
            else{
                Toast.makeText(this,"This device is not suppoerted",Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true ;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng dangerZone = new LatLng(20.667963 , -103.364835);
        LatLng hotel = new LatLng(20.689721, -103.324598);
        mMap.addCircle(new CircleOptions()
        .center(dangerZone)
        .radius(50)
        .strokeColor(Color.BLUE)
        .fillColor(0x220000FF)
        .strokeWidth(5.0f));
        mMap.addCircle(new CircleOptions()
                .center(hotel)
                .radius(50)
                .strokeColor(Color.BLUE)
                .fillColor(0x220000FF)
                .strokeWidth(5.0f));
        //Add geoQuery
        //.05f = 50 m
        addGeoQueryListener("Hoel",hotel);
        addGeoQueryListener("School",dangerZone);
        /*GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(dangerZone.latitude,
                dangerZone.longitude),0.05f);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                sendOreONotification(String.format("%s entered the danger zone",key));
            }

            @Override
            public void onKeyExited(String key) {
                sendOreONotification(String.format("%s Exit the danger zone",key));
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                Log.e(TAG, String.format("%s moved within the dangerous area [%f/%f]",key,location.latitude,location.longitude));
            }

            @Override
            public void onGeoQueryReady() {
                Log.e(TAG, "onGeoQueryReady: ongeoqueryready ::." );
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.e(TAG, "onGeoQueryError: "+error);
            }
        });*/
    }

    public void addGeoQueryListener(final String locationName, LatLng zone){
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(zone.latitude,
                zone.longitude),0.05f);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                sendOreONotification(String.format("%s entered the danger zone",key)+locationName);
            }

            @Override
            public void onKeyExited(String key) {
                sendOreONotification(String.format("%s Exit the ",key)+locationName);
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                Log.e(TAG, String.format("%s moved within the dangerous area [%f/%f]",key,location.latitude,location.longitude));
            }

            @Override
            public void onGeoQueryReady() {
                Log.e(TAG, "onGeoQueryReady: ongeoqueryready ::." );
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.e(TAG, "onGeoQueryError: "+error);
            }
        });
    }

    private void sendNotification(String title, String message) {
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(message);
        NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this,MapsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults |= Notification.DEFAULT_SOUND;
        manager.notify(new Random().nextInt(),notification);

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED){
            return;
        }else{
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    if (checkPlayServices()){
                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();
                    }
                }
                break;
        }
    }

    public void sendOreONotification(String message){
        Intent resultIntent = new Intent(this,MapsActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(),
                0,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.BigTextStyle notificationStyle = new NotificationCompat.BigTextStyle();
        notificationStyle.setSummaryText("this is summary text 😬");
        notificationStyle.bigText("☠ "+message);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(),String.valueOf(1));
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
        notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));
        notificationBuilder.setContentTitle(getString(R.string.app_name)+ " 😬")
                //.setContentText("🌮 "+notificationDetails)
                .setAutoCancel(false)
                .setStyle(notificationStyle)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = new NotificationChannel(String.valueOf(1), "NOTIFICATION_CHANNEL_NAME", NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.enableVibration(true);
        long []vibration = {100, 200, 300, 400, 500, 400, 300, 200, 400};
        notificationChannel.setVibrationPattern(vibration);
        notificationBuilder.setChannelId(String.valueOf(1));
        assert mNotificationManager != null;
        mNotificationManager.createNotificationChannel(notificationChannel);

        mNotificationManager.notify(1 /* Request Code */, notificationBuilder.build());

    }
}
