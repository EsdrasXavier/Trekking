package br.org.catolicasc.trekking;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

public class GpsLocationListener implements LocationListener {

    private final String TAG = "GPS";

    private double currentLatitude;
    private double currentLongitude;
    private LocationManager locationManager;
    private PositionHandler positionHandler;

    interface PositionHandler {
        void onPositionChanged(Double latitude, Double longitude);
    }


    public GpsLocationListener(Context context, PositionHandler p) {


        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);

        positionHandler = p;

    }

    public double getCurrentLatitude() {
        return currentLatitude;
    }

    public double getCurrentLongitude() {
        return currentLongitude;
    }

    private void setMostRecentLocation(Location lastKnownLocation) {

    }

    @Override
    public void onLocationChanged(Location location) {
        currentLongitude = location.getLongitude();
        currentLatitude = location.getLatitude();

        if (positionHandler != null) {
            positionHandler.onPositionChanged(currentLatitude, currentLongitude);
        }

        Log.i(TAG, "[onLocationChanged] Lat: " + currentLatitude + " - Long: " + currentLongitude);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
