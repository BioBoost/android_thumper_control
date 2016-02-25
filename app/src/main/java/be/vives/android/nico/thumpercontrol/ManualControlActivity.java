package be.vives.android.nico.thumpercontrol;

// Partial credits go to http://www.vogella.com/tutorials/AndroidTouch/article.html

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import be.vives.android.nico.thumpercontrol.rest.trex.ThumperStatusReport;
import be.vives.android.nico.thumpercontrol.rest.trex.TRexRestAPI;
import be.vives.android.nico.thumpercontrol.rest.trex.ThumperSpeed;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class ManualControlActivity extends AppCompatActivity {
    private static final int BATTERY_VOLTAGE_REFRESH_TIME = 5000;

    private static final int MAX_SPEED = 255;       // This should be a setting
    private int trex_refresh_time;
    private int max_trex_speed;                     // 0 to 100%

    private boolean leftIsHeld;
    private boolean rightIsHeld;

    private boolean isStopped;

    private int left_speed;
    private int right_speed;

    private ImageView left_throttle;
    private ImageView right_throttle;

    private Rect rectLeft;
    private Rect rectRight;

    private int half_range;

    // A Handler allows you to send and process Message and Runnable objects associated with a thread's MessageQueue.
    private Handler refreshTimer;

    // Represents a command that can be executed. Often used to run code in a different Thread.
    private Runnable thumperControlCode;

    // SparseArrays map integers to Objects. Like HashMap but more performant
    private SparseArray<Point> mActivePointers;

    TextView txtThumperState;
    TextView txtLeftSpeed;
    TextView txtRightSpeed;

    private String base_url;        // Base url must end with a slash !!!!
    private Retrofit retrofit;
    private TRexRestAPI service;

    // Thread to get thumper battery voltage
    private Runnable thumperBatteryVoltage;

    private float battery_voltage_threshold;

    private class ThumperControlTask implements Runnable {
        @Override
        public void run() {
            leftIsHeld = false;
            rightIsHeld = false;

            for (int size = mActivePointers.size(), i = 0; i < size; i++) {
                Point point = mActivePointers.valueAt(i);
                if (point != null) {
                    if (rectLeft.contains(point.x, point.y)) {
                        left_speed = calculateSpeed(rectLeft.centerY(), point.y, half_range);
                        leftIsHeld = true;
                    } else if (rectRight.contains(point.x, point.y)) {

                        right_speed = calculateSpeed(rectLeft.centerY(), point.y, half_range);
                        rightIsHeld = true;
                    }
                }
            }

            // We need to do this outside of for because of else clause
            // which cant be determined before the full list of pointers is itterated
            if (leftIsHeld) {
                left_throttle.setBackgroundColor(Color.GREEN);
            } else {
                left_throttle.setBackgroundColor(Color.RED);
                left_speed = 0;
            }

            if (rightIsHeld) {
                right_throttle.setBackgroundColor(Color.GREEN);
            } else {
                right_throttle.setBackgroundColor(Color.RED);
                right_speed = 0;
            }

            if (leftIsHeld || rightIsHeld) {
                txtThumperState.setText(R.string.driving);
            } else {
                txtThumperState.setText(R.string.stopped);
            }

            txtLeftSpeed.setText(left_speed + "");
            txtRightSpeed.setText(right_speed + "");

            // Check if we can stop sending when user is not touching screen
            if (!isStopped || (left_speed != 0) || (right_speed != 0)) {
                sendThumperSpeed();
            }

            isStopped = (left_speed == 0 && right_speed == 0);

            // Repeat this same runnable code again every x milliseconds
            refreshTimer.postDelayed(thumperControlCode, trex_refresh_time);
        }
    }

    private class ThumperBatteryVoltageTask implements Runnable {
        @Override
        public void run() {
            getThumperBatteryVoltage();

            // Repeat this same runnable code again every x milliseconds
            refreshTimer.postDelayed(thumperBatteryVoltage, BATTERY_VOLTAGE_REFRESH_TIME);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_control);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Create the Handler object (on the main thread by default)
        refreshTimer = new Handler();

        // Define the task to be run here
        thumperControlCode = new ThumperControlTask();

        mActivePointers = new SparseArray<Point>();

        // Following some helpful data to have for touch
        // Get location of controls
        // We need to switch left and right because motors are connected wrong
        right_throttle = ((ImageView)findViewById(R.id.imgLeftThrottle));
        left_throttle = ((ImageView)findViewById(R.id.imgRightThrottle));

        txtThumperState = ((TextView)findViewById(R.id.txtThumperState));

        // We need to switch left and right because motors are connected wrong
        txtRightSpeed = ((TextView)findViewById(R.id.txtLeftSpeed));
        txtLeftSpeed = ((TextView)findViewById(R.id.txtRightSpeed));

        thumperBatteryVoltage = new ThumperBatteryVoltageTask();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        // We cant do this in the onCreate as the Views have not been placed yet
        int[] l = new int[2];
        left_throttle.getLocationOnScreen(l);
        rectLeft = new Rect(l[0], l[1], l[0] + left_throttle.getWidth(), l[1] + left_throttle.getHeight());

        right_throttle.getLocationOnScreen(l);
        rectRight = new Rect(l[0], l[1], l[0] + right_throttle.getWidth(), l[1] + right_throttle.getHeight());

        half_range = Math.abs(rectLeft.top-rectLeft.bottom)/2;
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Removes pending code execution
        refreshTimer.removeCallbacks(thumperControlCode);
        refreshTimer.removeCallbacks(thumperBatteryVoltage);

        // Kill the Thumper
        left_speed = 0;
        right_speed = 0;
        sendThumperSpeed();
        isStopped = true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String serverip = sharedPref.getString(SettingsActivity.KEY_PREF_NODE_IP, "192.168.1.100");
        String serverport = sharedPref.getString(SettingsActivity.KEY_PREF_NODE_PORT, "3000");
        trex_refresh_time = Integer.parseInt(sharedPref.getString(SettingsActivity.KEY_PREF_TREX_REFRESH_TIME, "500"));
        max_trex_speed = Integer.parseInt(sharedPref.getString(SettingsActivity.KEY_PREF_TREX_MAX_SPEED, "50"));
        battery_voltage_threshold = Float.parseFloat(sharedPref.getString(SettingsActivity.KEY_PREF_TREX_BATTERY_THRESHOLD, "7.5"));

        base_url = "http://" + serverip + ":" + serverport + "/";

        retrofit = new Retrofit.Builder()
                .baseUrl(base_url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(TRexRestAPI.class);

        isStopped = true;

        // Schedule code for execution
        refreshTimer.post(thumperControlCode);
        refreshTimer.post(thumperBatteryVoltage);
    }

    private int calculateSpeed(float centerY, float yPos, int half_range) {
        return (int)((MAX_SPEED * (centerY - yPos)/half_range)*max_trex_speed/100.0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // get pointer index from the event object
        int pointerIndex = event.getActionIndex();

        // get pointer ID
        int pointerId = event.getPointerId(pointerIndex);

        // get masked (not specific to a pointer) action
        int maskedAction = event.getActionMasked();

        switch (maskedAction) {

            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                // We have a new pointer. Lets add it to the list of pointers

                Point point = new Point();
                point.x = (int)event.getX(pointerIndex);
                point.y = (int)event.getY(pointerIndex);
                mActivePointers.put(pointerId, point);
                break;
            }
            case MotionEvent.ACTION_MOVE: { // a pointer was moved
                for (int size = event.getPointerCount(), i = 0; i < size; i++) {
                    Point point = mActivePointers.get(event.getPointerId(i));
                    if (point != null) {
                        point.x = (int)event.getX(i);
                        point.y = (int)event.getY(i);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL: {
                mActivePointers.remove(pointerId);
                break;
            }
        }

        return true;
    }

    public void sendThumperSpeed() {
        ThumperSpeed speed = new ThumperSpeed(left_speed, right_speed);

        Call<ThumperStatusReport> callSetThumperSpeed = service.setThumperSpeed(speed);
        callSetThumperSpeed.enqueue(new Callback<ThumperStatusReport>() {
            @Override
            public void onResponse(Response<ThumperStatusReport> response, Retrofit retrofit) {
                if (response.body() == null) {
                    Log.e("REST", "Request returned no data");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.i("REST", t.toString());
                Toast.makeText(getApplicationContext(), "Request failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getThumperBatteryVoltage() {
        Call<ThumperStatusReport> callGetBatteryVoltage = service.getBatteryVoltage();
        callGetBatteryVoltage.enqueue(new Callback<ThumperStatusReport>() {
            @Override
            public void onResponse(Response<ThumperStatusReport> response, Retrofit retrofit) {
                if (response.body() == null) {
                    Log.e("REST", "Request returned no data");
                } else {
                    float voltage = response.body().getBatteryVoltage();
                    ((TextView) findViewById(R.id.txtBatteryVoltage)).setText(voltage + "V");

                    if (voltage <= battery_voltage_threshold) {
                        ((TextView) findViewById(R.id.txtBatteryVoltage)).setTextColor(getResources().getColor(R.color.battery_danger));
                        Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                        // Vibrate for 2000 milliseconds
                        v.vibrate(2000);
                    } else if (voltage <= (battery_voltage_threshold + 0.2*battery_voltage_threshold)) {
                        ((TextView) findViewById(R.id.txtBatteryVoltage)).setTextColor(getResources().getColor(R.color.battery_low));
                        Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                        // Vibrate for 500 milliseconds
                        v.vibrate(500);
                    } else {
                        ((TextView) findViewById(R.id.txtBatteryVoltage)).setTextColor(getResources().getColor(R.color.battery_ok));
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.i("REST", t.toString());
                Toast.makeText(getApplicationContext(), "Request failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
