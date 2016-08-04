package mightybarbet.swift;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.provider.MediaStore;
import android.provider.Settings;
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

    //startingSide: 0 is left, 1 is right --- direction: 0 is downwards, 1 is upwards
    boolean startingSide, direction;

    //0 means extending, 1 means exploding, 2 means hasExploded
    int state;

    public Paint whitePaint = new Paint();
    public Paint redPaint = new Paint();
    public int alphaCounter = 0;

    public KillerHorizontalLine (boolean startingSide, boolean direction, float speed, int playerY, SoundPool soundPool, int soundId){
        this.startingSide = startingSide;
        this.direction = direction;
        if (speed <= 3){
            this.speed = 3;
        } else {
            this.speed = speed;
        }
        state = 0;

        startY = playerY + rnd.nextInt(400) - 200;
        if (startY > Globals.GAME_HEIGHT){
            startY = Globals.GAME_HEIGHT;
        }

        if (!startingSide){
            startX = 0;
        } else {
            startX = Globals.GAME_WIDTH;
            endX = Globals.GAME_WIDTH;
            endY = startY;
        }

        if (startY <= playerY){
            rate = rnd.nextFloat() * ((Globals.GAME_HEIGHT - startY) / Globals.GAME_WIDTH);
        } else {
            rate = -rnd.nextFloat() * (startY/Globals.GAME_WIDTH);
        }

        //this.soundPool = soundPool;
        //float playbackSpeed = speed/9 + 2/6;
//        float playbackSpeed = 1.5f;
//        this.soundPool.play(soundId, 0.6f, 0.6f, 0, 0, playbackSpeed);
        //this.mediaPlayer = mediaPlayer;
        //this.mediaPlayer.start();

        whitePaint.setColor(Color.WHITE);
        whitePaint.setStrokeWidth(6);
        whitePaint.setStyle(Paint.Style.FILL);
        redPaint.setColor(Color.RED);
        redPaint.setStrokeWidth(5);
        redPaint.setStyle(Paint.Style.FILL);
    }

    public void update(){
        if (state == 0){
            if (!startingSide){
                endX += speed;
                endY = rate * endX + startY;
                if (state < 1 && endX >= Globals.GAME_WIDTH){
                    state = 1;
                    //redPaint.setAlpha(100); TODO: FIGURE OUT HOW TO CHANGE ALPHA WITHOUT TAKING A SHIT ON FPS
                }
            } else {
                endX -= speed;
                endY = rate * (Globals.GAME_WIDTH - endX) + startY;
                if (state < 1 && endX <= 0){
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
                    canvas.drawLine(startX, startY - 5, endX, endY - 5, redPaint);
                } else {
                    canvas.drawLine(startX, startY + 5, endX, endY + 5, redPaint);
                } break;
            case 1:
                //soundPool.release();
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
