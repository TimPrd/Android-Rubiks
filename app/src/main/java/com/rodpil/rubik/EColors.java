package com.rodpil.rubik;

import android.graphics.Color;

import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public enum EColors {
    ORANGE ( "#ff9900", new Scalar(6,100,100,0)  , new Scalar(11,255,255,0) , 0),
    YELLOW ( "#ffff66", new Scalar(20,100,100,0) , new Scalar(30,255,255,0) , 1),
    GREEN  ( "#66ff33", new Scalar(46,100,100,0) , new Scalar(100,255,255,0), 2),
    BLUE   ( "#3366ff", new Scalar(101,100,100,0), new Scalar(150,255,255,0), 3),
    RED    ( "#ff0000", new Scalar(160,100,100,0), new Scalar(180,255,255,0), 4),
    WHITE  ( "#ffffff", new Scalar(0,0,245)      , new Scalar(180,70,255)   , 5);


    private Scalar lower;
    private Scalar higher;
    private int bind;
    private String hex;

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

    public static EColors retrieveByCoul(String hex){
        for (EColors e: values()) {
            if (("#"+hex).equals(e.hex))
                return e;
        }
        return null;
    }

    public static EColors retrieveByBind(int num){
        for (EColors e: values()) {
            if (num == e.bind)
                return e;
        }
        return null;
    }

    public static String[] colors(){
        List<String> alColors = new ArrayList<>();
        for (EColors color : EColors.values()) {
            alColors.add(color.hex);
        }
        return alColors.toArray(new String[0]);
    }

    EColors(String hex, Scalar lowestBound, Scalar highestBound, int bind) {
        this.hex = hex;
        this.lower = lowestBound;
        this.higher= highestBound;
        this.bind  = bind;
    }
}
