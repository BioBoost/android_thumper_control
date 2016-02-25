package be.vives.android.nico.thumpercontrol.rest.neopixel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class NeoPixelString {

    // @Expose = An annotation that indicates this member
    // should be exposed for JSON serialization or deserialization.

    @SerializedName("string_id")
    @Expose
    private String stringId;

    @SerializedName("number_of_pixels")
    @Expose
    private String numberOfPixels;

    public String getStringId() {
        return stringId;
    }

    public String getNumberOfPixel() {
        return numberOfPixels;
    }
}
