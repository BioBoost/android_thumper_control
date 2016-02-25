package be.vives.android.nico.thumpercontrol;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class NeoPixelControlActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {
    private String base_url;        // Base url must end with a slash !!!!
    private Retrofit retrofit;
    private NeoPixelService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_neo_pixel_control);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String serverip = sharedPref.getString(SettingsActivity.KEY_PREF_NODE_IP, "192.168.1.100");
        String serverport = sharedPref.getString(SettingsActivity.KEY_PREF_NODE_PORT, "3000");

        base_url = "http://" + serverip + ":" + serverport + "/";
        ((TextView)(findViewById(R.id.lblServer))).setText(base_url);

        retrofit = new Retrofit.Builder()
                .baseUrl(base_url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(NeoPixelService.class);

        // Set change listener for seekbars
        ( (SeekBar)findViewById(R.id.seekRed)).setOnSeekBarChangeListener(this);
        ( (SeekBar)findViewById(R.id.seekGreen)).setOnSeekBarChangeListener(this);
        ( (SeekBar)findViewById(R.id.seekBlue)).setOnSeekBarChangeListener(this);
        ( (SeekBar)findViewById(R.id.seekDelay)).setOnSeekBarChangeListener(this);
    }

    public void onGetPixelStringInfoClick(View view) {
        // Params needed for request
        String id = "todo";     // Should be put in settings

        // Create call instance
        Call<NeoPixelString> callStringInfo = service.getResponse(id);

        // Call enqueue to make an asynchronous request
        callStringInfo.enqueue(new Callback<NeoPixelString>() {
            // On Android, callbacks will be executed on the main thread
            @Override
            public void onResponse(Response<NeoPixelString> response, Retrofit retrofit) {
                if (response.body() != null) {
                    NeoPixelString str = response.body();
                    Log.i("REST", response.toString());
                    Log.i("REST", "ID = " + str.getStringId() + " COUNT = " + str.getNumberOfPixel());
                    ((EditText) findViewById(R.id.txtNumberOfPixels)).setText(str.getNumberOfPixel());
                } else {
                    Log.e("REST", "Request returned no data");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.i("REST", t.toString());
                Toast.makeText(getApplicationContext(), "Failed to send request for information", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onSetPixelColorClick(View view) {
        String id = "todo";     // Should be put in settings
        int red = 0;
        int green = 0;
        int blue = 0;

        if (view == findViewById(R.id.btnColorEffect)) {
            red = ((SeekBar) findViewById(R.id.seekRed)).getProgress();
            green = ((SeekBar) findViewById(R.id.seekGreen)).getProgress();
            blue = ((SeekBar) findViewById(R.id.seekBlue)).getProgress();
        }

        setColorEffect(id, red, green, blue);
    }

    public void onStrobeClick(View view) {
        String id = "todo";     // Should be put in settings
        int red = ((SeekBar) findViewById(R.id.seekRed)).getProgress();
        int green = ((SeekBar) findViewById(R.id.seekGreen)).getProgress();
        int blue = ((SeekBar) findViewById(R.id.seekBlue)).getProgress();
        int delay = ((SeekBar) findViewById(R.id.seekDelay)).getProgress();

        setStrobeEffect(id, red, green, blue, delay);
    }

    private void setColorEffect(String id, int r, int g, int b) {
        NeoPixelColorEffect effect = new NeoPixelColorEffect(r, g, b);

        Call<StatusReport> callSetStringColor = service.setStringColor(id, effect);
        callSetStringColor.enqueue(new Callback<StatusReport>() {
            @Override
            public void onResponse(Response<StatusReport> response, Retrofit retrofit) {
                if (response.body() != null) {
                    Log.i("REST", response.toString());
                } else {
                    Log.e("REST", "Request returned no data");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.i("REST", t.toString());
                Toast.makeText(getApplicationContext(), "Failed to set color", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setStrobeEffect(String id, int r, int g, int b, int delay) {
        NeoPixelStrobeEffect effect = new NeoPixelStrobeEffect(r, g, b, (byte)delay);

        Call<StatusReport> callStrobe = service.strobe(id, effect);
        callStrobe.enqueue(new Callback<StatusReport>() {
            @Override
            public void onResponse(Response<StatusReport> response, Retrofit retrofit) {
                if (response.body() != null) {
                    Log.i("REST", response.toString());
                } else {
                    Log.e("REST", "Request returned no data");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.i("REST", t.toString());
                Toast.makeText(getApplicationContext(), "Failed to activate strobe", Toast.LENGTH_SHORT).show();
            }
        });
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
