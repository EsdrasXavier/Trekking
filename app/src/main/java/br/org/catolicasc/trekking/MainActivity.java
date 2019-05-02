package br.org.catolicasc.trekking;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private String TAG = "MainActivity";

    private TextView angleText;
    private TextView longitudeText;
    private TextView latitudeText;

    private SensorManager mSensorManager;
    private Sensor compass;
    private Gps gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Mapquest, openstreetmap os

        angleText = findViewById(R.id.angle);
        longitudeText = findViewById(R.id.longitude);
        latitudeText = findViewById(R.id.latitude);

 sdjaspidj

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        compass = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        if (compass != null){
            mSensorManager.registerListener((SensorEventListener) this, compass, SensorManager.SENSOR_DELAY_NORMAL);
        }

        gps = new Gps(this);


    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener((SensorEventListener) this, compass, SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener((SensorEventListener) this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            float degree = Math.round(event.values[0]);
            angleText.setText("Heading: " + Float.toString(degree) + " degrees");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void makeUseOfNewLocation(Location loc) {
        String longitude = "Longitude: " + loc.getLongitude();
        Log.v("TESTE", longitude);
        String latitude = "Latitude: " + loc.getLatitude();
        Log.v("TESTE", latitude);
        latitudeText.setText(latitude);
        longitudeText.setText(longitude);
    }
}
