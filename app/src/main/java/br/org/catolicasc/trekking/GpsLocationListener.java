package br.org.catolicasc.trekking;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
//import android.location.LocationListener;
import com.google.android.gms.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.sql.Connection;

public class GpsLocationListener implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private final String TAG = "GPS_LOCATION_LISTENER";
    private final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final long UPDATE_INTERVAL = 100, FASTEST_INTERVAL = 100; // = 0.1 seconds

    private Context context;
    private double currentLatitude;
    private double currentLongitude;
    private LocationManager locationManager;
    private PositionHandler positionHandler;
    private GoogleApiClient googleApiClient;
    private Location location;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient mFusedLocationClient;


    interface PositionHandler {
        void onPositionChanged(Double latitude, Double longitude);
    }


    public GpsLocationListener(Context context, PositionHandler p) {

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        // locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, this);

        positionHandler = p;
        this.context = context;
        googleApiClient = new GoogleApiClient.Builder(context).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).
                addApi(LocationServices.API).
                build();
        if (googleApiClient != null) googleApiClient.connect();
    }

    private boolean checkforPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();

        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this.context);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog((Activity) this.context, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
            } else {
//                fin
            }

            return false;
        }

        return true;
    }

    private void startLocationUpdates() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        if (ActivityCompat.checkSelfPermission(this.context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                &&  ActivityCompat.checkSelfPermission(this.context,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this.context, "You need to enable permissions to display location !", Toast.LENGTH_SHORT).show();
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "[ON CONNECTED] Connected into google service");
        if (ActivityCompat.checkSelfPermission(this.context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                &&  ActivityCompat.checkSelfPermission(this.context,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this.context);
        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if (location != null) {
            if (positionHandler != null) {
                positionHandler.onPositionChanged(location.getLatitude(), location.getLongitude());
                startLocationUpdates();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            currentLongitude = location.getLongitude();
            currentLatitude = location.getLatitude();

            if (positionHandler != null) {
                positionHandler.onPositionChanged(currentLatitude, currentLongitude);
            }

            Log.i(TAG, "[onLocationChanged] Lat: " + currentLatitude + " - Long: " + currentLongitude);
        }
    }
}
