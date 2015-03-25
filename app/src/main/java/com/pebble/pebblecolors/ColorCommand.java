package com.pebble.pebblecolors;

import android.graphics.Color;

/**
 * Created by joe-work on 3/24/15.
 */
public class ColorCommand {

    private static final int DEFAULT_COLOR = 127;

    public byte type;
    public int r;
    public int g;
    public int b;

    public ColorCommand(){
        this.type = 0x02;
        this.r = DEFAULT_COLOR;
        this.g = DEFAULT_COLOR;
        this.b = DEFAULT_COLOR;
    }

    public ColorCommand(final byte type, final int r, final int g, final int b) {
        this.type = type;
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public int getAbsoluteColor() {
        return Color.rgb(r, g, b);
    }

    @Override
    public String toString() {
        return String.format("TYPE: %d R: %d G: %d B: %d", type, r, g, b);
    }
}
