package mightybarbet.swift;

import android.graphics.Rect;

/**
 * Created by Bensas on 5/5/15.
 */
public abstract class GameObject {

    protected int x;
    protected int y;
    protected double speedX, accelX;
    protected double speedY, accelY;
    protected int width;
    protected int height;

    //Getters
    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }

    public Rect getCollisionBox(){
        return new Rect(x, y, x+width, y+height);
    }


    //Setters
    public void setX(int x){
        this.x = x;
    }

    public void setY(int y){
        this.y = y;
    }

    public void setSpeedX(double speedX){this.speedX = speedX;}

    public void setSpeedY(double speedY){this.speedY = speedY;}

    public void setaccelX(double accelX){this.accelX = accelX;}

    public void setAccelY(double accelY){this.accelY = accelY;}


}
