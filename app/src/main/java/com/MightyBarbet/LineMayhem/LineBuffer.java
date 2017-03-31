package com.MightyBarbet.LineMayhem;

/**
 * Created by Bensas on 3/30/17.
 */
public class LineBuffer {
    short[] lineTypes, lineDirections, lineStartingSides;
    float[] lineSpeedRnds, lineRateFloatRnds;
    short[] lineStartRnds;

    public LineBuffer(int size){
        lineTypes = new short[size];
        lineDirections = new short[size];
        lineStartingSides = new short[size];

        lineSpeedRnds = new float[size];
        lineRateFloatRnds= new float[size];

        lineStartRnds = new short[size];
    }
}
