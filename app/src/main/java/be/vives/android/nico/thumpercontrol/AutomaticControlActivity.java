package be.vives.android.nico.thumpercontrol;

import android.app.Activity;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class AutomaticControlActivity extends AppCompatActivity implements OrientationChangeListener {

    private Orientation mOrientation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_automatic_control);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mOrientation = new Orientation((SensorManager)getSystemService(Activity.SENSOR_SERVICE), getWindow().getWindowManager(), this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mOrientation.startListening(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mOrientation.stopListening();
    }

    @Override
    public void onOrientationChanged(float pitch, float roll) {
        ((TextView)findViewById(R.id.txtPitch)).setText(Float.toString(pitch));
        ((TextView)findViewById(R.id.txtRoll)).setText(Float.toString(roll));
    }
}
