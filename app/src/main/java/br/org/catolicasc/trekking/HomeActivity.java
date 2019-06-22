package br.org.catolicasc.trekking;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.org.catolicasc.trekking.adapters.PointsRecyclerViewAdapter;
import br.org.catolicasc.trekking.adapters.SwipeToDeleteCallback;
import br.org.catolicasc.trekking.dal.PointDal;
import br.org.catolicasc.trekking.models.Point;
import io.github.controlwear.virtual.joystick.android.JoystickView;

public class HomeActivity extends Fragment implements CompassListener.CompassHandler,
        GpsLocationListener.PositionHandler,
        PointsRecyclerViewAdapter.PointsCRUD {

    private final String TAG = "HomeActivity";

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

    // The view
    protected TextView mTextViewControlAngle;
    protected TextView mTextViewControlStrength;
    protected TextView mTextViewAngle;
    protected TextView mTextViewLat;
    protected TextView mTextViewLon;
    protected TextView mTextViewDirection;
    protected Button mButtonAdd;
    protected Button mButtonStart;
    protected Button mButtonStop;
    protected ProgressBar progressBar;

    private BluetoothService mBluetoothService;

    private Context context;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle("Connectar dispositovo");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_home, container, false);

        mTextViewAngle = rootView.findViewById(R.id.tv_angle);
        mTextViewControlAngle = rootView.findViewById(R.id.tv_control_angle);
        mTextViewControlStrength = rootView.findViewById(R.id.tv_control_strength);
        mTextViewLat = rootView.findViewById(R.id.tv_lat);
        mTextViewLon = rootView.findViewById(R.id.tv_lon);
        mTextViewDirection = rootView.findViewById(R.id.tv_direction);
        mButtonAdd = rootView.findViewById(R.id.btn_add);
        mButtonStart = rootView.findViewById(R.id.btn_start);
        mButtonStop = rootView.findViewById(R.id.btn_stop);
        progressBar = rootView.findViewById(R.id.progress);
        progressBar.setVisibility(View.INVISIBLE);

        mContext = this.context.getApplicationContext();
        // Setup Points Recycler View
        RecyclerView recyclerView = rootView.findViewById(R.id.rv_points);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        // recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerView, this));
        pointsRecyclerViewAdapter = new PointsRecyclerViewAdapter(mContext, new ArrayList<Point>(), this);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(pointsRecyclerViewAdapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(pointsRecyclerViewAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));


        // Setup compass listener
        mCompassListener = new CompassListener(mContext, this);
        // Setup Gps listener
        mGpsLocationListener = new GpsLocationListener(mContext, this);

        // Setup Location Listener
        currentPoint = new Point(0, 0);
        prevPoint = new Point(0, 0);
        mPIDController = PIDController.fabricate(KP, KD, KI, TOLERANCE);
        setupJoystick(rootView);

        // Setup Points Form;
        mButtonAdd.setOnClickListener(v -> {
            Telemetry currentPositionTelemetry = new Telemetry();
            currentPositionTelemetry.execute((Void[]) null);
        });

        return rootView;
    }


    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
    }


    @Override
    public void onResume() {
        super.onResume();
        try {
            mBluetoothService = (BluetoothService) getActivity().getIntent().getExtras().get("conn");
            Log.i(TAG, "Data: " + mBluetoothService);
        } catch (Exception e) {
            Log.i(TAG, "Error: " + e);
        }
        if (pointsRecyclerViewAdapter != null) {
            pointsRecyclerViewAdapter.setPoints(fetchPoints());
            Log.d(TAG, "onResume: send message to the recycler view reload data");
        } else {
            Log.e(TAG, "onResume: recycler view is not defined");
        }
    }

    private void setupJoystick(View rootView) {
        JoystickView joystick = rootView.findViewById(R.id.joystick);
        joystick.setOnMoveListener((angle, strength) -> {
            mTextViewControlAngle.setText(angle + "°");
            mTextViewControlStrength.setText(strength + "%");
            if (mBluetoothService != null) {
                byte a[] = new byte[] {2, 4, 5};
                mBluetoothService.write(a);
            }
        });
    }


    private void disableEnableControls(boolean enable){
        mButtonAdd.setEnabled(enable);
        mButtonStart.setEnabled(enable);
        mButtonStop.setEnabled(enable);
    }


    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public void onAngleChanged(Double angle) {
        mTextViewAngle.setText(angle.toString() + "°");

        if (mPIDController != null) {
            Double power = mPIDController.performPid(angle);
            mTextViewDirection.setText("± " + String.format("%.2f", power));
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
        mTextViewLon.setText(Point.preciseLatLon(8, longitude));
        calculateDistance();
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


    private class Telemetry extends AsyncTask<Void, Void, Void> {
        private final int TELEMETRY_CICLES = 5;
        private final int TELEMETRY_DELEAY = 700; /* Will be used each iteration of the telemetry */

        @Override
        protected void onPreExecute() {
            disableEnableControls(false);
            mHandler.post(() -> progressBar.setVisibility(View.VISIBLE));
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

            mHandler.post(() -> {
                mTextViewLat.setText(prevPoint.getPreciseLat(8));
                mTextViewLon.setText(prevPoint.getPreciseLon(8));
                savePoint(prevPoint);
                calculateDistance();
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            disableEnableControls(true);
            mHandler.post(() -> progressBar.setVisibility(View.INVISIBLE));
        }
    }
}
