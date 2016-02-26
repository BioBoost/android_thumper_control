package be.vives.android.nico.thumpercontrol;

// Partial credits go to http://www.vogella.com/tutorials/AndroidTouch/article.html

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import be.vives.android.nico.thumpercontrol.orientation.Orientation;
import be.vives.android.nico.thumpercontrol.orientation.OrientationChangeListener;
import be.vives.android.nico.thumpercontrol.rest.trex.TRexRestAPI;
import be.vives.android.nico.thumpercontrol.rest.trex.ThumperSpeed;
import be.vives.android.nico.thumpercontrol.rest.trex.ThumperStatusReport;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class AutomaticControlActivity extends AppCompatActivity implements OrientationChangeListener {
    private boolean leftIsHeld;
    private boolean rightIsHeld;

    private boolean isStopped;

    private int left_speed;
    private int right_speed;

    private ImageView leftControl;
    private ImageView rightControl;

    private Rect rectLeft;
    private Rect rectRight;

    // SparseArrays map integers to Objects. Like HashMap but more performant
    private SparseArray<Point> mActivePointers;

    private Orientation mOrientation;
    private long lastTimeUpdate;
    private int refreshDelay = 100;         // This should be an option !

    // Device placement
    private static int PITCH_FULL_FORWARD = 70;     // More + compared to reverse !
    private static int PITCH_FULL_REVERSE = -30;
    private static int PITCH_RANGE = PITCH_FULL_FORWARD - PITCH_FULL_REVERSE;

    private static int ROLL_FULL_LEFT = -150;
    private static int ROLL_FULL_RIGHT = -30;       // More + compared to left !
    private static int ROLL_RANGE = ROLL_FULL_RIGHT - ROLL_FULL_LEFT;

    // Speed control for thumper itself
    private static int MAX_SPEED = 150;
    private static int TURN_SPEED = 100;

    private String base_url;        // Base url must end with a slash !!!!
    private Retrofit retrofit;
    private TRexRestAPI service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_automatic_control);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mOrientation = new Orientation((SensorManager)getSystemService(Activity.SENSOR_SERVICE), getWindow().getWindowManager(), this, refreshDelay);

        mActivePointers = new SparseArray<Point>();

        // Following some helpful data to have for touch
        // Get location of controls
        // We need to switch left and right because motors are connected wrong
        leftControl = ((ImageView)findViewById(R.id.imgLeftHandle));
        rightControl = ((ImageView)findViewById(R.id.imgRightHandle));
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        // We cant do this in the onCreate as the Views have not been placed yet
        int[] l = new int[2];
        leftControl.getLocationOnScreen(l);
        rectLeft = new Rect(l[0], l[1], l[0] + leftControl.getWidth(), l[1] + leftControl.getHeight());

        rightControl.getLocationOnScreen(l);
        rectRight = new Rect(l[0], l[1], l[0] + rightControl.getWidth(), l[1] + rightControl.getHeight());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mOrientation.startListening(this);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String serverip = sharedPref.getString(SettingsActivity.KEY_PREF_NODE_IP, "192.168.1.100");
        String serverport = sharedPref.getString(SettingsActivity.KEY_PREF_NODE_PORT, "3000");
