package mightybarbet.swift;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by Bensas on 7/25/16.
 */
public class TextLogo extends TextButton {
    public TextLogo(int x, int y, int fontSize, String text, Paint.Align alignment, boolean isPressable, int statePointer, Context context) {
        super(x, y, fontSize, text, alignment, isPressable, statePointer, context);
    }
    @Override
    public void draw(Canvas canvas){
        paint.setColor(Color.WHITE);
        //paint.setStyle(Paint.Style.FILL);
        //canvas.drawText(text, x, y, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(8);
        canvas.drawText(text, x, y + isPressed*7, paint);
        paint.setStrokeWidth(7);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(fontSize);
        paint.setColor(Color.RED);
        canvas.drawText(text, x, y + isPressed*7, paint);
    }
}
