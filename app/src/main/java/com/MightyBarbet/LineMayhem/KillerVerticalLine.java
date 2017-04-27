package com.MightyBarbet.LineMayhem;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.SoundPool;
import android.util.Log;

import java.util.Random;

/**
 * Created by Bensas on 02/06/16. (june)
 */
public class KillerVerticalLine {
    public Paint whitePaint;
    public Paint redPaint;
    public int alphaCounter = 0;
    Spawner spawner;
    float startX, startY, currentEndX, currentEndY;
    float[] endXs, endYs;
    float speed, rate;
    long startTime, ETA;
    int updateIndex;
    //startingSide: 0 is bottom, 1 is top --- direction: 0 is leftwards, 1 is rightwards
    boolean startingSide, direction;
    //0 means inactive, 1 means extending, 2 means exploding, 3 means hasExploded (post-explosion animation is playing)
    int state;

    public KillerVerticalLine (Spawner spawner){
        state = 0;
        this.spawner = spawner;

        whitePaint = new Paint();
        redPaint = new Paint();

        whitePaint.setColor(Color.WHITE);
        whitePaint.setStrokeWidth(5);
        whitePaint.setStyle(Paint.Style.FILL);
        redPaint.setColor(Color.RED);
        redPaint.setStrokeWidth(5);
        redPaint.setStyle(Paint.Style.FILL);
    }

    //This method resets the attributes of the line to random values. It's used during the game.
    public void resetLine(int playerX, Random rnd){
        startingSide = rnd.nextBoolean();
        direction = rnd.nextBoolean();
        speed = rnd.nextFloat() * 16f;
        if (speed <= 8){
            this.speed = 8;
        }

        startTime = System.currentTimeMillis();
        ETA = System.currentTimeMillis() + (long)((long)(1000/60) * (Globals.GAME_HEIGHT / speed));

        startX = playerX + rnd.nextInt(350) - 175;
        if (startX < Globals.BOUNDARY_WIDTH){
            startX = Globals.BOUNDARY_WIDTH;
        } else if (startX > Globals.GAME_WIDTH - Globals.BOUNDARY_WIDTH){
            startX = Globals.GAME_WIDTH - Globals.BOUNDARY_WIDTH;
        }

        float randomFloat = rnd.nextFloat();
        if (startX <= playerX){
            rate = randomFloat>0.1f?randomFloat * ((Globals.GAME_WIDTH - startX) / Globals.GAME_HEIGHT): 0.1f * ((Globals.GAME_WIDTH - startX) / Globals.GAME_HEIGHT);
        } else {
            rate = randomFloat>0.1f? -randomFloat*(startX/Globals.GAME_HEIGHT) : -0.1f*(startX/Globals.GAME_HEIGHT);
        }

        if (!startingSide){
            startY = Globals.GAME_HEIGHT;
            endYs = new float[(int)(Globals.GAME_HEIGHT / speed)+10];
            endXs = new float[endYs.length + 10];
            for (int i = 0; i < endYs.length; i++){
                endYs[i] = startY - speed * i;
                endXs[i] = rate * (Globals.GAME_HEIGHT - endYs[i]) + startX;
            }
            currentEndY = startY;
            currentEndX = startX;
        } else {
            startY = 0;
            endYs = new float[(int)(Globals.GAME_HEIGHT / speed)+10];
            endXs = new float[endYs.length + 10];
            for (int i = 0; i < endYs.length; i++){
                endYs[i] = speed * i;
                endXs[i] = rate * endYs[i] + startX;
            }
            currentEndY = startY;
            currentEndX = startX;
        }

        //Log.d(getClass().getCanonicalName(), "Vertical Line - Rate: " + rate + " - Current time: " + System.nanoTime() + " - ETA: " + ETA);

        state = 1;
        alphaCounter = 0;
        updateIndex = 0;
    }

