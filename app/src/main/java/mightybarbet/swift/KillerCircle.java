package mightybarbet.swift;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by Bensas on 22/07/16.(july)
 */
public class KillerCircle {
    //0 means extending, 1 means exploding, 2 means hasexploded
    int state;
    public KillerCircle(){

    }

    public void draw(Canvas canvas){
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);

        canvas.drawArc(300, 300, 300, 300, 0, 180, false, paint);
    }
}
