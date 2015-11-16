package be.vives.android.nico.thumpercontrol;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;

public interface NeoPixelService {
    @GET("/neopixels/strings/{id}")
    Call<NeoPixelString> getResponse(@Path("id") String stringId);
}
