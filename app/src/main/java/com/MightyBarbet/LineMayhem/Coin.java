package com.MightyBarbet.LineMayhem;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by Bensas on 2/11/18.
 */
public class Coin {
    final static int STATE_APPEARING = 0, STATE_IDLE = 1, STATE_COLLECTED = 2, STATE_INVISIBLE = 3;
    int x, y, state;

    Paint paint;

    Bitmap[] animAppear, animIdle, animCollected;
    Bitmap currentFrame;
    int counter = 0, framePointer = 0;

    public Coin(MainGameScript mainGame){

        animAppear = new Bitmap[]{
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_appear0001), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_appear0002), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_appear0003), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_appear0004), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_appear0005), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_appear0006), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_appear0007), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_appear0008), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_appear0009), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_appear0010), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_appear0011), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_appear0012), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_appear0013), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_appear0014), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_appear0015), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_appear0016), 180, 131, false)
        };
        animIdle = new Bitmap[]{
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_idle1), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_idle2), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_idle3), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_idle4), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_idle5), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_idle6), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_idle7), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_idle8), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_idle9), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_idle10), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_idle11), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_idle12), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_idle12), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_idle12), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_idle11), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_idle10), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_idle9), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_idle8), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_idle7), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_idle6), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_idle5), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_idle4), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_idle3), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_idle2), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_idle1), 180, 131, false)
        };

        animCollected = new Bitmap[]{
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_appear0016), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_appear0015), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_appear0014), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_appear0013), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_appear0012), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_appear0011), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_appear0010), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_appear0009), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_appear0008), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_appear0007), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_appear0006), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_appear0005), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_appear0004), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_appear0003), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_appear0002), 180, 131, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mainGame.getResources(), R.drawable.coin_appear0001), 180, 131, false)
        };
        setState(STATE_APPEARING);
        currentFrame = animAppear[0];

    }

    public void setState(int newState){
        state = newState;
        framePointer = 0;
        switch (state){
            case STATE_APPEARING:
                currentFrame = animAppear[framePointer];
                break;
            case STATE_IDLE:
                currentFrame = animIdle[framePointer];
                break;
            case STATE_COLLECTED:
                currentFrame = animCollected[framePointer];
                break;
        }
    }

    public void update(){
        switch (state){
            case STATE_APPEARING:
                framePointer++;
                if (framePointer > animAppear.length-1)
                    setState(STATE_IDLE);
                else
                    currentFrame = animAppear[framePointer];
                break;
            case STATE_IDLE:
                if (framePointer > animIdle.length-1)
                    framePointer = 0;
                else
                    currentFrame = animIdle[framePointer];
                framePointer++;
                break;
            case STATE_COLLECTED:

                if (framePointer > animCollected.length-1){
                    setState(STATE_INVISIBLE);
                    x = -100;
                    y = -100;
                    framePointer = 0;
                }
                else
                    currentFrame = animCollected[framePointer];
                framePointer++;
                break;
            case STATE_INVISIBLE:
                break;
        }

    }

    public void draw(Canvas canvas){
        canvas.drawBitmap(currentFrame, x - currentFrame.getWidth()/2, y -currentFrame.getHeight()/2, paint);
    }
}
