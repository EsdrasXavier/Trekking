package br.org.catolicasc.trekking;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements GpsLocationListener.PositionHandler, CompassListener.CompassHandler {

    private String TAG = "MainActivity";

    private TextView angleText;
    private TextView longitudeText;
    private TextView latitudeText;
    private GpsLocationListener gpsLocationListener;
    private CompassListener compassListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Mapquest, openstreetmap os

        angleText = findViewById(R.id.angle);
        longitudeText = findViewById(R.id.longitude);
        latitudeText = findViewById(R.id.latitude);

        gpsLocationListener = new GpsLocationListener(this, this);
        compassListener = new CompassListener(this, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onPositionChanged(Double latitude, Double longitude) {
        Log.i(TAG, "[ON POSITION CHANGED] Lat: " + latitude.toString() + " - Lon: " + longitude.toString());
        latitudeText.setText( "Latitude: " + latitude.toString());
        longitudeText.setText("Longitude: " +longitude.toString());
    }

    @Override
    public void onAngleChanged(Double angle) {
        Log.i(TAG, "[ON ANGLE CHANGED] angle: " + angle.toString());
        angleText.setText("Angle: " + angle.toString() + "°");
    }
}
