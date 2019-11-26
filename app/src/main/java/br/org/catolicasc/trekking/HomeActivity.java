package br.org.catolicasc.trekking;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
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
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    protected TextView mTextViewDistance;
    protected Button mButtonAdd;
    protected Button mButtonStart;
    protected Button mButtonStop;
    protected ProgressBar progressBar;

    private BluetoothService mBluetoothService;
    private ExecutePoints executePoints;

    private Context context;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private int power = 0;
    private double currentAngle = 0;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Objects.requireNonNull(getActivity()).setTitle("Dashboard");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_home, container, false);

        mTextViewAngle = rootView.findViewById(R.id.tv_angle);
        mTextViewControlAngle = rootView.findViewById(R.id.tv_control_angle);
        mTextViewControlStrength = rootView.findViewById(R.id.tv_control_strength);
        mTextViewLat = rootView.findViewById(R.id.tv_lat);
        mTextViewLon = rootView.findViewById(R.id.tv_lon);
        mTextViewDirection = rootView.findViewById(R.id.tv_direction);
        mTextViewDistance = rootView.findViewById(R.id.tv_distance);
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
        CompassListener mCompassListener = new CompassListener(mContext, this);
        // Setup Gps listener
        GpsLocationListener mGpsLocationListener = new GpsLocationListener(Objects.requireNonNull(getActivity()), this);

        // Setup Location Listener
        currentPoint = new Point(0, 0);
        prevPoint = new Point(0, 0);
        mPIDController = PIDController.fabricate(KP, KD, KI, TOLERANCE);
        mPIDController.setMaxOutput(270);
        mPIDController.setMinOutput(200);
        mPIDController.setTolerance(10);
        setupJoystick(rootView);

        // Setup Points Form;
        mButtonAdd.setOnClickListener(v -> {
            Telemetry currentPositionTelemetry = new Telemetry();
            currentPositionTelemetry.execute((Void[]) null);
        });

        // Setup the start button listener
        mButtonStart.setOnClickListener(v -> startFollowPoints());

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
    }


    @SuppressLint("LogNotTimber")
    @Override
    public void onResume() {
        super.onResume();
        try {
            mBluetoothService = (BluetoothService) Objects.requireNonNull(getActivity()).getIntent().getExtras().get("conn");
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

    @SuppressLint("SetTextI18n")
    private void setupJoystick(View rootView) {
        JoystickView joystick = rootView.findViewById(R.id.joystick);
        joystick.setOnMoveListener((int angle, int strength) -> {
            mTextViewControlAngle.setText(angle + "°");
            mTextViewControlStrength.setText(strength + "%");

            if (mBluetoothService != null) {

                int speed = Protocol.map(strength, 0, 50, 100, 400);
                byte[] left = new byte[2];
                byte[] right = new byte[2];

                if (angle == 0) { // Quando soltar o controle ele para o robô
                    left = Protocol.intToByte(0);
                    right = Protocol.intToByte(0);
                } else if (135 > angle && angle > 45) { // Frente
                    left = Protocol.intToByte(speed);
                    right = Protocol.intToByte(speed);
                } else if (315 > angle && angle > 225) { // Tras
                    left = Protocol.intToByte(-speed);
                    right = Protocol.intToByte(-speed);
                } else if (225 > angle && angle > 135) { // Esquerda
                    left = Protocol.intToByte(-speed);
                    right = Protocol.intToByte(speed);
                } else if ((360 >= angle && angle >= 315) || (45 >= angle && angle >= 0)) { // Direita
                    left = Protocol.intToByte(speed);
                    right = Protocol.intToByte(-speed);
                }

                byte[] a = new byte[] {Protocol.MOTOR_CONTROL, left[0], left[1], right[0], right[1]};
                mBluetoothService.write(a);
            }
        });
    }


    private void disableEnableControls(boolean enable){
        mButtonAdd.setEnabled(enable);
        mButtonStart.setEnabled(enable);
        mButtonStop.setEnabled(enable);
    }


    @SuppressLint("LogNotTimber")
    private void startFollowPoints() {
        Log.i(TAG, "Start/Stop Button pressed!");
        if (mBluetoothService == null && false) { // FIXME: I just add the  `&& false` for debug purpose
            Toast.makeText(this.context, "Device not connected to robot!", Toast.LENGTH_LONG).show();
            return ;
        }

        if (executePoints == null) {
            Log.i(TAG, "Starting service!");
            executePoints = new ExecutePoints(this.context);
            executePoints.execute((Void[]) null);
        } else {
            Log.i(TAG, "Stopping service!");
            executePoints.cancel(true);
            executePoints = null;
        }
    }


    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public void onAngleChanged(Double angle) {
        mTextViewAngle.setText(angle.toString() + "°");
        currentAngle = angle;

        byte[] data = Protocol.intToByte(angle.intValue());
        byte[] sendData = new byte[] { Protocol.SEND_ANGLE, data[0], data[1] };

        if (mBluetoothService != null) {
            mBluetoothService.write(data);
        }

        if (mPIDController != null) {
            Double powerFromPid = mPIDController.performPid(angle);
            power = powerFromPid.intValue();
            // mTextViewDirection.setText("± " + String.format("%.2f", powerFromPid));

            if (mPIDController.onTarget()) {
//                mPIDController.reset();
               // mTextViewDirection.setTextColor(Color.BLACK);
            } else {
                //mTextViewDirection.setTextColor(Color.RED);
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
        return dal.findAllGeographicPoints();
    }

    @SuppressLint("LogNotTimber")
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
        if (angle > 0) {
            mPIDController.setSetPoint(angle);
        }
    }


    @SuppressLint("StaticFieldLeak")
    private class ExecutePoints extends AsyncTask<Void, Void, Void> {
        private WeakReference<Context> contextRef;

        ExecutePoints(Context context) {
            contextRef = new WeakReference<>(context);
        }

        @SuppressLint({"SetTextI18n", "ResourceAsColor"})
        @Override
        protected void onPreExecute() {
            disableEnableControls(false);
            mHandler.post(() -> {
                Toast.makeText(this.contextRef.get(), "Starting the route!", Toast.LENGTH_LONG).show();
                mButtonStart.setText("Stop");
                mButtonStart.setBackgroundColor(Color.parseColor("#a61d3e"));
                mButtonStart.setEnabled(true);
            });
        }

        @SuppressLint({"LogNotTimber", "SetTextI18n"})
        @Override
        protected Void doInBackground(Void... voids) {
            List<Point> points = fetchPoints();

            if (points == null) { // In case there is no point
                mHandler.post(() -> Toast.makeText(this.contextRef.get(), "Unable to fetch points!", Toast.LENGTH_LONG).show());
                return null;
            }

            byte[] left = Protocol.intToByte(0);
            byte[] right = Protocol.intToByte(0);;
            byte[] data;
            int index = 0;
            boolean isObstacle;
            boolean isOnTarget = false;
            Point point = points.get(index);

            while (true) {
                if (isCancelled()) break;
                double angle = GpsMath.courseTo(currentPoint.getLat(), currentPoint.getLon(), point.getLat(), point.getLon());
                double distance = GpsMath.distanceBetween(currentPoint.getLat(), currentPoint.getLon(), point.getLat(), point.getLon());
                double rightAngle = GpsMath.getBestTurnAngle(currentAngle, angle);

                if (!isOnTarget) {
                    if (currentAngle > (angle + 5) || currentAngle < (angle - 5)) {
                        if (currentAngle < angle) {
                            left = Protocol.intToByte(-80);
                            right = Protocol.intToByte(80);
                        } else {
                            left = Protocol.intToByte(80);
                            right = Protocol.intToByte(-80);
                        }
                    } else {
                        isOnTarget = true;
                    }
                } else {
                    // In case the robot is too off the route kkk
                    // qm joga lol ehh gyayyy
                    if (rightAngle < -15 || rightAngle > 15) {
                        isOnTarget = false;
                        continue;
                    } else if (rightAngle < 0) {
                        // Goes right
                    } else if (rightAngle > 0) {
                        // Goes left
                    } else {
                        // Move straight
                    }
                }


                if (mBluetoothService != null) {
                    data = new byte[] {Protocol.MOTOR_CONTROL, left[0], left[1], right[0], right[1]};
                    mBluetoothService.write(data);
                }

                mHandler.post(() -> {
                    // Just update the screen for debug pourposes
                    mTextViewDistance.setText(String.valueOf(distance));
                    mTextViewDirection.setText(String.valueOf(rightAngle));
                });

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    break;
                }
            }

            if (mBluetoothService != null) {
                data = new byte[]{Protocol.MOTOR_CONTROL, 0, 0, 0, 0};
                mBluetoothService.write(data);
            }
            return null;
        }

        protected void onCancelled() {
            disableEnableControls(true);
            mHandler.post(() -> {
                if (mBluetoothService != null) {
                    byte[] data = new byte[]{Protocol.MOTOR_CONTROL, 0, 0, 0, 0};
                    mBluetoothService.write(data);
                }

                Toast.makeText(this.contextRef.get(), "Route done!", Toast.LENGTH_LONG).show();
                mButtonStart.setText("Start");
                mButtonStart.setBackgroundColor(Color.parseColor("#43A047"));
            });
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
