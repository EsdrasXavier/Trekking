package br.org.catolicasc.trekking;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

//import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapquest.mapping.MapQuest;

import com.mapquest.mapping.maps.MapView;

public class MainActivity extends AppCompatActivity implements GpsLocationListener.PositionHandler, CompassListener.CompassHandler {

    private String TAG = "MainActivity";

    private TextView angleText;
    private TextView longitudeText;
    private TextView latitudeText;
    private Button reCenterButton;
    private GpsLocationListener gpsLocationListener;
    private CompassListener compassListener;
    private double lat;
    private double lon;

    private MapView mMapView;
    private MapboxMap mMapboxMap;
    private MarkerOptions markerOptions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapQuest.start(getApplicationContext());
        setContentView(R.layout.activity_main);
        // Mapquest, openstreetmap os

        mMapView = (MapView) findViewById(R.id.mapquestMapView);

        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                LatLng pos = new LatLng(lat, lon);
                mMapboxMap = mapboxMap;
                mMapView.setStreetMode();
                mMapboxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 18));

                mMapboxMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(@NonNull Marker marker) {
                        Toast.makeText(MainActivity.this, marker.getTitle(), Toast.LENGTH_LONG).show();
                        return true;
                    }
                });
                addMarker(mMapboxMap);
            }


        });

        angleText = findViewById(R.id.angle);
        longitudeText = findViewById(R.id.longitude);
        latitudeText = findViewById(R.id.latitude);
        reCenterButton = findViewById(R.id.button2);

        reCenterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMapboxMap != null) {
                    LatLng pos = new LatLng(lat, lon);
                    markerOptions.setPosition(pos);
                    mMapboxMap.updateMarker(markerOptions.getMarker());
                    mMapboxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 17));
                }
            }
        });

        gpsLocationListener = new GpsLocationListener(this, this);
        compassListener = new CompassListener(this, this);
    }



    private void addMarker(MapboxMap mapboxMap) {
        markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(lat, lon));
        markerOptions.title("Eu");
        markerOptions.snippet("Welcome to Denver!");
        mapboxMap.addMarker(markerOptions);
    }


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }


    @Override
    public void onPositionChanged(Double latitude, Double longitude) {
        Log.i(TAG, "[ON POSITION CHANGED] Lat: " + latitude.toString() + " - Lon: " + longitude.toString());
        lat = latitude;
        lon = longitude;
        latitudeText.setText( "Latitude: " + latitude.toString());
        longitudeText.setText("Longitude: " +longitude.toString());
    }

    @Override
    public void onAngleChanged(Double angle) {
        Log.i(TAG, "[ON ANGLE CHANGED] angle: " + angle.toString());
        angleText.setText("Angle: " + angle.toString() + "°");
    }
}
