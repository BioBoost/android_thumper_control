package be.vives.android.nico.thumpercontrol;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class NeoPixelControlActivity extends AppCompatActivity {
    private static String base_url = "http://10.176.33.22:3000/";
        // Base url must end with a slash !!!!
    private Retrofit retrofit;
    private NeoPixelService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_neo_pixel_control);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
                    Log.i("REST", response.toString());
                    Log.i("REST", "ID = " + response.body().getStringId() + " COUNT = " + response.body().getNumberOfPixel());
                    ((EditText) findViewById(R.id.txtNumberOfPixels)).setText(response.body().getNumberOfPixel());
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

        NeoPixelStringColor color = new NeoPixelStringColor(red, green, blue);

        Call<NeoPixelStringColor> callSetStringColor = service.setStringColor(id, color);
        callSetStringColor.enqueue(new Callback<NeoPixelStringColor>() {
            @Override
            public void onResponse(Response<NeoPixelStringColor> response, Retrofit retrofit) {
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
