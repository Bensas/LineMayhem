package com.MightyBarbet.LineMayhem;

import android.graphics.Bitmap;

/**
 * Created by Bensas on 2/9/18.
 */
public class Skin {
    Bitmap bitmap;
    String name;
    int price;
    public Skin(String name, Bitmap bitmap, int price){
        this.name = name;
        this.bitmap = bitmap;
        this.price = price;
    }
}
