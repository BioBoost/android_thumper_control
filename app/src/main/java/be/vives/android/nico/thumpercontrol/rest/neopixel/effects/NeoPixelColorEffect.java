package be.vives.android.nico.thumpercontrol.rest.neopixel.effects;

/**
 * Created by Nico De Witte on 11/22/2015.
 */
public class NeoPixelColorEffect {
    private int red;
    private int green;
    private int blue;

    public NeoPixelColorEffect() {
        this(0, 0, 0);
    }

    public NeoPixelColorEffect(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }
}
