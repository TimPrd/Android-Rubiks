package com.rodpil.rubik;

public class Cubie {

    private int x;
    private int y;
    private double[] color;
    private int indexCoul;

    public Cubie(int x, int y, double[] color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }


    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }


    public String getClosestColor() {
        double diff = 9999;
        String res = "";
        for (EColors c : EColors.values()) {
            if (this.color != null) {
                double tmp = (Math.abs(this.color[0] - Integer.valueOf(c.getHex().substring(1, 3), 16)) +
                        Math.abs(this.color[1] - Integer.valueOf(c.getHex().substring(3, 5), 16)) +
                        Math.abs(this.color[1] - Integer.valueOf(c.getHex().substring(5, 7), 16)));
                if (tmp < diff) {
                    diff = tmp;
                    res = c.getHex();
                }
            }
        }

        return res;
    }


    public double[] getColor() {
        return color;
    }

    public int getIndexCoul() {
        return indexCoul;
    }
}
