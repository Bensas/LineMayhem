package mightybarbet.swift;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

import mightybarbet.swift.MainGameScript;

/**
 *  Generic MainThread class created off a YouTube tutorial, slightly modified. 05/04/2015
 */
public class MainThread extends Thread {
    private boolean isRunning;
    private int FPS = 60;
    private float averageFPS;
    private SurfaceHolder surfaceHolder;
    private MainGameScript mainGameScript;
    public Canvas canvas;

    //CONSTRUCTOR METHOD
    public MainThread(SurfaceHolder surfaceHolder, MainGameScript mainGameScript)
    {
        super();
        this.surfaceHolder = surfaceHolder;
        this.mainGameScript = mainGameScript;
    }

    //RUN METHOD
    @Override
    public void run() {
        long startTime;
        long timeMilliseconds;
        long waitTime;
        long totalTime = 0;
        long frameCount = 0;
        long targetTime = 1000 / FPS;

        while (isRunning){
            startTime = System.nanoTime();
            canvas = null;

            try{
                canvas = this.surfaceHolder.lockCanvas();
                synchronized (surfaceHolder){
                    this.mainGameScript.draw(canvas);
                    this.mainGameScript.update(); //THE ORIGINAL ORDER WAS UPDATE() FIRST AND DRAW() SECOND
                }
            } catch (Exception e){
            }finally{
                if (canvas != null){
                    try{
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }catch(Exception e){e.printStackTrace();}
                }
            }

            timeMilliseconds = (System.nanoTime()-startTime)/1000000;
            waitTime = targetTime - timeMilliseconds;

            try{
                this.sleep(waitTime);
            }catch(Exception e){}

            totalTime += System.nanoTime() - startTime;
            frameCount++;

            if(frameCount == FPS){
                averageFPS = 1000/((totalTime/frameCount)/1000000);
                Log.d("MainThread:", "FPS= " + averageFPS);
                frameCount = 0;
                totalTime = 0;
            }
        }

    }



    //Set running boolean method
    public void setIsRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }
}