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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GpsLocationListener.PositionHandler, CompassListener.CompassHandler {

    private String TAG = "MainActivity";

    private ArrayList<Point> points = new ArrayList<Point>(20);
    private List<LatLng> coordinates = new ArrayList<>();
    private Point currentPoint;
    private double currenteAngle;
    private TextView angleText;
    private TextView longitudeText;
    private TextView latitudeText;
    private TextView currentPointText;
    private TextView pointInfo;
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
    private MarkerOptions currentPosition;
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
                addMarker(mapboxMap);
            }
        });

        angleText = findViewById(R.id.angle);
        longitudeText = findViewById(R.id.longitude);
        latitudeText = findViewById(R.id.latitude);
        reCenterButton = findViewById(R.id.button2);
        addPoint = findViewById(R.id.addPoint);
        progressBar = findViewById(R.id.progressBar);
        currentPointText = findViewById(R.id.currentPoint);
        pointInfo = findViewById(R.id.pointInfo);
        progressBar.setVisibility(View.INVISIBLE);

        String txt = "Angulo para chegar ao ponto: 0°\n";
        txt += "Distancia: 0m";
        pointInfo.setText(txt);
        currentPointText.setText("Lat: 0 \n Lon: 0");

        currentPoint = new Point(lat, lon);

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
//                addMarker(mMapboxMap, _lat, _lon, "Ponto");
                progressBar.setVisibility(View.INVISIBLE);
                addPoint.setSaveEnabled(true);

                String txt = "Lat: " + points.get(0).getLatitude() + "\n Lon: " + points.get(0).getLongitude();
                currentPointText.setText(txt);
//                updatePolyline(mMapboxMap);
            }
        });

        reCenterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMapboxMap != null) {
                    LatLng pos = new LatLng(lat, lon);
//                    updateCurrentPositionMarker(mMapboxMap);
//                    mMapboxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 17));
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

    private void updateCurrentPositionMarker(MapboxMap mapboxMap) {
        if (currentPosition != null) {
            mapboxMap.removeMarker(currentPosition.getMarker());
            currentPosition.position(new LatLng(currentPoint.getLatitude(), currentPoint.getLongitude()));
            mapboxMap.addMarker(markerOptions);
        }
    }


    private void addMarker(MapboxMap mapboxMap) {
        currentPosition = new MarkerOptions();
        currentPosition.position(new LatLng(currentPoint.getLatitude(), currentPoint.getLongitude()));
        currentPosition.title("Eu");
        mapboxMap.addMarker(markerOptions);
    }


    private void addMarker(MapboxMap mapboxMap, double _lat, double _lon, String name) {
        markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(_lat, _lon));
        markerOptions.title(name);
        mapboxMap.addMarker(markerOptions);
    }


    private void calculateDistance() {
        if (points.size() > 0) {
            Point p = points.get(0);
            Double angle = GpsMath.courseTo(lat, lon, p.getLatitude(), p.getLongitude());
            Double distance = GpsMath.distanceBetween(lat, lon, p.getLatitude(), p.getLongitude());

            String _angle = new DecimalFormat("#.00").format(angle);
            String _distance = new DecimalFormat("#.00").format(distance);

            String txt = "Angulo para chegar ao ponto: " + _angle + "°\n";
            txt += "Distancia: " + _distance + "m";
            pointInfo.setText(txt);
        }
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
        currentPoint.setLatitude(lat);
        currentPoint.setLongitude(lon);
        latitudeText.setText( "Latitude: " + currentPoint.getLatitude().toString());
        longitudeText.setText("Longitude: " + currentPoint.getLongitude().toString());
        calculateDistance();
    }

    @Override
    public void onAngleChanged(Double angle) {
        Log.i(TAG, "[ON ANGLE CHANGED] angle: " + angle.toString());
        currenteAngle = angle;
        angleText.setText("Angle: " + angle.toString() + "°");
    }
}
