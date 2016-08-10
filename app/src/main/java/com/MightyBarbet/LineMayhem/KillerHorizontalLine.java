package com.MightyBarbet.LineMayhem;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.SoundPool;
import android.util.Log;

import java.util.Random;

/**
 * Created by Bensas on 01/06/16. (june)
 */
public class KillerHorizontalLine {
    Random rnd = new Random();
    //MediaPlayer mediaPlayer;
    SoundPool soundPool;

    //startX and startY are assigned at the start and remain static.
    //endX and endY are modified on every update to make the line extend.
    float startX, startY, endX, endY;
    float speed, rate;

    float[] endXs, endYs;
    int updateIndex;

    //startingSide: 0 is left, 1 is right --- direction: 0 is downwards, 1 is upwards
    boolean startingSide, direction;

    //0 means inactive, 1 means extending, 2 means exploding, 3 means hasExploded (post-explosion animation is playing)
    int state;

    public Paint whitePaint = new Paint();
    public Paint redPaint = new Paint();
    public int alphaCounter = 0;

    public KillerHorizontalLine (){
        state = 0;

        whitePaint.setColor(Color.WHITE);
        whitePaint.setStrokeWidth(6);
        whitePaint.setStyle(Paint.Style.FILL);
        redPaint.setColor(Color.RED);
        redPaint.setStrokeWidth(5);
        redPaint.setStyle(Paint.Style.FILL);
    }

    //This method resets the attributes of the line to random values. It's used during the game.
    public void resetLine(int playerY, Random rnd){
        Log.d(getClass().getSimpleName(), "Resetting horizontal line...");
        startingSide = rnd.nextBoolean();
        direction = rnd.nextBoolean();
        speed = rnd.nextFloat() * 11f;
        if (speed <= 4){
            this.speed = 4;
        }

        startY = playerY + rnd.nextInt(400) - 200;
        if (startY > Globals.GAME_HEIGHT){
            startY = Globals.GAME_HEIGHT;
        }

        if (startY <= playerY){
            rate = rnd.nextFloat() * ((Globals.GAME_HEIGHT - startY) / Globals.GAME_WIDTH);
        } else {
            rate = -rnd.nextFloat() * (startY/Globals.GAME_WIDTH);
        }

        if (!startingSide){
            startX = 0;
            endXs = new float[(int)(Globals.GAME_WIDTH / speed) + 10]; //The 10 is to have a margin. It could be 1.
            endYs = new float[endXs.length + 10];
            for (int i = 0; i < endXs.length; i++){
                endXs[i] = speed * i;
                endYs[i] = rate * endXs[i] + startY;
            }
            endX = startX;
            endY = startY;
        } else {
            startX = Globals.GAME_WIDTH;
            endXs = new float[(int)(Globals.GAME_WIDTH / speed) + 10];
            endYs = new float[endXs.length + 10];
            for (int i = 0; i < endXs.length; i++){
                endXs[i] = startX - speed * i;
                endYs[i] = rate * (Globals.GAME_WIDTH - endXs[i]) + startY;
            }
            endX = startX;
            endY = startY;
        }


        state = 1;
        alphaCounter = 0;
        updateIndex = 0;
    }

    //This method allows manually setting of the attributes to create custom lines. It's used in the instructions menu.
    public void resetLineWithCustomAttributes(boolean startingSide, boolean direction,float speed, int startX, int startY, float rate){
        this.startingSide = startingSide;
        this.direction = direction;
        this.speed = speed;
        this.rate = rate * ((Globals.GAME_HEIGHT - startY) / Globals.GAME_WIDTH);

        if (!startingSide){
            this.startY = startY;
            this.startX = 0;
            endXs = new float[(int)(Globals.GAME_WIDTH / speed) + 10]; //The 10 is to have a margin. It could be 1.
            endYs = new float[endXs.length + 10];
            for (int i = 0; i < endXs.length; i++){
                endXs[i] = speed * i;
                endYs[i] = rate * endXs[i] + startY;
            }
            endX = startX;
            endY = startY;
        } else {
            this.startY = startY;
            this.startX = Globals.GAME_WIDTH;
            endXs = new float[(int)(Globals.GAME_WIDTH / speed) + 10];
            endYs = new float[endXs.length + 10];
            for (int i = 0; i < endXs.length; i++){
                endXs[i] = startX - speed * i;
                endYs[i] = rate * (Globals.GAME_WIDTH - endXs[i]) + startY;
            }
            endX = startX;
            endY = startY;
        }
        state = 1;
        alphaCounter = 0;
        updateIndex = 0;
    }

    public void update(){
        if (state == 1){
            if (!startingSide){
                endY = endYs[updateIndex];
                endX = endXs[updateIndex];
                //endX += speed;
                //endY = rate * endX + startY;
                if (endX >= Globals.GAME_WIDTH){
                    state = 2;
                    //redPaint.setAlpha(100); TODO: FIGURE OUT HOW TO CHANGE ALPHA WITHOUT TAKING A SHIT ON FPS
                }
            } else {
                endY = endYs[updateIndex];
                endX = endXs[updateIndex];
                //endX -= speed;
                //endY = rate * (Globals.GAME_WIDTH - endX) + startY;
                if (endX <= 0){
                    state = 2;
                    //redPaint.setAlpha(100);
                }
            }
            updateIndex++;
        }
    }

    public void draw(Canvas canvas){

        switch (state){
            case 1:
                //Log.d(getClass().getSimpleName(), "Drawing lnine, rate: " + rate);
                canvas.drawLine(startX, startY, endXs[updateIndex], endYs[updateIndex], whitePaint);
                if (direction){
                    canvas.drawLine(startX, startY - 5, endXs[updateIndex], endYs[updateIndex] - 5, redPaint);
                } else {
                    canvas.drawLine(startX, startY + 5, endXs[updateIndex], endYs[updateIndex] + 5, redPaint);
                } break;
            case 2:
                //Log.d(getClass().getSimpleName(), "State: 2, rate: " + rate);

                //soundPool.release();
                explodeLine(canvas, redPaint);
                state = 3;
                break;
            case 3:
                //Log.d(getClass().getSimpleName(), "State: 3, rate: " + rate);
                explodeLine(canvas, redPaint);
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

    public void explodeLine(Canvas canvas, Paint paint){
        Path path = new Path();
        path.moveTo(startX, startY);
        path.lineTo(endX, endY);
        if (!startingSide){
            if (direction){
                path.lineTo(Globals.GAME_WIDTH, 0);
                path.lineTo(0, 0);
            } else {
                path.lineTo(Globals.GAME_WIDTH, Globals.GAME_HEIGHT);
                path.lineTo(0, Globals.GAME_HEIGHT);
            }
        } else {
            if (direction){
                path.lineTo(0, 0);
                path.lineTo(Globals.GAME_WIDTH, 0);
            } else {
                path.lineTo(0, Globals.GAME_HEIGHT);
                path.lineTo(Globals.GAME_WIDTH, Globals.GAME_HEIGHT);
            }
        }
        path.lineTo(startX, startY);
        canvas.drawPath(path, paint);
    }
}
