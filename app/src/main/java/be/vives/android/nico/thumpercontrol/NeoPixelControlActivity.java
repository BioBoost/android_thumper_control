package be.vives.android.nico.thumpercontrol;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import be.vives.android.nico.thumpercontrol.rest.neopixel.NeoPixelRestService;
import be.vives.android.nico.thumpercontrol.rest.neopixel.NeoPixelString;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class NeoPixelControlActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    private NeoPixelRestService neopixel_service;
    private String string_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_neo_pixel_control);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set change listener for seekbars
        ( (SeekBar)findViewById(R.id.seekRed)).setOnSeekBarChangeListener(this);
        ( (SeekBar)findViewById(R.id.seekGreen)).setOnSeekBarChangeListener(this);
        ( (SeekBar)findViewById(R.id.seekBlue)).setOnSeekBarChangeListener(this);
        ( (SeekBar)findViewById(R.id.seekDelay)).setOnSeekBarChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String serverip = sharedPref.getString(SettingsActivity.KEY_PREF_NODE_IP, "192.168.1.100");
        String serverport = sharedPref.getString(SettingsActivity.KEY_PREF_NODE_PORT, "3000");
        string_id = sharedPref.getString(SettingsActivity.KEY_PREF_NEO_STRINGID, "0");

        String base_url = "http://" + serverip + ":" + serverport + "/";
        ((TextView)(findViewById(R.id.lblServer))).setText(base_url);

        neopixel_service = new NeoPixelRestService(base_url, this);
    }

    public void onGetPixelStringInfoClick(View view) {
        neopixel_service.getPixelStringInfo(string_id, new Callback<NeoPixelString>() {
            // On Android, callbacks will be executed on the main thread
            @Override
            public void onResponse(Response<NeoPixelString> response, Retrofit retrofit) {
                if (response.body() != null) {
                    ((EditText) findViewById(R.id.txtNumberOfPixels)).setText(response.body().getNumberOfPixel());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(getApplicationContext(), "Failed to send request for information", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onSetPixelColorClick(View view) {
        int red = 0;
        int green = 0;
        int blue = 0;

        if (view == findViewById(R.id.btnColorEffect)) {
            red = ((SeekBar) findViewById(R.id.seekRed)).getProgress();
            green = ((SeekBar) findViewById(R.id.seekGreen)).getProgress();
            blue = ((SeekBar) findViewById(R.id.seekBlue)).getProgress();
        } // else turn all off

        neopixel_service.setColor(string_id, red, green, blue, null);
    }

    public void onStrobeClick(View view) {
        int red = ((SeekBar) findViewById(R.id.seekRed)).getProgress();
        int green = ((SeekBar) findViewById(R.id.seekGreen)).getProgress();
        int blue = ((SeekBar) findViewById(R.id.seekBlue)).getProgress();
        int delay = ((SeekBar) findViewById(R.id.seekDelay)).getProgress();

        neopixel_service.setStrobeEffect(string_id, red, green, blue, delay, null);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar == (findViewById(R.id.seekDelay))) {
            if (progress < 10) {
                ((SeekBar) findViewById(R.id.seekDelay)).setProgress(10);
            }
        }

        ((TextView)findViewById(R.id.txtRed)).setText("" + ((SeekBar) findViewById(R.id.seekRed)).getProgress());
        ((TextView)findViewById(R.id.txtGreen)).setText("" + ((SeekBar) findViewById(R.id.seekGreen)).getProgress());
        ((TextView)findViewById(R.id.txtBlue)).setText("" + ((SeekBar) findViewById(R.id.seekBlue)).getProgress());
        ((TextView)findViewById(R.id.txtDelay)).setText("" + ((SeekBar) findViewById(R.id.seekDelay)).getProgress());
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) { }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) { }
}
