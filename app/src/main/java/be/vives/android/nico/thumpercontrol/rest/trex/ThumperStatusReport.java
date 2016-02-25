package be.vives.android.nico.thumpercontrol.rest.trex;

import com.google.gson.annotations.Expose;

/**
 * Created by Nico De Witte on 12/7/2015.
 */
public class ThumperStatusReport {
    @Expose
    private float battery_voltage;

    public float getBatteryVoltage() {
        return battery_voltage;
    }

}
