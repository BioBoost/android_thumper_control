package be.vives.android.nico.thumpercontrol.orientation;

//Based on: https://www.reddit.com/r/androiddev/comments/1av1la/
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.WindowManager;
import android.widget.Toast;

public class Orientation implements SensorEventListener {

    private int sensorDelayMicros;

    private final SensorManager mSensorManager;
    private final WindowManager mWindowManager;
    private Context context;

    private Sensor mAccelero;
    private Sensor mMagnetic;
    private int mLastAccuracy;

    private OrientationChangeListener mListener;

    private float mags[] = new float[3];
    private float accels[] = new float[3];

    public Orientation(SensorManager sensorManager, WindowManager windowManager, Context context, int refreshDelay) {
        mSensorManager = sensorManager;
        mWindowManager = windowManager;

        // Can be null if the sensor hardware is not available
        mAccelero = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        
        if (mAccelero == null || mMagnetic == null) {
            Toast.makeText(context, "Required sensors are not available", Toast.LENGTH_SHORT).show();
            mAccelero = null;
            mMagnetic = null;
        }

        sensorDelayMicros = 1000*refreshDelay;
        this.context = context;
    }

    public void startListening(OrientationChangeListener listener) {
        // Return if sensors not available
        if (mAccelero == null || mMagnetic == null) {
            return;
        }

        // Return if listener already registered
        if (mListener == listener) {
            return;
        }

        mListener = listener;
        mSensorManager.registerListener(this, mAccelero, sensorDelayMicros);
        mSensorManager.registerListener(this, mMagnetic, sensorDelayMicros);
    }

    public void stopListening() {
        mSensorManager.unregisterListener(this);
        mListener = null;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (mLastAccuracy != accuracy) {
            mLastAccuracy = accuracy;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mListener == null) {
            return;
        }
        if (mLastAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            return;
        }

        float Rot[] = null;     //for gravity rotational data
        float I[] = null;       //for magnetic rotational data


//        float azimuth;
        float pitch;
        float roll;

        switch (event.sensor.getType())
        {
            case Sensor.TYPE_MAGNETIC_FIELD:
                mags = event.values.clone();
                break;
            case Sensor.TYPE_ACCELEROMETER:
                accels = event.values.clone();
                break;
        }

        if (mags != null && accels != null) {
            Rot = new float[9];
            I = new float[9];
            float values[] = new float[3];

            SensorManager.getRotationMatrix(Rot, I, accels, mags);
            // Correct if screen is in Landscape

            float[] outR = new float[9];
            SensorManager.remapCoordinateSystem(Rot, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);
            SensorManager.getOrientation(outR, values);

            // azimuth = values[0] * 57;        // Dont need this
            pitch = values[1] * 57;
            roll = values[2] * 57;
            mags = null;        //retrigger the loop when things are repopulated
            accels = null;      //retrigger the loop when things are repopulated

            mListener.onOrientationChanged(pitch, roll);
        }
    }
}