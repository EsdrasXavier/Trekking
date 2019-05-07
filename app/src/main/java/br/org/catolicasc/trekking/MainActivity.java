package br.org.catolicasc.trekking;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

//import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapquest.mapping.MapQuest;

import com.mapquest.mapping.maps.MapView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GpsLocationListener.PositionHandler, CompassListener.CompassHandler {

    private String TAG = "MainActivity";

//    private
    private ArrayList<Point> points = new ArrayList<Point>(20);
    private List<LatLng> coordinates = new ArrayList<>();
    private TextView angleText;
    private TextView longitudeText;
    private TextView latitudeText;
    private Button reCenterButton;
    private Button addPoint;
    private ProgressBar progressBar;
    private GpsLocationListener gpsLocationListener;
    private CompassListener compassListener;
    private double lat;
    private double lon;

    private MapView mMapView;
    private MapboxMap mMapboxMap;
    private MarkerOptions markerOptions;
    private PolylineOptions polylineOptions;


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
                addMarker(mMapboxMap, lat, lon, "Eu");
            }


        });

        angleText = findViewById(R.id.angle);
        longitudeText = findViewById(R.id.longitude);
        latitudeText = findViewById(R.id.latitude);
        reCenterButton = findViewById(R.id.button2);
        addPoint = findViewById(R.id.addPoint);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);


        addPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                addPoint.setSaveEnabled(false);
                int media = 20;
                double _lat = 0;
                double _lon = 0;
                for (int i = 0; i < media; i++) {
                    _lat += lat;
                    _lon += lon;
                }

                _lat = _lat / media;
                _lon = _lon / media;
                points.add(new Point(_lat, _lon));
                coordinates.add(new LatLng(_lat, _lon));
                addMarker(mMapboxMap, _lat, _lon, "Ponto");
                progressBar.setVisibility(View.INVISIBLE);
                addPoint.setSaveEnabled(true);
                updatePolyline(mMapboxMap);
            }
        });

        reCenterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMapboxMap != null) {
                    LatLng pos = new LatLng(lat, lon);
                    markerOptions = new MarkerOptions();
                    markerOptions.setPosition(pos);
                    mMapboxMap.removeMarker(markerOptions.getMarker());
                    mMapboxMap.addMarker(markerOptions);
                    mMapboxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 17));
                }
            }
        });

        gpsLocationListener = new GpsLocationListener(this, this);
        compassListener = new CompassListener(this, this);
    }

    private void updatePolyline(MapboxMap mapboxMap) {
        if (this.coordinates.size() < 0) return ;

        if (!mapboxMap.getPolylines().isEmpty()) {
            mapboxMap.removePolyline(polylineOptions.getPolyline());
        }

        polylineOptions = new PolylineOptions();
        polylineOptions.addAll(coordinates);
        polylineOptions.width(5);
        polylineOptions.color(Color.BLUE);
        mapboxMap.addPolyline(polylineOptions);
    }


    private void addMarker(MapboxMap mapboxMap, double _lat, double _lon, String name) {
        markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(_lat, _lon));
        markerOptions.title(name);
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
