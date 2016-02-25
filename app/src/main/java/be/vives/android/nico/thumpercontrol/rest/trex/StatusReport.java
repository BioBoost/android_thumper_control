package be.vives.android.nico.thumpercontrol.rest.trex;

import com.google.gson.annotations.Expose;

/**
 * Created by Nico De Witte on 11/22/2015.
 */
public class StatusReport {
    // @Expose = An annotation that indicates this member
    // should be exposed for JSON serialization or deserialization.

    @Expose
    private String status;

    public String getStatus() {
        return status;
    }
}
