package be.vives.android.nico.thumpercontrol;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by Nico De Witte on 12/1/2015.
 */
public interface ThumperControlRestService {
    @POST("speed")
    Call<ThumperStatusReport> setThumperSpeed(@Body ThumperSpeed speed);
}