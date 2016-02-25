package be.vives.android.nico.thumpercontrol.rest.trex;

/**
 * Created by Nico De Witte on 12/1/2015.
 */
public class ThumperSpeed {
    private int left_speed;
    private int right_speed;

    public ThumperSpeed() {
        this(0, 0);
    }

    public ThumperSpeed(int left_speed, int right_speed) {
        this.left_speed = left_speed;
        this.right_speed = right_speed;
    }
}
