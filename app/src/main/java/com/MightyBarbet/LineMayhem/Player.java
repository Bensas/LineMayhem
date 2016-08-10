package com.MightyBarbet.LineMayhem;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

/**
 * Created by Bensas on 5/5/15. Actually used on 01/06/2016 (june)
 */
public class Player extends GameObject{
    //Attributes
    Paint paint = new Paint();
    public int radius = 10;
    public Bitmap skinArrow;

    //Variables used for movement
    private double speedFactor = 0.3;
    private float slowDownFactorX, slowDownFactorY;

    //Constructor
    public Player(Context context){
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        skinArrow = BitmapFactory.decodeResource(context.getResources(), R.drawable.skin_arrow);

        x = Globals.GAME_WIDTH / 2;
        y = Globals.GAME_HEIGHT / 2;

        width = 2 * radius;
        height = 2 * radius;
    }

    //MovePlayer method takes swypeDelta values from the main script and modifies the acceleration based on it.
    public void movePlayer(float deltaX, float deltaY){
        speedX += deltaX * speedFactor;
        speedY += deltaY * speedFactor;
    }

    //Update method
    public void update(){

        //The below code would ideally be replaced by
        speedX *= 0.9;
        speedY *= 0.9;
        //Unfortunately, processors suck at math and FPS tank hard with those lines of code.
        Log.d(getClass().getSimpleName(), speedX + " - " + speedY);
//        if (speedX > 0){
//            if (speedX < 0.1){
//                speedX = 0;
//            }
//            else if (speedX < 1){
//                speedX -= 0.08 ;
//            }
//            else if (speedX < 5){
//                speedX -= 0.1;
//            }
//            else if (speedX < 10){
//                speedX -= 1;
//            }
//            else {
//                speedY -= 2;
//            }
//        }
//        else if (speedX < 0){
//            if (speedX > -0.1){
//                speedX = 0;
//            }
//            else if (speedX > -1){
//                speedX += 0.08 ;
//            }
//            else if (speedX > -5){
//                speedX += 0.1;
//            }
//            else if (speedX > -10){
//                speedX += 1;
//            }
//            else {
//                speedY += 2;
//            }
//        }
//        if (speedY > 0){
//            if (speedY < 0.1){
//                speedY = 0;
//            }
//            else if (speedY < 1){
//                speedY -= 0.08 ;
//            }
//            else if (speedY < 5){
//                speedY -= 0.5;
//            }
//            else if (speedY < 10){
//                speedY -= 1;
//            }
//            else {
//                speedY -= 2;
//            }
//        }
//        else if (speedY < 0){
//            if (speedY > -0.1){
//                speedY = 0;
//            }
//            else if (speedY > -1){
//                speedY += 0.08 ;
//            }
//            else if (speedY > -5){
//                speedY += 0.5;
//            }
//            else if (speedY > -10){
//                speedY += 1;
//            }
//            else {
//                speedY += 2;
//            }
//        }


//        if (speedY > 0.1){
//            speedY -= 0.09 ;
//        } else if (speedY < -0.1){
//            speedY += 0.09 ;
//        }

        //If speed/acceleration are smaller than 0.1, round them down to 0
        if (Math.abs(speedX) < 0.1){
            speedX = 0;
            accelX = 0.02;
        }
        if (Math.abs(speedY) < 0.1){
            speedY = 0;
            accelX = 0.02;
        }


        x += speedX;
        y += speedY;

        radius = (int)(10 + (Math.abs(speedX) + Math.abs(speedY))/5);

        //System.out.println(x + " " + y);

    }

    //Draw method
    public void draw(Canvas canvas){
        //canvas.drawBitmap(skinArrow, x-skinArrow.getWidth()/2, y-skinArrow.getHeight()/2, paint);
        canvas.drawCircle(x, y, 10, paint);
    }

    public void reset(){
        x = Globals.GAME_WIDTH / 2;
        y = Globals.GAME_HEIGHT / 2;
        speedX = 0;
        speedY = 0;
    }
}
