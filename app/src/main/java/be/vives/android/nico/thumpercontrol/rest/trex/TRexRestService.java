package be.vives.android.nico.thumpercontrol.rest.trex;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by Nico De Witte on 12/1/2015.
 */
public interface TRexRestService {
    @POST("speed")
    Call<ThumperStatusReport> setThumperSpeed(@Body ThumperSpeed speed);
}