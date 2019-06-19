package br.org.catolicasc.trekking;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

import br.org.catolicasc.trekking.models.Point;

public class PlaygroundActivity extends BaseActivity implements GpsLocationListener.PositionHandler,
        CompassListener.CompassHandler {
    private String TAG = "PlaygroundActivity";
    private final int TELEMETRY_CICLES = 5;
    private final int TELEMETRY_DELEAY = 700; /* Will be used each iteration of the telemetry */

    /**
     * Constants to use in the PID controller
     */
    private final double KP = 3.5;
    private final double KD = 0.05;
    private final double KI = 10.5;
    private final double TOLERANCE = 5.0;

    private ArrayList<Point> points = new ArrayList<Point>(20);
    private Point lastPoint;
    private Point currentPoint;
    private TextView angleText;
    private TextView longitudeText;
    private TextView latitudeText;
    private TextView currentPointText;
    private TextView pointInfo;
    private TextView onTargetView;
    private TextView motorPower;
    private Button addPoint;
    private Button bluetoothButton;
    private ProgressBar progressBar;
    private GpsLocationListener mGpsLocationListener;
    private CompassListener mCompassListener;
    private double lat;
    private double lon;
    private PIDController mPIDController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout container = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_playground, container);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().findItem(R.id.playground).setChecked(true);

        angleText = findViewById(R.id.angle);
        longitudeText = findViewById(R.id.longitude);
        latitudeText = findViewById(R.id.latitude);
        addPoint = findViewById(R.id.addPoint);
        bluetoothButton = findViewById(R.id.bluetooth);
        progressBar = findViewById(R.id.progressBar);
        currentPointText = findViewById(R.id.currentPoint);
        pointInfo = findViewById(R.id.pointInfo);
        onTargetView = findViewById(R.id.onTargetView);
        motorPower = findViewById(R.id.motorPower);
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
        Log.i(TAG, "Created button listener");
        try {
            lastPoint = new Point(0, 0);
            mGpsLocationListener = new GpsLocationListener(this, this);
            mCompassListener = new CompassListener(this, this);
        } catch (Exception e) {
            Log.e(TAG, "Error on listeners creation. Error: " + e);
        }

        mPIDController = PIDController.fabricate(KP, KD, KI, TOLERANCE);
        bluetoothButton.setOnClickListener(v -> {
            Intent mIntent = new Intent(v.getContext(), BluetoothActivity.class);;
            startActivityForResult(mIntent, 0);
        });
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
    }


    private void calculateDistance() {
        if (lastPoint != null) {

            Double angle = GpsMath.courseTo(lat, lon, lastPoint.getLat(), lastPoint.getLon());
            Double distance = GpsMath.distanceBetween(lat, lon, lastPoint.getLat(), lastPoint.getLon());
            String _angle = new DecimalFormat("#.00").format(angle);
            String _distance = new DecimalFormat("#.00").format(distance);
            String txt = "Angulo para chegar ao ponto: " + _angle + "°\n";
            txt += "Distancia: " + _distance + "m";
            pointInfo.setText(txt);
            if (angle > 0) {
                mPIDController.setSetPoint(angle);
            }
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
        latitudeText.setText( "Latitude: " + latitude);
        longitudeText.setText("Longitude: " + longitude);
        calculateDistance();
    }

    @Override
    public void onAngleChanged(Double angle) {
        // Log.i(TAG, "[ON ANGLE CHANGED] angle: " + angle.toString());
        angleText.setText("Angle: " + angle.toString() + "°");

        if (mPIDController != null) {
            Double power = mPIDController.performPid(angle);
            String myPower = power.toString() + " == " + (- power);

            String isOnTarget = "On target: Não";
            if (mPIDController.onTarget()) {
                isOnTarget = "On target: Sim";
                mPIDController.reset();
            }

            motorPower.setText(myPower);
            onTargetView.setText(isOnTarget);
        }
    }
}
