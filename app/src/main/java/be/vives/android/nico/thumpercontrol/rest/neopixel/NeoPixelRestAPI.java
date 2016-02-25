package be.vives.android.nico.thumpercontrol.rest.neopixel;

import be.vives.android.nico.thumpercontrol.rest.neopixel.effects.NeoPixelColorEffect;
import be.vives.android.nico.thumpercontrol.rest.neopixel.effects.NeoPixelStrobeEffect;
import be.vives.android.nico.thumpercontrol.rest.StatusReport;
import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

public interface NeoPixelRestAPI {
    // Do NOT start path with slash !!!
    @GET("neopixels/strings/{id}")
    Call<NeoPixelString> getResponse(@Path("id") String stringId);

    @POST("neopixels/strings/{id}")
    Call<StatusReport> setStringColor(@Path("id") String stringId,
                                      @Body NeoPixelColorEffect effect);

    @POST("neopixels/effects/strobe/{id}")
    Call<StatusReport> strobe(@Path("id") String stringId,
                              @Body NeoPixelStrobeEffect effect);
}
