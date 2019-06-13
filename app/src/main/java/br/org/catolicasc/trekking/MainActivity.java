package br.org.catolicasc.trekking;

import android.support.design.widget.NavigationView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class MainActivity extends BaseActivity {
    private String TAG = "MainActivity";


    protected TextView mTextViewAngleLeft;
    protected TextView mTextViewStrengthLeft;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout container = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_main, container);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().findItem(R.id.control).setChecked(true);

        mTextViewAngleLeft = (TextView) findViewById(R.id.tvAngle);
        mTextViewAngleLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "angulo");
            }
        });
        mTextViewStrengthLeft = (TextView) findViewById(R.id.tvStrength);
        JoystickView joystick= (JoystickView) findViewById(R.id.joystick);
        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                mTextViewAngleLeft.setText(angle + "°");
                mTextViewStrengthLeft.setText(strength + "%");
            }
        });
    }

}
