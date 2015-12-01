package be.vives.android.nico.thumpercontrol;

// Partial credits go to http://www.vogella.com/tutorials/AndroidTouch/article.html

import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;

public class ManualControlActivity extends AppCompatActivity {
    private static final int MAX_SPEED = 120;       // This should be a setting
    private static final int REFRESH_MS = 250;       // This should be a setting

    private boolean leftIsHeld;
    private boolean rightIsHeld;

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
                txtThumperState.setText("Hauling Ass");
            } else {
                txtThumperState.setText("Stopped");
            }

            txtLeftSpeed.setText(left_speed + "");
            txtLeftSpeed.setText(right_speed + "");

            // Repeat this same runnable code again every x milliseconds
            refreshTimer.postDelayed(thumperControlCode, REFRESH_MS);
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
        left_throttle = ((ImageView)findViewById(R.id.imgLeftThrottle));
        right_throttle = ((ImageView)findViewById(R.id.imgRightThrottle));

        txtThumperState = ((TextView)findViewById(R.id.txtThumperState));
        txtLeftSpeed = ((TextView)findViewById(R.id.txtLeftSpeed));
        txtRightSpeed = ((TextView)findViewById(R.id.txtRightSpeed));
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
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Schedule code for execution
        refreshTimer.post(thumperControlCode);
    }

    private int calculateSpeed(float centerY, float yPos, int half_range) {
        return (int)(MAX_SPEED * (centerY - yPos)/half_range);
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
}
