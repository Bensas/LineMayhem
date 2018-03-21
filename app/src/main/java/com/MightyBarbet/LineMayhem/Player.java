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
    public int radius = 10, fadeoutCounter;
    public Skin skin;
    //These booleans are for multiplayer games.
    public boolean isAlive = true, hasExploded = false, isReady = false;
    String id, ign;
    int connectionAliveTimer = 0;
    TextButton nameTag;
    Paint paint = new Paint();
    int score;

    //Variables used for movement
    private double speedFactor = 0.33;

    //Constructor
    public Player(Context context, String colorStr, String id, String ign){
        this.id = id;
        this.ign = ign;
        nameTag = new TextButton(x, y-32, 30, ign, Paint.Align.CENTER, false, 0, context);
        nameTag.setColor(Color.parseColor(colorStr));
        setSkin(colorStr);
        paint.setStyle(Paint.Style.FILL);
        skin = null;

        x = Globals.GAME_WIDTH / 2;
        y = Globals.GAME_HEIGHT / 2;
//
//        width = 2 * radius;
//        height = 2 * radius;
    }

    public void setSkin(String skinString){
        skin = null;
        paint.setColor(Color.parseColor(skinString));
    }
    public void setSkin(Skin skin){this.skin = skin;}

    //MovePlayer method takes swypeDelta values from the main script and modifies the speed based on it.
    public void movePlayer(float deltaX, float deltaY){
        speedX += deltaX * speedFactor;
        speedY += deltaY * speedFactor;
    }

    //Update method
    public void update(){

        speedX *= 0.9;
        speedY *= 0.9;

        //If speed/acceleration are smaller than 0.1, round them down to 0
        if (Math.abs(speedX) < 0.1){
            speedX = 0;
        }
        if (Math.abs(speedY) < 0.1){
            speedY = 0;
        }

        x += speedX;
        y += speedY;

        radius = (int)(10 + (Math.abs(speedX) + Math.abs(speedY))/40);
        //Log.d("Player.update()", "Current radius: " + radius);
        nameTag.x = x;
        nameTag.y = y - 32;
        //System.out.println(x + " - " + y);
    }

    //Draw method
    public void draw(Canvas canvas){
        if (skin != null){
            canvas.save();
            canvas.scale(radius/10f, radius/10f, x, y);
            canvas.drawBitmap(skin.bitmap, x-skin.bitmap.getWidth()/2, y-skin.bitmap.getHeight()/2, paint);
            canvas.restore();
        } else {
            canvas.drawCircle(x, y, radius, paint);
        }
    }

    public void reset(){
        x = Globals.GAME_WIDTH / 2;
        y = Globals.GAME_HEIGHT / 2;
        speedX = 0;
        speedY = 0;
    }
}
