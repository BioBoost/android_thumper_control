package be.vives.android.nico.thumpercontrol;

public class NeoPixelStringColor {
    private int red;
    private int green;
    private int blue;

    public NeoPixelStringColor() {
        this(0, 0, 0);
    }

    public NeoPixelStringColor(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }
}
