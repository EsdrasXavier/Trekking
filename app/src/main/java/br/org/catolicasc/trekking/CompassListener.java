package br.org.catolicasc.trekking;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class CompassListener implements SensorEventListener {

    private Sensor compass;
    private CompassHandler compassHandler;

    interface CompassHandler {
        void onAngleChanged(Double angle);
    }

    CompassListener(Context context, CompassHandler _compassHandler) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        compass = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        if (compass != null) {
            sensorManager.registerListener((SensorEventListener) this, compass, SensorManager.SENSOR_DELAY_UI);
            compassHandler = _compassHandler;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            double degree = Math.round(event.values[0]);
            if (this.compassHandler != null) {
                this.compassHandler.onAngleChanged(degree);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
