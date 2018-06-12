package com.rodpil.rubik.Cube;

import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.List;

/**
 * Enumeration of the colors of the cube
 */
public enum EColors {
    ORANGE("#ff9900", new Scalar(6, 100, 100, 0), new Scalar(11, 255, 255, 0), 0),
    YELLOW("#ffff66", new Scalar(20, 100, 100, 0), new Scalar(30, 255, 255, 0), 1),
    GREEN("#66ff33", new Scalar(46, 100, 100, 0), new Scalar(100, 255, 255, 0), 2),
    BLUE("#3366ff", new Scalar(101, 100, 100, 0), new Scalar(150, 255, 255, 0), 3),
    RED("#ff0000", new Scalar(160, 100, 100, 0), new Scalar(180, 255, 255, 0), 4),
    WHITE("#ffffff", new Scalar(0, 0, 245), new Scalar(180, 70, 255), 5);


    private Scalar lower;
    private Scalar higher;
    private int bind;
    private String hex;

    EColors(String hex, Scalar lowestBound, Scalar highestBound, int bind) {
        this.hex = hex;
        this.lower = lowestBound;
        this.higher = highestBound;
        this.bind = bind;
    }

    /**
     * Retrieve a color of the enum by a color in hexa
     * @param hex color detected
     * @return the cubie color related
     */
    public static EColors retrieveByCoul(String hex) {
        for (EColors e : values()) {
            if (("#" + hex).equals(e.hex))
                return e;
        }
        return null;
    }

    /**
     * Retrieve the binding of cube (1,2..6)
     * @param num the binding to search for
     * @return the cube associated with this binding
     */
    public static EColors retrieveByBind(int num) {
        for (EColors e : values()) {
            if (num == e.bind)
                return e;
        }
        return null;
    }

    /**
     * @return fetch all the possible colors
     */
    public static String[] colors() {
        List<String> alColors = new ArrayList<>();
        for (EColors color : EColors.values()) {
            alColors.add(color.hex);
        }
        return alColors.toArray(new String[0]);
    }

    public Scalar getLower() {
        return lower;
    }

    public Scalar getHigher() {
        return higher;
    }

    public int getBind() {
        return bind;
    }

    public String getHex() {
        return hex;
    }
}
