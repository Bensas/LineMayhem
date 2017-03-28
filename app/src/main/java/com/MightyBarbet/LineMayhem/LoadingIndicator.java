package com.MightyBarbet.LineMayhem;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

/**
 * Created by Bensas on 3/3/17.
 */
public class LoadingIndicator {
    Bitmap texture;
    Paint paint;
    Matrix rotator;

    TextButton button;

    int x, y, gameWidth = 60, gameHeight = 60;
    int rotationCounter;
    boolean isVisible = false;

    public LoadingIndicator(Context context){
        texture = BitmapFactory.decodeResource(context.getResources(), R.drawable.loading_indicator);
        paint = new Paint();
        rotator = new Matrix();
    }

    public void setButton(TextButton button){
        this.button = button;
    }

    public void draw(Canvas canvas){
        Matrix rotator = new Matrix();
        //20 is the size of the outer margin of the TextButtons
        //(the boundaries contain only the text)
        x = button.x - button.boundaries.width()/2 - gameWidth - 20 - 10;
        y = button.y - button.boundaries.height() - 10;
        rotator.postScale((float)gameWidth/texture.getWidth(), (float)gameHeight/texture.getHeight());
        rotator.postTranslate(x, y);
        rotator.postRotate(rotationCounter, x + gameWidth/2, y + gameHeight/2);
        //canvas.rotate(rotationCounter);
        canvas.drawBitmap(texture, rotator, paint);
        //canvas.rotate(-rotationCounter);
        rotationCounter += 6;
    }
}
