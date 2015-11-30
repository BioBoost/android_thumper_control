package be.vives.android.nico.thumpercontrol;

import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ManualControlActivity extends AppCompatActivity {

    private boolean leftIsHeld;
    private boolean rightIsHeld;

    private Handler refreshTimer;
    private Runnable thumperControlCode;

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
        thumperControlCode = new Runnable() {
            @Override
            public void run() {
                // Do something here on the main thread
                if (leftIsHeld || rightIsHeld) {
                    ((TextView)findViewById(R.id.txtThumperState)).setText("Hauling Ass");
                } else {
                    ((TextView)findViewById(R.id.txtThumperState)).setText("Stopped");
                }

                // Repeat this same runnable code again every x milliseconds
                refreshTimer.postDelayed(thumperControlCode, 250);
            }
        };

        // Kick off the first runnable task right away
        refreshTimer.post(thumperControlCode);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Removes pending code execution
        refreshTimer.removeCallbacks(thumperControlCode);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // Get masked action (type of action)
        int action = MotionEventCompat.getActionMasked(event);
        Log.v("HOLD", actionToString(action));

        // Get the index of the pointer associated with the action
        int index = MotionEventCompat.getActionIndex(event);

        // Get location of hold controls
        ImageView left_throttle = ((ImageView)findViewById(R.id.imgLeftThrottle));
        ImageView right_throttle = ((ImageView)findViewById(R.id.imgRightThrottle));

        int[] l = new int[2];
        left_throttle.getLocationOnScreen(l);
        Rect rectLeft = new Rect(l[0], l[1], l[0] + left_throttle.getWidth(), l[1] + left_throttle.getHeight());

        right_throttle.getLocationOnScreen(l);
        Rect rectRight = new Rect(l[0], l[1], l[0] + right_throttle.getWidth(), l[1] + right_throttle.getHeight());

        // Get current pointer position
        int xPos = (int)MotionEventCompat.getX(event, index);
        int yPos = (int)MotionEventCompat.getY(event, index);

        // Determine which hold controls are pressed and released
        switch (action) {
            case MotionEvent.ACTION_DOWN:	// Single pointer
                if (rectLeft.contains(xPos, yPos)) {
                    leftIsHeld = true;
                } else if (rectRight.contains(xPos, yPos)) {
                    rightIsHeld = true;
                }
                break;
            case MotionEvent.ACTION_UP:		// Single pointer
                leftIsHeld = false;
                rightIsHeld = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (rectLeft.contains(xPos, yPos)) {
                    leftIsHeld = true;
                } else if (rectRight.contains(xPos, yPos)) {
                    rightIsHeld = true;
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:	// Multi pointer goes down
                if (rectLeft.contains(xPos, yPos)) {
                    leftIsHeld = true;
                } else if (rectRight.contains(xPos, yPos)) {
                    rightIsHeld = true;
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:		// Multi pointer goes up
                if (rectLeft.contains(xPos, yPos)) {
                    leftIsHeld = false;
                } else if (rectRight.contains(xPos, yPos)) {
                    rightIsHeld = false;
                }
                break;
        }

        // Visual indication
        if (leftIsHeld) {
            left_throttle.setBackgroundColor(Color.RED);
        } else {
            left_throttle.setBackgroundColor(Color.GREEN);
        }

        if (rightIsHeld) {
            right_throttle.setBackgroundColor(Color.RED);
        } else {
            right_throttle.setBackgroundColor(Color.GREEN);
        }

        return true;
    }

    // Given an action int, returns a string description
    public static String actionToString(int action) {
        switch (action) {
            case MotionEvent.ACTION_DOWN: return "Down";
            case MotionEvent.ACTION_MOVE: return "Move";
            case MotionEvent.ACTION_POINTER_DOWN: return "Pointer Down";
            case MotionEvent.ACTION_UP: return "Up";
            case MotionEvent.ACTION_POINTER_UP: return "Pointer Up";
            case MotionEvent.ACTION_OUTSIDE: return "Outside";
            case MotionEvent.ACTION_CANCEL: return "Cancel";
        }
        return "";
    }
}
