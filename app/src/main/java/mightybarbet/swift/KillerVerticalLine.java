package mightybarbet.swift;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.SoundPool;
import android.provider.Settings;
import android.util.Log;

import java.util.Random;

/**
 * Created by Bensas on 02/06/16. (june)
 */
public class KillerVerticalLine {
    SoundPool soundPool;

    //startX and startY are assigned at the start and remain static.
    //endX and endY are modified on every update to make the line extend.
    float startX, startY, endX, endY;
    float speed, rate;

    //startingSide: 0 is bottom, 1 is top --- direction: 0 is leftwards, 1 is rightwards
    boolean startingSide, direction;

    //0 means inactive, 1 means extending, 2 means exploding, 3 means hasExploded (post-explosion animation is playing)
    int state;

    public Paint whitePaint;
    public Paint redPaint;
    public int alphaCounter = 0;

    public KillerVerticalLine (){
        state = 0;

        whitePaint = new Paint();
        redPaint = new Paint();

        whitePaint.setColor(Color.WHITE);
        whitePaint.setStrokeWidth(6);
        whitePaint.setStyle(Paint.Style.FILL);
        redPaint.setColor(Color.RED);
        redPaint.setStrokeWidth(5);
        redPaint.setStyle(Paint.Style.FILL);
    }

    public void resetLine(int playerX, Random rnd){
        startingSide = rnd.nextBoolean();
        direction = rnd.nextBoolean();
        speed = 10f;
        if (speed <= 3){
            this.speed = 3;
        }

        startX = playerX + rnd.nextInt(350) - 175;

        if (!startingSide){
            startY = Globals.GAME_HEIGHT;
            endY = startY;
            endX = startX;
        } else {
            startY = 0;
            endY = startY;
            endX = startX;
        }

        if (startX <= playerX){
            rate = rnd.nextFloat() * ((Globals.GAME_WIDTH - startX) / Globals.GAME_HEIGHT);
        } else {
            rate = -rnd.nextFloat() * (startX/Globals.GAME_HEIGHT);
        }

        state = 1;
        alphaCounter = 0;
    }

    public void update(){
        if (state == 1){
            if (!startingSide){
                endY -= speed;
                endX = rate * (Globals.GAME_HEIGHT - endY) + startX;
                if (endY <= 0){
                    state = 2;
                    //redPaint.setAlpha(100);
                }
            } else {
                endY += speed;
                endX = rate * endY + startX;
                if (endY >= Globals.GAME_HEIGHT){
                    state = 2;
                    //redPaint.setAlpha(100);
                }
            }
        }

    }

    public void draw(Canvas canvas){
        switch (state){
            case 1:
                //Log.d(getClass().getSimpleName(), "Drawing lnine, rate: " + rate);
                canvas.drawLine(startX, startY, endX, endY, whitePaint);
                if (direction){
                    canvas.drawLine(startX + 5, startY, endX + 5, endY, redPaint);
                } else {
                    canvas.drawLine(startX - 5, startY, endX - 5, endY, redPaint);
                } break;
            case 2:
                //Log.d(getClass().getSimpleName(), "State: 2, rate: " + rate);


                //soundPool.release()
                explodeLine(canvas, redPaint);
                state = 3;
                break;
            case 3:
                //Log.d(getClass().getSimpleName(), "State: 3, rate: " + rate);

                alphaCounter += 1;
                if (alphaCounter >= 10){
                    state = 4;
                }
                break;
            case 4:
                //Log.d(getClass().getSimpleName(), "State: 4, rate: " + rate);
                break;
            case 0:
                //Log.d(getClass().getSimpleName(), "State: 0, rate: " + rate);

                break;
        }
    }

    public void explodeLine(Canvas canvas, Paint whitePaint){
        Path path = new Path();
        path.moveTo(startX, startY);
        path.lineTo(endX, endY);
        if (!startingSide){
            if (direction){
                path.lineTo(Globals.GAME_WIDTH, 0);
                path.lineTo(Globals.GAME_WIDTH, Globals.GAME_HEIGHT);
            } else {
                path.lineTo(0, 0);
                path.lineTo(0, Globals.GAME_HEIGHT);
            }
        } else {
            if (direction){
                path.lineTo(Globals.GAME_WIDTH, Globals.GAME_HEIGHT);
                path.lineTo(Globals.GAME_WIDTH, 0);
            } else {
                path.lineTo(0, Globals.GAME_HEIGHT);
                path.lineTo(0, 0);
            }
        }
        path.lineTo(startX, startY);
        canvas.drawPath(path, whitePaint);
    }
}