//        trex_refresh_time = Integer.parseInt(sharedPref.getString(SettingsActivity.KEY_PREF_TREX_REFRESH_TIME, "500"));
//        max_trex_speed = Integer.parseInt(sharedPref.getString(SettingsActivity.KEY_PREF_TREX_MAX_SPEED, "50"));
//        battery_voltage_threshold = Float.parseFloat(sharedPref.getString(SettingsActivity.KEY_PREF_TREX_BATTERY_THRESHOLD, "7.5"));

        base_url = "http://" + serverip + ":" + serverport + "/";
        ((TextView)(findViewById(R.id.lblServer))).setText(base_url);

        retrofit = new Retrofit.Builder()
                .baseUrl(base_url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(TRexRestAPI.class);

        isStopped = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mOrientation.stopListening();

        // Kill the Thumper
        left_speed = 0;
        right_speed = 0;
        sendThumperSpeed();
        isStopped = true;
    }

    private void calculateSpeeds(float pitch, float roll) {
        // We need to rescale the pitch to our speed range first

        // Limit pitch
        pitch = Math.min(PITCH_FULL_FORWARD, pitch);
        pitch = Math.max(PITCH_FULL_REVERSE, pitch);

        // Limit roll
        roll = Math.max(ROLL_FULL_LEFT, roll);
        roll = Math.min(ROLL_FULL_RIGHT, roll);

        double pitch_mult = (2 * (pitch - PITCH_FULL_REVERSE) / (PITCH_FULL_FORWARD - PITCH_FULL_REVERSE)) - 1; // Between -1 and +1
        int base_speed = (int)((pitch_mult * MAX_SPEED));

        // Now we need to add turn control
        double roll_mult = (2 * (roll - ROLL_FULL_LEFT) / (ROLL_FULL_RIGHT - ROLL_FULL_LEFT)) - 1; // Between -1 and +1
        int turn = (int)(roll_mult * TURN_SPEED);

        left_speed = base_speed + turn;
        right_speed = base_speed - turn;

        left_speed = Math.min(left_speed, MAX_SPEED);
        left_speed = Math.max(left_speed, -MAX_SPEED);

        right_speed = Math.min(right_speed, MAX_SPEED);
        right_speed = Math.max(right_speed, -MAX_SPEED);
    }

    @Override
    public void onOrientationChanged(float pitch, float roll) {
        if (leftIsHeld && rightIsHeld) {
            // Indicate driving control active
            isStopped = false;

            ((TextView)findViewById(R.id.txtPitch)).setText(String.format("%.2f", pitch));
            ((TextView)findViewById(R.id.txtRoll)).setText(String.format("%.2f", roll));

            // Calculate left and right speed here
            calculateSpeeds(pitch, roll);

            // We need to check if refreshDelay is met
            // Orientation delay is not consistent !
            long currentTime = System.currentTimeMillis();
            long time_delta = currentTime - lastTimeUpdate;
            if (time_delta >= refreshDelay) {
                // NEEDS IMPLEMENTATION
                // Send speed of left and right to thumper
                sendThumperSpeed();

                lastTimeUpdate = System.currentTimeMillis();
            }
        } else {
            if (!isStopped) {
                // TODO
                // Stop the thumper here
                left_speed = 0;
                right_speed = 0;

                isStopped = true;
                sendThumperSpeed();

            }
        }
    }

    private void checkForHolds() {
        leftIsHeld = false;
        rightIsHeld = false;

        for (int size = mActivePointers.size(), i = 0; i < size; i++) {
            Point point = mActivePointers.valueAt(i);
            if (point != null) {
                if (rectLeft.contains(point.x, point.y)) {
                    leftIsHeld = true;
                } else if (rectRight.contains(point.x, point.y)) {
                    rightIsHeld = true;
                }
            }
        }

//        isStopped = (leftIsHeld == 0 && rightIsHeld == 0);
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

        // Check which ones are held
        checkForHolds();

        // Visual indication
        if (leftIsHeld) {
            leftControl.setImageResource(R.drawable.ic_hold_true);
        } else {
            leftControl.setImageResource(R.drawable.ic_hold_false);
        }

        if (rightIsHeld) {
            rightControl.setImageResource(R.drawable.ic_hold_true);
        } else {
            rightControl.setImageResource(R.drawable.ic_hold_false);
        }

        return true;
    }

    public void sendThumperSpeed() {
        ThumperSpeed speed = new ThumperSpeed(right_speed, left_speed);
                // Switch speeds as motors are connected wrong

        ((TextView)findViewById(R.id.txtLeftMotorSpeed)).setText("" + left_speed);
        ((TextView)findViewById(R.id.txtRightMotorSpeed)).setText("" + right_speed);

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
}
