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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_neo_pixel_control);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void onGetPixelStringInfoClick(View view) {
        Toast.makeText(this, "Doing REST request", Toast.LENGTH_SHORT).show();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.0.3:3000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        NeoPixelService service = retrofit.create(NeoPixelService.class);

        String id = ((EditText)findViewById(R.id.txtStringId)).getText().toString();
        Call<NeoPixelString> callPet = service.getResponse(id);
        callPet.enqueue(new Callback<NeoPixelString>() {
            @Override
            public void onResponse(Response<NeoPixelString> response, Retrofit retrofit) {
                if (response.body() != null) {
                    Log.i("REST", response.toString());
                    Log.i("REST", "ID = " + response.body().getStringId() + " COUNT = " + response.body().getNumberOfPixel());
                    ((EditText)findViewById(R.id.txtNumberOfPixels)).setText(response.body().getNumberOfPixel());
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
