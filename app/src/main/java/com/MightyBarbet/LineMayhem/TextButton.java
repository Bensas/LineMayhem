package com.MightyBarbet.LineMayhem;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

/**
 * Created by Bensas on 21/07/16. (july)
 */

public class TextButton {
    Typeface typeface;
    int x, y, defaultX, defaultY, fontSize, statePointer; //The state pointer determines what state the game will change to when the button is pressed.
                                      //If it's 0, it means it's not a state changing button.
                                      //In this case, the button handler will check the text and act specifically.
    String text;
    boolean isPressable;

    int isPressed = 0; //Would use a boolean if I could interact with it as 0 or 1 without too much hassle
    Rect boundaries;
    Paint paint;
    int color;
    public TextButton(int x, int y, int fontSize, String text, Paint.Align alignment, boolean isPressable, int statePointer, Context context){
        defaultX = x;
        defaultY = y;
        this.x = defaultX;
        this.y = defaultY;

        this.fontSize = fontSize;
        this.text = text;
        this.isPressable = isPressable;
        this.statePointer = statePointer;

        paint = new Paint();
        paint.setTextAlign(alignment);
        paint.setTextSize(fontSize);
        typeface = Typeface.createFromAsset(context.getAssets(), "fonts/pixelmix.ttf");
        paint.setTypeface(typeface);
        color = Color.WHITE;

        //getTextBounds generates a rectangle on the origin, storing it on the boundaries Rect. We then position it at the button's location.
        boundaries = new Rect();
        paint.getTextBounds(text, 0, text.length(), boundaries);
        boundaries = new Rect(x - boundaries.width()/2, y - boundaries.height(), x + boundaries.width()/2, y);

    }

    public void draw(Canvas canvas){
        //boundaries = new Rect(x - boundaries.width()/2, y - boundaries.height(), x + boundaries.width()/2, y);
        if (isPressable){
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.RED);
            paint.setStrokeWidth(4);
            canvas.drawRect(boundaries.left-16, boundaries.top-16 + isPressed*12, boundaries.right+16, boundaries.bottom+16 + isPressed*12, paint);
            paint.setColor(Color.WHITE);
            paint.setStrokeWidth(5);
            canvas.drawRect(boundaries.left-20, boundaries.top-20 + isPressed*12, boundaries.right+20, boundaries.bottom+20 + isPressed*12, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);

        //When the button is being pressed, it moves down a bit to indicate so.
        canvas.drawText(text, x, y + isPressed*12, paint);
    }

    public void setFontSize(int size){
        paint.setTextSize(size);
    }
    public void setText(String newText){
        text = newText;
        boundaries = new Rect();
        paint.getTextBounds(text, 0, text.length(), boundaries);
        boundaries = new Rect(x - boundaries.width()/2, y - boundaries.height(), x + boundaries.width()/2, y);
    }

    public void setColor(int color){
        this.color = color;
    }
}
