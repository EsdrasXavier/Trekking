package br.org.catolicasc.trekking;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import br.org.catolicasc.trekking.adapters.PointsRecyclerViewAdapter;
import br.org.catolicasc.trekking.adapters.SwipeToDeleteCallback;
import br.org.catolicasc.trekking.dal.PointDal;
import br.org.catolicasc.trekking.models.Point;
import io.github.controlwear.virtual.joystick.android.JoystickView;

public class MainActivity extends BaseActivity implements CompassListener.CompassHandler,
        GpsLocationListener.PositionHandler,
        PointsRecyclerViewAdapter.PointsCRUD {
    private String TAG = "MainActivity";
    /**
     * Constants to use in the PID controller
     */
    private final double KP = 3.5;
    private final double KD = 0.05;
    private final double KI = 10.5;
    private final double TOLERANCE = 5.0;

    private PointsRecyclerViewAdapter pointsRecyclerViewAdapter;
    private CompassListener mCompassListener;
    private GpsLocationListener mGpsLocationListener;
    private Point currentPoint;
    private Point prevPoint;
    private PIDController mPIDController;
    private Context mContext;
    // View
    protected TextView mTextViewControlAngle;
    protected TextView mTextViewControlStrength;
    protected TextView mTextViewAngle;
    protected TextView mTextViewLat;
    protected TextView mTextViewLon;
    protected TextView mTextViewDirection;
    protected Button mButtonAdd;
    protected Button mButtonStart;
    protected Button mButtonStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout container = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_main, container);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().findItem(R.id.control).setChecked(true);

        // Setup Points Recycler View
        RecyclerView recyclerView = findViewById(R.id.rv_points);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerView, this));
        pointsRecyclerViewAdapter = new PointsRecyclerViewAdapter(this, new ArrayList<Point>(), this);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(pointsRecyclerViewAdapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(pointsRecyclerViewAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));

        mContext = getApplicationContext();
        // Setup compass listener
        mCompassListener = new CompassListener(this, this);
        // Setup Gps listener
        mGpsLocationListener = new GpsLocationListener(this, this);

        // Assign view variables
        mTextViewAngle = findViewById(R.id.tv_angle);
        mTextViewControlAngle = (TextView) findViewById(R.id.tv_control_angle);
        mTextViewControlStrength = findViewById(R.id.tv_control_strength);
        mTextViewLat = findViewById(R.id.tv_lat);
        mTextViewLon = findViewById(R.id.tv_lon);
        mTextViewDirection = findViewById(R.id.tv_direction);
        mButtonAdd = findViewById(R.id.btn_add);
        mButtonStart = findViewById(R.id.btn_start);
        mButtonStop = findViewById(R.id.btn_stop);

        // Setup Location Listener
        currentPoint = new Point(0, 0);
        prevPoint = new Point(0, 0);
        mPIDController = PIDController.fabricate(KP, KD, KI, TOLERANCE);
        setupJoystick();

        // Setup Points Form;
        mButtonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Telemetry currentPositionTelemetry = new Telemetry();
                currentPositionTelemetry.execute((Void[]) null);
            }
        });
    }

    private void setupJoystick() {
        JoystickView joystick = findViewById(R.id.joystick);
        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                mTextViewControlAngle.setText(angle + "°");
                mTextViewControlStrength.setText(strength + "%");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pointsRecyclerViewAdapter != null) {
            pointsRecyclerViewAdapter.setPoints(fetchPoints());
            Log.d(TAG, "onResume: send message to the recycler view reload data");
        } else {
            Log.e(TAG, "onResume: recycler view is not defined");
        }
    }

    public List<Point> fetchPoints() {
        PointDal dal = new PointDal(mContext);
//        Point p = new Point(-26.4673054, -49.1158967);
//        dal.createGeographicPoint(p);
        return dal.findAllGeographicPoints();
    }

    public boolean savePoint(Point point) {
        if (!point.isValid()) {
            return false;
        }

        PointDal dal = new PointDal(mContext);
        if (point.isPersisted()) {
            if (dal.updateGeographicPoint(point)) {
                Log.d(TAG, "Successfully updated Point: "+ point.getId());
            } else {
                Log.d(TAG, "Failed to update Point");
            }
        } else {
            long id = dal.createGeographicPoint(point);
            if (id > 0) {
                point.setId(id);
                pointsRecyclerViewAdapter.addPoints(point);
                Log.d(TAG, "Successfully created a new Point with id: " + id);
                return true;
            } else {
                Log.d(TAG, "Failed to create a new Point");
            }
        }


        return false;
    }

    @Override
    public void updatePoint(Point p) {
        savePoint(p);
    }

    @Override
    public void deletePoint(Point p) {
        if (!p.isPersisted()) { return; }

        PointDal dal = new PointDal(mContext);
        dal.deleteGeographicPoint(p.getId());
    }

    @Override
    public void onAngleChanged(Double angle) {
        mTextViewAngle.setText(angle.toString() + "°");

        if (mPIDController != null) {
            Double power = mPIDController.performPid(angle);
            mTextViewDirection.setText("± " + power);
            if (mPIDController.onTarget()) {
                mPIDController.reset();
                mTextViewDirection.setTextColor(Color.BLACK);
            } else {
                mTextViewDirection.setTextColor(Color.RED);
            }
        }
    }

    @Override
    public void onPositionChanged(Double latitude, Double longitude) {
        // Log.d(TAG, "[ON POSITION CHANGED] Lat: " + latitude.toString() + " - Lon: " + longitude.toString());

        if (currentPoint == null) {
            return;
        }
        currentPoint.setLat(latitude);
        currentPoint.setLon(longitude);
        mTextViewLat.setText(Point.preciseLatLon(8, latitude));
        mTextViewLon.setText(Point.preciseLatLon(8, latitude));
        calculateDistance();
    }

    private void calculateDistance() {
        if (prevPoint == null) {
            return;
        }

        Double angle = GpsMath.courseTo(currentPoint.getLat(), currentPoint.getLon(), prevPoint.getLat(), prevPoint.getLon());
//        Double distance = GpsMath.distanceBetween(currentPoint.getLat(), currentPoint.getLon(), prevPoint.getLat(), prevPoint.getLon());
//        String angleStr = new DecimalFormat("#.00").format(angle);
//        String distanceStr = new DecimalFormat("#.00").format(distance);
//        String txt = "Angulo para chegar ao ponto: " + angleStr + "°\n";
//        txt += "Distancia: " + distanceStr + "m";
//        Log.d(TAG, txt);
        if (angle > 0) {
            mPIDController.setSetPoint(angle);
        }
    }

    private void disableEnableControls(boolean enable){
        mButtonAdd.setEnabled(enable);
        mButtonStart.setEnabled(enable);
        mButtonStop.setEnabled(enable);
    }

    private class Telemetry extends AsyncTask<Void, Void, Void> {
        private final int TELEMETRY_CICLES = 5;
        private final int TELEMETRY_DELEAY = 700; /* Will be used each iteration of the telemetry */

        @Override
        protected void onPreExecute() {
            disableEnableControls(false);
//            runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));
        }

        @Override
        protected Void doInBackground(Void... params) {
            double lat = 0;
            double lon = 0;

            for (int i = 0; i < TELEMETRY_CICLES; i++) {
                lat += currentPoint.getLat();
                lon += currentPoint.getLon();

                try {
                    Thread.sleep(TELEMETRY_DELEAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            lat = lat / TELEMETRY_CICLES;
            lon = lon / TELEMETRY_CICLES;

            prevPoint = new Point(lat, lon);

            runOnUiThread(() -> {
//                mTextViewLat.setText(prevPoint.getPreciseLat(8));
//                mTextViewLon.setText(prevPoint.getPreciseLon(8));
                savePoint(prevPoint);
                calculateDistance();
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            disableEnableControls(true);
//            runOnUiThread(() -> progressBar.setVisibility(View.INVISIBLE));
        }
    }
}
