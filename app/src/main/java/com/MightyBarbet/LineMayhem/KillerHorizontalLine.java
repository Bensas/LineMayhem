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
    public Paint whitePaint = new Paint();
    public Paint redPaint = new Paint();
    public int alphaCounter = 0;
    Spawner spawner;
    float startX, startY, currentEndX, currentEndY;
    float speed, rate;
    long ETA;
    float[] endXs, endYs;
    int updateIndex;
    //startingSide: 0 is left, 1 is right --- direction: 0 is downwards, 1 is upwards
    boolean startingSide, direction;
    //0 means inactive, 1 means extending, 2 means exploding, 3 means hasExploded (post-explosion animation is playing)
    int state;

    public KillerHorizontalLine (Spawner spawner){
        this.spawner = spawner;
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
        startingSide = rnd.nextBoolean();
        direction = rnd.nextBoolean();
        speed = rnd.nextFloat() * 11f;

        if (speed <= 3.5f){
            this.speed = 3.5f;
        }
        ETA = System.nanoTime() + (long)((long)(1000000000/60) * (Globals.GAME_WIDTH / speed));

        startY = playerY + rnd.nextInt(400) - 200;
        if (startY > Globals.GAME_HEIGHT - Globals.BOUNDARY_WIDTH - 20){
            startY = Globals.GAME_HEIGHT - Globals.BOUNDARY_WIDTH - 20;
        } else if (startY < Globals.BOUNDARY_WIDTH + 20){
            startY = Globals.BOUNDARY_WIDTH + 20;
        }

        float randomFloat = rnd.nextFloat();
        if (startY <= playerY){
            rate = randomFloat>0.1f?randomFloat * ((Globals.GAME_HEIGHT - startY) / Globals.GAME_WIDTH) : 0.1f * ((Globals.GAME_HEIGHT - startY) / Globals.GAME_WIDTH);
        } else {
            rate = randomFloat>0.3f?-randomFloat * (startY/Globals.GAME_WIDTH) : -0.1f * (startY/Globals.GAME_WIDTH);
        }

        if (!startingSide){
            startX = 0;
            endXs = new float[(int)(Globals.GAME_WIDTH / speed) + 10]; //The 10 is to have a margin. It could be 1.
            endYs = new float[endXs.length + 10];
            for (int i = 0; i < endXs.length; i++){
                endXs[i] = speed * i;
                endYs[i] = rate * endXs[i] + startY;
            }
        } else {
            startX = Globals.GAME_WIDTH;
            endXs = new float[(int)(Globals.GAME_WIDTH / speed) + 10];
            endYs = new float[endXs.length + 10];
            for (int i = 0; i < endXs.length; i++){
                endXs[i] = startX - speed * i;
                endYs[i] = rate * (Globals.GAME_WIDTH - endXs[i]) + startY;
            }
        }
        currentEndY = startY;
        currentEndX = startX;

        Log.d(getClass().getCanonicalName(), "Horizontal Line - Rate: " + rate + " - Current time: " + System.nanoTime() + " - ETA: " + ETA);

        state = 1;
        alphaCounter = 0;
        updateIndex = 0;
    }

    //This method allows manually setting of the attributes to create custom lines. It's used in the instructions menu and in multiplayer mode.
    public void resetLineWithCustomAttributes(boolean startingSide, boolean direction,float speed, int startX, int startY, float rate){
        this.startingSide = startingSide;
        this.direction = direction;
        this.speed = speed;
        this.rate = rate;

        if (!startingSide){
            this.startY = startY;
            this.startX = 0;
            endXs = new float[(int)(Globals.GAME_WIDTH / speed) + 10]; //The 10 is to have a margin. It could be 1.
            endYs = new float[endXs.length + 10];
            for (int i = 0; i < endXs.length; i++){
                endXs[i] = speed * i;
                endYs[i] = rate * endXs[i] + startY;
            }
        } else {
            this.startY = startY;
            this.startX = Globals.GAME_WIDTH;
            endXs = new float[(int)(Globals.GAME_WIDTH / speed) + 10];
            endYs = new float[endXs.length + 10];
            for (int i = 0; i < endXs.length; i++){
                endXs[i] = startX - speed * i;
                endYs[i] = rate * (Globals.GAME_WIDTH - endXs[i]) + startY;
            }
        }

        if (spawner.mainGame.multiplayer) {
            if (!spawner.firstLineLaunched){
                currentEndX = endXs[0];
                currentEndY = endYs[0];
                spawner.resetTimer();
                spawner.firstLineLaunched = true;
            }else{
                try{
                    currentEndX = endXs[-spawner.timer];
                    currentEndY = endYs[-spawner.timer];
                    updateIndex = -spawner.timer;
                } catch (ArrayIndexOutOfBoundsException e){
                    Log.d("HorizontalLine", "Spawner timer does not fit array: " + spawner.timer);
                    Log.d("HorizontalLine", "Array size: " + endXs.length);

                    currentEndX = endXs[0];
                    currentEndY = endYs[0];
                }

                spawner.resetTimer();
            }

        } else {
            currentEndX = endXs[0];
            currentEndY = endYs[0];
            updateIndex = 0;
        }

        state = 1;
        alphaCounter = 0;

    }

    public void update(){
        if (state == 1){
            if (!startingSide){
                currentEndY = endYs[updateIndex];
                currentEndX = endXs[updateIndex];
                //endX += speed;
                //endY = rate * endX + startY;
                if (currentEndX >= Globals.GAME_WIDTH){
                    //Log.d(getClass().getName(), "Horizontal Line - Rate: " + rate + " - Arrival time: " + System.nanoTime());
                    state = 2;
                    //redPaint.setAlpha(100);
                }
            } else {
                currentEndY = endYs[updateIndex];
                currentEndX = endXs[updateIndex];
                //endX -= speed;
                //endY = rate * (Globals.GAME_WIDTH - endX) + startY;
                if (currentEndX <= 0){
                    //Log.d(getClass().getName(), "Horizontal Line - Rate: " + rate + " - Arrival time: " + System.nanoTime());

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
        }
    }

    public void explodeLine(Canvas canvas, Paint paint){
        Path path = new Path();
        path.moveTo(startX, startY);
        path.lineTo(currentEndX, currentEndY);
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
