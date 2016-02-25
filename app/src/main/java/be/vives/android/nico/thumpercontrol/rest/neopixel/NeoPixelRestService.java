package be.vives.android.nico.thumpercontrol.rest.neopixel;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import be.vives.android.nico.thumpercontrol.rest.StatusReport;
import be.vives.android.nico.thumpercontrol.rest.neopixel.effects.NeoPixelColorEffect;
import be.vives.android.nico.thumpercontrol.rest.neopixel.effects.NeoPixelStrobeEffect;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by bioboost on 2/25/16.
 */
public class NeoPixelRestService {

    private String base_url;        // Base url must end with a slash !!!!
    private Retrofit retrofit;
    private NeoPixelRestAPI api;
    private Context context;

    public NeoPixelRestService(String base_url, Context context) {
        this.context = context;
        this.base_url = base_url;

        retrofit = new Retrofit.Builder()
                .baseUrl(base_url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(NeoPixelRestAPI.class);
    }

    public void setColor(String id, int r, int g, int b, Callback<StatusReport> callback) {
        NeoPixelColorEffect effect = new NeoPixelColorEffect(r, g, b);

        Call<StatusReport> callSetStringColor = api.setStringColor(id, effect);
        if (callback != null) {
            callSetStringColor.enqueue(callback);
        } else {
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
                    Log.e("REST", "Request to set color failed: " + t.toString());
                    Toast.makeText(context, "Failed to set color", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void setStrobeEffect(String id, int r, int g, int b, int delay, Callback<StatusReport> callback) {
        NeoPixelStrobeEffect effect = new NeoPixelStrobeEffect(r, g, b, delay);

        Call<StatusReport> callStrobe = api.strobe(id, effect);
        if (callback != null) {
            callStrobe.enqueue(callback);
        } else {
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
                    Log.e("REST", "Request to activate strobe failed: " + t.toString());
                    Toast.makeText(context, "Failed to activate strobe", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void getPixelStringInfo(String id, Callback<NeoPixelString> callback) {
        // Create call instance
        Call<NeoPixelString> callStringInfo = api.getResponse(id);

        if (callback != null) {
            callStringInfo.enqueue(callback);
        } else {
            callStringInfo.enqueue(new Callback<NeoPixelString>() {
                // On Android, callbacks will be executed on the main thread
                @Override
                public void onResponse(Response<NeoPixelString> response, Retrofit retrofit) {
                    if (response.body() != null) {
                        NeoPixelString str = response.body();
                        Log.i("REST", response.toString());
                        Log.i("REST", "ID = " + str.getStringId() + " COUNT = " + str.getNumberOfPixel());
                    } else {
                        Log.e("REST", "Request returned no data");
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e("REST", "Request to retrieve string info failed: " + t.toString());
                    Toast.makeText(context, "Failed to retrieve string info", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}