    //This method allows manually setting of the attributes to create custom lines. It's used in the instructions menu.
    public void resetLineWithCustomAttributes(boolean startingSide, boolean direction,float speed, int startX, int startY, float rate){
        this.startingSide = startingSide;
        this.direction = direction;
        this.speed = speed;
        this.rate = rate;

        if (speed <= 8){
            this.speed = 8;
        }

        startTime = System.nanoTime();
        ETA = System.currentTimeMillis() + (long)((long)(1000/60) * (Globals.GAME_HEIGHT / speed));

        if (!startingSide){
            this.startX = startX;
            this.startY = Globals.GAME_HEIGHT;
            endYs = new float[(int)(Globals.GAME_HEIGHT / speed)+10];
            endXs = new float[endYs.length + 10];
            for (int i = 0; i < endYs.length; i++){
                endYs[i] = startY - speed * i;
                endXs[i] = rate * (Globals.GAME_HEIGHT - endYs[i]) + startX;
            }
            currentEndY = startY;
            currentEndX = startX;
        } else {
            this.startX = startX;
            this.startY = 0;
            endYs = new float[(int)(Globals.GAME_HEIGHT / speed)+10];
            endXs = new float[endYs.length + 10];
            for (int i = 0; i < endYs.length; i++){
                endYs[i] = speed * i;
                endXs[i] = rate * endYs[i] + startX;
            }
            currentEndY = startY;
            currentEndX = startX;
        }

        state = 1;
        alphaCounter = 0;
        updateIndex = 0;
    }
    //This method is used in multiplayer games.
    //Instead of creating random numbers for rate and startX of the line, it receives them. (For all devices to create the same line)
    //It also received the time when the line was created in the original device, in order to sync the position of the line.
    public void resetLineWithCustomAttributes(boolean startingSide, boolean direction,float speedRnd, float rateRnd, float startXRnd, int playerX, long startTime){
        this.startingSide = startingSide;
        this.direction = direction;
        this.startTime = startTime;

        speed = speedRnd * 16f;
        if (speed <= 8){
            this.speed = 8;
        }

        ETA = startTime + (long)((long)(1000/60) * (Globals.GAME_HEIGHT / speed));

        startX = playerX + startXRnd - 175;
        if (startX < Globals.BOUNDARY_WIDTH){
            startX = Globals.BOUNDARY_WIDTH;
        } else if (startX > Globals.GAME_WIDTH - Globals.BOUNDARY_WIDTH){
            startX = Globals.GAME_WIDTH - Globals.BOUNDARY_WIDTH;
        }

        if (startX <= playerX){
            this.rate = rateRnd>0.1f?rateRnd * ((Globals.GAME_WIDTH - startX) / Globals.GAME_HEIGHT): 0.1f * ((Globals.GAME_WIDTH - startX) / Globals.GAME_HEIGHT);
        } else {
            this.rate = rateRnd>0.1f? -rateRnd*(startX/Globals.GAME_HEIGHT) : -0.1f*(startX/Globals.GAME_HEIGHT);
        }

        if (!startingSide){
            this.startY = Globals.GAME_HEIGHT;
            endYs = new float[(int)(Globals.GAME_HEIGHT / speed)+10];
            endXs = new float[endYs.length + 10];
            for (int i = 0; i < endYs.length; i++){
                endYs[i] = startY - speed * i;
                endXs[i] = rate * (Globals.GAME_HEIGHT - endYs[i]) + startX;
            }
            currentEndY = startY;
            currentEndX = startX;
        } else {
            this.startY = 0;
            endYs = new float[(int)(Globals.GAME_HEIGHT / speed)+10];
            endXs = new float[endYs.length + 10];
            for (int i = 0; i < endYs.length; i++){
                endYs[i] = speed * i;
                endXs[i] = rate * endYs[i] + startX;
            }
            //((System.currentTimeMillis() - startTime)/1000000)/16.7;
            Log.d(getClass().getSimpleName(), "Start index: " + (100 - spawner.timerReduction) % spawner.timer);
            currentEndY = endYs[(100 - spawner.timerReduction) % spawner.timer];
            currentEndX = endXs[(100 - spawner.timerReduction) % spawner.timer];
            spawner.resetTimer();
            //Log.d(getClass().getSimpleName(), "Difference in time between devices = " + (System.currentTimeMillis() - startTime) + "ms");
        }

        state = 1;
        alphaCounter = 0;
        updateIndex = 0;
    }

    public void update(){
        if (state == 1){
            if (!startingSide){
                currentEndY = endYs[updateIndex];
                currentEndX = endXs[updateIndex];
                //endX = rate * (Globals.GAME_HEIGHT - endY) + startX;
                if (currentEndY <= 0){
                    //Log.d(getClass().getName(), "Vertical Line - Rate: " + rate + " - Arrival time: " + System.nanoTime());
                    state = 2;
                    //redPaint.setAlpha(100);
                }
            } else {
                currentEndY = endYs[updateIndex];
                currentEndX = endXs[updateIndex];
                //endX = rate * endY + startX;
                if (currentEndY >= Globals.GAME_HEIGHT){
                    //Log.d(getClass().getName(), "Vertical Line - Rate: " + rate + " - Arrival time: " + System.nanoTime());
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
                canvas.drawLine(startX, startY, currentEndX, currentEndY, whitePaint);
                if (direction){
                    canvas.drawLine(startX + 5, startY, currentEndX + 5, currentEndY, redPaint);
                } else {
                    canvas.drawLine(startX - 5, startY, currentEndX - 5, currentEndY, redPaint);
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

    public void explodeLine(Canvas canvas, Paint whitePaint){
        Path path = new Path();
        path.moveTo(startX, startY);
        path.lineTo(currentEndX, currentEndY);
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
