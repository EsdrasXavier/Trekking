package br.org.catolicasc.trekking;

import android.content.Context;
import android.support.design.widget.NavigationView;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;

import br.org.catolicasc.trekking.adapters.PointsRecyclerViewAdapter;
import br.org.catolicasc.trekking.adapters.RecyclerItemClickListener;
import br.org.catolicasc.trekking.models.Point;
import br.org.catolicasc.trekking.presenters.MainPresenter;
import br.org.catolicasc.trekking.views.MainView;
import io.github.controlwear.virtual.joystick.android.JoystickView;

public class MainActivity extends BaseActivity implements MainView {
    private String TAG = "MainActivity";

    private PointsRecyclerViewAdapter pointsRecyclerViewAdapter;
    private MainPresenter presenter;
    protected TextView mTextViewAngleLeft;
    protected TextView mTextViewStrengthLeft;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout container = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_main, container);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().findItem(R.id.control).setChecked(true);

        this.presenter = new MainPresenter(this);

        RecyclerView recyclerView = findViewById(R.id.rv_points);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerView, this));
        pointsRecyclerViewAdapter = new PointsRecyclerViewAdapter(this, new ArrayList<Point>());
        recyclerView.setAdapter(pointsRecyclerViewAdapter);


        mTextViewAngleLeft = (TextView) findViewById(R.id.tv_angle);
        mTextViewAngleLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "angulo");
            }
        });
        mTextViewStrengthLeft = (TextView) findViewById(R.id.tv_strength);
        JoystickView joystick= (JoystickView) findViewById(R.id.joystick);
        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                mTextViewAngleLeft.setText(angle + "°");
                mTextViewStrengthLeft.setText(strength + "%");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pointsRecyclerViewAdapter != null) {
            pointsRecyclerViewAdapter.setPoints(presenter.fetchPoints());
            Log.d(TAG, "onResume: send message to the recycler view reload data");
        } else {
            Log.e(TAG, "onResume: recycler view is not defined");
        }
    }

    @Override
    public Context getContextForPresenter() {
        return getApplicationContext();
    }
}
