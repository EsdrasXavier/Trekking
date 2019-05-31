package br.org.catolicasc.trekking;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

import br.org.catolicasc.trekking.models.Point;

public class MainActivity extends AppCompatActivity implements GpsLocationListener.PositionHandler, CompassListener.CompassHandler {

    private String TAG = "MainActivity";
    private final int TELEMETRY_CICLES = 5;
    private final int TELEMETRY_DELEAY = 700; /* Will be used each iteration of the telemetry */

    private ArrayList<Point> points = new ArrayList<Point>(20);
    private Point lastPoint;
    private Point currentPoint;
    private TextView angleText;
    private TextView longitudeText;
    private TextView latitudeText;
    private TextView currentPointText;
    private TextView pointInfo;
    private Button addPoint;
    private ProgressBar progressBar;
    private GpsLocationListener gpsLocationListener;
    private CompassListener compassListener;
    private double lat;
    private double lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        angleText = findViewById(R.id.angle);
        longitudeText = findViewById(R.id.longitude);
        latitudeText = findViewById(R.id.latitude);
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
                Telemetry currentPositionTelemetry = new Telemetry();
                currentPositionTelemetry.execute((Void[]) null);

            }
        });

        lastPoint = new Point(0, 0);
        gpsLocationListener = new GpsLocationListener(this, this);
        compassListener = new CompassListener(this, this);
    }

    // what to do before background task
    private void disableEnableControls(boolean enable){
        addPoint.setEnabled(enable);
    }

    private class Telemetry extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            disableEnableControls(false);
            runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));
        }

        @Override
        protected Void doInBackground(Void... params) {
            double _lat = 0;
            double _lon = 0;

            for (int i = 0; i < TELEMETRY_CICLES; i++) {
                _lat += lat;
                _lon += lon;

                try {
                    Thread.sleep(TELEMETRY_DELEAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            _lat = _lat / TELEMETRY_CICLES;
            _lon = _lon / TELEMETRY_CICLES;
            lastPoint.setLat(_lat);
            lastPoint.setLon(_lon);

            runOnUiThread(() -> {
                String txt = "Lat: " + lastPoint.getLat() + "\n Lon: " + lastPoint.getLon();
                currentPointText.setText(txt);
                calculateDistance();
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            disableEnableControls(true);
            runOnUiThread(() -> progressBar.setVisibility(View.INVISIBLE));
        }
    };


    private void calculateDistance() {
        if (lastPoint != null) {

            Double angle = GpsMath.courseTo(lat, lon, lastPoint.getLat(), lastPoint.getLon());
            Double distance = GpsMath.distanceBetween(lat, lon, lastPoint.getLat(), lastPoint.getLon());

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
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onPositionChanged(Double latitude, Double longitude) {
        Log.i(TAG, "[ON POSITION CHANGED] Lat: " + latitude.toString() + " - Lon: " + longitude.toString());
        lat = latitude;
        lon = longitude;
        currentPoint.setLat(lat);
        currentPoint.setLon(lon);
        latitudeText.setText( "Latitude: " + currentPoint.getLat());
        longitudeText.setText("Longitude: " + currentPoint.getLon());
        calculateDistance();
    }

    @Override
    public void onAngleChanged(Double angle) {
        // Log.i(TAG, "[ON ANGLE CHANGED] angle: " + angle.toString());
        angleText.setText("Angle: " + angle.toString() + "°");
    }
}
