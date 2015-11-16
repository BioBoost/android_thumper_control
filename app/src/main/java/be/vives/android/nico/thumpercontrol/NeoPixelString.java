package be.vives.android.nico.thumpercontrol;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class NeoPixelString implements Parcelable {

    //@Expose = An annotation that indicates this member should be exposed for JSON serialization or deserialization.

    @SerializedName("string_id")
    @Expose
    private String stringId;

    @SerializedName("number_of_pixels")
    @Expose
    private String numberOfPixels;

    public String getStringId() {
        return stringId;
    }

    public void setStringId(String id) {
        this.stringId = id;
    }

    public String getNumberOfPixel() {
        return numberOfPixels;
    }

    public void setNumberOfPixels(String count) {
        this.numberOfPixels = count;
    }

    protected NeoPixelString(Parcel in) {
        stringId = in.readString();
        numberOfPixels = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(stringId);
        dest.writeString(numberOfPixels);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<NeoPixelString> CREATOR = new Parcelable.Creator<NeoPixelString>() {
        @Override
        public NeoPixelString createFromParcel(Parcel in) {
            return new NeoPixelString(in);
        }

        @Override
        public NeoPixelString[] newArray(int size) {
            return new NeoPixelString[size];
        }
    };
}

