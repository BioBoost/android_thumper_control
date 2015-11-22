package be.vives.android.nico.thumpercontrol;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class NeoPixelControlActivity extends AppCompatActivity {
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
    }

    public void onGetPixelStringInfoClick(View view) {
        Toast.makeText(this, "Doing REST request", Toast.LENGTH_SHORT).show();

        // Params needed for request
        String id = ((EditText)findViewById(R.id.txtStringId)).getText().toString();

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
                Toast.makeText(getApplicationContext(), "Request failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onSetPixelColorClick(View view) {
        Toast.makeText(this, "Doing REST request", Toast.LENGTH_SHORT).show();

        String id = ((EditText)findViewById(R.id.txtStringId)).getText().toString();
        int red = Integer.parseInt(((EditText) findViewById(R.id.txtRed)).getText().toString());
        int green = Integer.parseInt(((EditText)findViewById(R.id.txtGreen)).getText().toString());
        int blue = Integer.parseInt(((EditText)findViewById(R.id.txtBlue)).getText().toString());

        NeoPixelColorEffect effect = new NeoPixelColorEffect(red, green, blue);

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
                Toast.makeText(getApplicationContext(), "Request failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onStrobeClick(View view) {
        Toast.makeText(this, "Doing REST request", Toast.LENGTH_SHORT).show();

        String id = ((EditText)findViewById(R.id.txtStringId)).getText().toString();
        int red = Integer.parseInt(((EditText) findViewById(R.id.txtRed)).getText().toString());
        int green = Integer.parseInt(((EditText)findViewById(R.id.txtGreen)).getText().toString());
        int blue = Integer.parseInt(((EditText)findViewById(R.id.txtBlue)).getText().toString());
        byte delay = Byte.parseByte(((EditText)findViewById(R.id.txtDelay)).getText().toString());

        NeoPixelStrobeEffect effect = new NeoPixelStrobeEffect(red, green, blue, delay);

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
                Toast.makeText(getApplicationContext(), "Request failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
