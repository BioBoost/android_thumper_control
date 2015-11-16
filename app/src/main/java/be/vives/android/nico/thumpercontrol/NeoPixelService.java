package be.vives.android.nico.thumpercontrol;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

public interface NeoPixelService {
    @GET("/neopixels/strings/{id}")
    Call<NeoPixelString> getResponse(@Path("id") String stringId);

    @POST("/neopixels/strings/{id}")
    Call<NeoPixelStringColor> setStringColor(@Path("id") String stringId, @Body NeoPixelStringColor color);
}
