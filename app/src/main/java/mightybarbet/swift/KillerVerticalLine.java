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
    Random rnd = new Random();
    SoundPool soundPool;

    //startX and startY are assigned at the start and remain static.
    //endX and endY are modified on every update to make the line extend.
    float startX, startY, endX, endY;
    float speed, rate;

    //startingSide: 0 is bottom, 1 is top --- direction: 0 is leftwards, 1 is rightwards
    boolean startingSide, direction;

    //0 means extending, 1 means exploding, 2 means hasExploded
    int state;

    public Paint whitePaint = new Paint();
    public Paint redPaint = new Paint();
    public int alphaCounter = 0;

    public KillerVerticalLine (boolean startingSide, boolean direction, float speed, int playerX, SoundPool soundPool, int soundId){
        this.startingSide = startingSide;
        this.direction = direction;
        if (speed <= 3){
            this.speed = 3;
        } else {
            this.speed = speed;
        }
        state = 0;

        startX = playerX + rnd.nextInt(350) - 175;


        if (!startingSide){
            startY = Globals.GAME_HEIGHT;
            endY = Globals.GAME_HEIGHT;
            endX = startX;
        } else {
            endX = startX;
        }

        if (startX <= playerX){
            rate = rnd.nextFloat() * ((Globals.GAME_WIDTH - startX) / Globals.GAME_HEIGHT);
        } else {
            rate = -rnd.nextFloat() * (startX/Globals.GAME_HEIGHT);
        }

//        this.soundPool = soundPool;
//        //float playbackSpeed = speed/9 + 1/6;
//        float playbackSpeed = 1.5f;
//        this.soundPool.play(soundId, 0.6f, 0.6f, 0, 0, playbackSpeed);

        whitePaint.setColor(Color.WHITE);
        whitePaint.setStrokeWidth(6);
        whitePaint.setStyle(Paint.Style.FILL);
        redPaint.setColor(Color.RED);
        redPaint.setStrokeWidth(5);
        redPaint.setStyle(Paint.Style.FILL);
        //Log.d("VerticalLine", "Start X: " + startX + " StartY:" + startY);
    }

    public void update(){
        if (state == 0){
            if (!startingSide){
                endY -= speed;
                endX = rate * (Globals.GAME_HEIGHT - endY) + startX;
                if (endY <= 0){
                    state = 1;
                    //redPaint.setAlpha(100);
                }
            } else {
                endY += speed;
                endX = rate * endY + startX;
                if (endY >= Globals.GAME_HEIGHT){
                    state = 1;
                    //redPaint.setAlpha(100);
                }
            }
        }

    }

    public void draw(Canvas canvas){

        switch (state){
            case 0:
                canvas.drawLine(startX, startY, endX, endY, whitePaint);
                if (direction){
                    canvas.drawLine(startX + 5, startY, endX + 5, endY, redPaint);
                } else {
                    canvas.drawLine(startX - 5, startY, endX - 5, endY, redPaint);
                } break;
            case 1:
                explodeLine(canvas, redPaint);
                state = 2;
                break;
            case 2:
                alphaCounter += 1;
                explodeLine(canvas, redPaint);
                if (alphaCounter >= 10){
                    state = 4;
                }
                break;
            case 4:
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
