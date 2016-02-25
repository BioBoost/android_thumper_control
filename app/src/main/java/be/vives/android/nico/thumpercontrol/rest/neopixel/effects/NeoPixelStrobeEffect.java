package be.vives.android.nico.thumpercontrol.rest.neopixel.effects;

/**
 * Created by Nico De Witte on 11/22/2015.
 */
public class NeoPixelStrobeEffect {
    private int red;
    private int green;
    private int blue;
    private int delay;

    public NeoPixelStrobeEffect() {
        this(0, 0, 0, 0);
    }

    public NeoPixelStrobeEffect(int red, int green, int blue, int delay) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.delay = delay;
    }
}
