package com.MightyBarbet.LineMayhem;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;


import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Random;

//import mightybarbet.swift.unused.Background;
//import mightybarbet.swift.unused.HostileNPC;
//import mightybarbet.swift.unused.World;

/**
 * Created by Bensas on 5/5/15. Actually worked on it on 30/05/2016 (may)
 */
public class MainGameScript extends SurfaceView implements SurfaceHolder.Callback{

    private LineMayhem context;

    private MainThread thread;

    public MediaPlayer mediaPlayer;
    public int fadeoutCounter = 0;
    public boolean musicOn = true;

    private GoogleApiClient googleApiClient;
    private final static String LEADERBOARD_ID = "CgkIlaKbopsaEAIQAA";

    float scaleFactorX;
    float scaleFactorY;

    //Variables for swipe interpretation
    private float swipeStartX, swipeStartY, swipeDeltaX, swipeDeltaY, swipeDeltaTime;

    ArrayList<TextButton> mainMenuElements = new ArrayList<>();
    private Bitmap logoBitmap;
    private Paint logoPaint;
    int logoBounceTimer = 0;

    ArrayList<TextButton> gameOverMenuElements = new ArrayList<>();
    int interstitialAdCounter;

    ArrayList<TextButton> instructionsMenuElements = new ArrayList<>();
    int instructionsAnimationTimer;
    Paint instructionsMenuPaint;

    //Instantiate classes
    private Player player;
    private Boundaries boundaries;
    private Spawner spawner;
    TextButton score;

    public int currentScore = 0;

    //GameState variable: 1 = main menu, 2 = in game, 3 = end game screen, 4 = instructions page 1, 5= instructions page 2;
    public int gameState = 1, nextGameState = 0, gameStateTimer = Globals.GAME_HEIGHT + 200;

    //Create options object for loading of bitmaps
    //private BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();


    //MainScript constructor
    public MainGameScript(LineMayhem context, GoogleApiClient googleApiClient){
        super(context);

        SharedPreferences sharedPrefs = context.getSharedPreferences("GAME_PREFERENCES", Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = sharedPrefs.edit();
        prefEditor.putInt("HIGH_SCORE", 0);
        prefEditor.apply();

        this.context = context;
        this.googleApiClient = googleApiClient;

        getHolder().addCallback(this);
        setFocusable(true);

        thread = new MainThread(getHolder(), this);

        logoBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.logo), 720, 352, false);
        logoPaint = new Paint();

        mediaPlayer = MediaPlayer.create(context, R.raw.music);
        mediaPlayer.setLooping(true);
        //soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        //musicId = soundPool.load(getContext(), R.raw.music4, 1);
        //soundPool.setLoop(musicId, -1);

        //mediaPlayer.setLooping(true);
    }

    //Surface methods
    @Override
    public void surfaceCreated(SurfaceHolder holder){
        if (player == null){
            try{
                googleApiClient.connect();
            } catch (Exception e){
                e.printStackTrace();
            }
            scaleFactorX = getWidth()/(Globals.GAME_WIDTH * 1.0f);
            scaleFactorY = getHeight() / (Globals.GAME_HEIGHT * 1.0f);

            //mainMenuElements.add(new TextLogo(Globals.GAME_WIDTH/2, 250, 150, "Line", Paint.Align.CENTER, true, 0, getContext()));
            //mainMenuElements.add(new TextLogo(Globals.GAME_WIDTH/2, 400, 140, "MayheM", Paint.Align.CENTER, true, 0, getContext()));

            mainMenuElements.add(new TextButton(Globals.GAME_WIDTH/2, 600, 50, getResources().getString(R.string.button_play), Paint.Align.CENTER, true, 2, getContext()));
            mainMenuElements.add(new TextButton(Globals.GAME_WIDTH/2, 800, 50, getResources().getString(R.string.button_instructions), Paint.Align.CENTER, true, 4, getContext()));
            mainMenuElements.add(new TextButton(Globals.GAME_WIDTH/2, 1000, 50, getResources().getString(R.string.button_highscores), Paint.Align.CENTER, true, 0, getContext()));
            mainMenuElements.add(new TextButton(Globals.GAME_WIDTH/2, 1150, 30, getResources().getString(R.string.button_music), Paint.Align.CENTER, true, 0, getContext()));


            instructionsMenuElements.add(new TextButton(Globals.GAME_WIDTH/2, 100, 60, getResources().getString(R.string.button_instructions) + ":", Paint.Align.CENTER, false, 0, getContext()));
            instructionsMenuElements.add(new TextButton(Globals.GAME_WIDTH/2, 250, 32, getResources().getString(R.string.text_instructions1), Paint.Align.CENTER, false, 0, getContext()));
            instructionsMenuElements.add(new TextButton(Globals.GAME_WIDTH/2, 300, 32, getResources().getString(R.string.text_instructions12), Paint.Align.CENTER, false, 0, getContext()));
            instructionsMenuElements.add(new TextButton(Globals.GAME_WIDTH/2, 950, 32, getResources().getString(R.string.text_instructions2), Paint.Align.CENTER, false, 0, getContext()));
            instructionsMenuElements.add(new TextButton(Globals.GAME_WIDTH/2, 1000, 35, getResources().getString(R.string.text_instructions3), Paint.Align.CENTER, false, 0, getContext()));
            instructionsMenuElements.add(new TextButton(Globals.GAME_WIDTH/2, 1150, 60, getResources().getString(R.string.button_play), Paint.Align.CENTER, true, 2, getContext()));

            instructionsMenuPaint = new Paint();
            instructionsMenuPaint.setColor(Color.WHITE);
            instructionsMenuPaint.setStyle(Paint.Style.FILL);
            instructionsMenuPaint.setStrokeWidth(5);

            gameOverMenuElements.add(new TextButton(Globals.GAME_WIDTH/2, 220, 120, "GAME", Paint.Align.CENTER, false, 0, getContext()));
            gameOverMenuElements.add(new TextButton(Globals.GAME_WIDTH/2, 350, 120, "OVER", Paint.Align.CENTER, false, 0, getContext()));
            gameOverMenuElements.add(new TextButton(Globals.GAME_WIDTH/2, 500, 50, "You survived", Paint.Align.CENTER, false, 0, getContext()));
            gameOverMenuElements.add(new TextButton(Globals.GAME_WIDTH/2, 595, 65, "0", Paint.Align.CENTER, false, 6, getContext())); //*NatGeo voice* The score element is distinguished by it's unlikely statePointer
            gameOverMenuElements.add(new TextButton(Globals.GAME_WIDTH/2, 680, 50, "killer Lines!", Paint.Align.CENTER, false, 0, getContext()));
            gameOverMenuElements.add(new TextButton(Globals.GAME_WIDTH/2, 875, 50, getResources().getString(R.string.button_highscores), Paint.Align.CENTER, true, 0, getContext()));
            gameOverMenuElements.add(new TextButton(Globals.GAME_WIDTH/2, 1050, 70, "Play Again", Paint.Align.CENTER, true, 2, getContext()));

            score = new TextButton(Globals.BOUNDARY_WIDTH + 10, Globals.BOUNDARY_WIDTH + 40, 35, "Score: " + currentScore, Paint.Align.LEFT, false, 0, context);

            //Modify BitmapFactory option to prevent auto scaling of bitmaps
            //bitmapOptions.inScaled = false;

            //Instantiate player and background
            player = new Player(getContext());

            //Create boundaries
            boundaries = new Boundaries();

            spawner = new Spawner(this);
        }
        if (gameState == 2){
            if (!mediaPlayer.isPlaying()){
                mediaPlayer.start();
            }
        }

        if (thread.getState() == Thread.State.TERMINATED) {
            thread = new MainThread(getHolder(),this);
        }
        thread.setIsRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder){
        int counter = 0;
        boolean retry = true;
        //soundPool.pause(musicId);
        if (mediaPlayer.isPlaying()){
            mediaPlayer.pause();
        } else {

        }
        while(retry && counter < 1000){
            counter++;
            try{
                thread.setIsRunning(false);
                thread.join();
                retry = false;

            }catch(InterruptedException e){e.printStackTrace();}
        }
        System.out.println(thread.getState());
    }


    //Touch events handler
    @Override
    public boolean onTouchEvent(MotionEvent event){
        float touchX = event.getX() / scaleFactorX;
        float touchY = event.getY() / scaleFactorY;

        //If there is a pending gameState change, we don't want let the player interact until the transition is complete.
        if (nextGameState == 0){
            switch (gameState){
                case 1:
                    if (event.getAction() == MotionEvent.ACTION_DOWN){
                        for (TextButton button: mainMenuElements){
                            if (button.isPressable){
                                if (button.boundaries.contains((int)touchX, (int)touchY)){
                                    button.isPressed = 1;
                                }
                            }
                        }
                    }
                    else if (event.getAction() == MotionEvent.ACTION_UP){
                        for (TextButton button: mainMenuElements){
                            if (button.isPressable){
                                button.isPressed = 0;
                                if (button.boundaries.contains((int)touchX, (int)touchY)){
                                    handleTextButton(button, getContext());
                                }
                            }
                        }
                    }
                    return true;

                case 2:
                    //When the finger is touching the screen
                    if (event.getAction() == MotionEvent.ACTION_DOWN){
                        swipeStartX = touchX;
                        swipeStartY = touchY;
                        return true;
                    }
                    else if (event.getAction() == MotionEvent.ACTION_UP){
                        swipeDeltaX = touchX - swipeStartX;
                        swipeDeltaY = touchY - swipeStartY;
                        swipeDeltaTime = (event.getEventTime() - event.getDownTime()) * 0.01f;

                        player.movePlayer(swipeDeltaX/swipeDeltaTime, swipeDeltaY/swipeDeltaTime);
                        return true;
                    }
                    return true;

                case 3:
                    if (event.getAction() == MotionEvent.ACTION_DOWN){
                        for (TextButton button: gameOverMenuElements){
                            if (button.isPressable){
                                if (button.boundaries.contains((int)touchX, (int)touchY)){
                                    button.isPressed = 1;
                                }
                            }
                        }
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP){
                        for (TextButton button: gameOverMenuElements){
                            if (button.isPressable){
                                button.isPressed = 0;
                                if (button.boundaries.contains((int)touchX, (int)touchY)){
                                    handleTextButton(button, getContext());
                                }
                            }
                        }
                    }
                    return true;
                case 4:
                    if (event.getAction() == MotionEvent.ACTION_DOWN){
                        for (TextButton button: instructionsMenuElements){
                            if (button.isPressable){
                                if (button.boundaries.contains((int)touchX, (int)touchY)){
                                    button.isPressed = 1;
                                }
                            }
                        }
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP){
                        for (TextButton button: instructionsMenuElements){
                            if (button.isPressable){
                                button.isPressed = 0;
                                if (button.boundaries.contains((int)touchX, (int)touchY)){
                                    handleTextButton(button, getContext());
                                }
                            }
                        }
                    }
                    return true;

            }
        }

        return super.onTouchEvent(event);
    }

    public void handleTextButton(TextButton button, Context context){
        //The state pointer is 0 if the button does not change the game state. In this case we check the text of the button to react accordingly.
        if (button.statePointer == 0){
            if (button.text.equals("Highscores")){
                if (googleApiClient != null && !googleApiClient.isConnected()){
                    googleApiClient.connect();
                } else {
                    try{
                        ((Activity)context).startActivityForResult(Games.Leaderboards.getLeaderboardIntent(googleApiClient, LEADERBOARD_ID), 9002);
                    } catch (Exception e){
                        e.printStackTrace();
                        Toast toast = new Toast(context);
                        toast.makeText(context, "Google Play Services is not connected :(", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            }
            else if (button.text.contains("Music")){
                if (musicOn){
                    musicOn = false;
                    button.setText("Music: OFF");
                } else {
                    musicOn = true;
                    button.setText("Music: ON");
                }
            }
        } else {
            nextGameState = button.statePointer;
        }
    }

    //I tried putting the actions to be executed inside the case statement in the onTouchEvent method inside their own methods for tidiness' sake,
    //but for whatever reason they were not being called correctly. I encourage you to try and fix them.
//    public boolean onTouchMainMenu(MotionEvent event){
//        if (event.getAction() == MotionEvent.ACTION_UP){
//            //if ()
//        }
//        return true;
//    }
//    public boolean onTouchInGame(MotionEvent event){
//        Log.d("onTouchInGame", "onTouchInGame called!" );
//        //When the finger is touching the screen
//        if (event.getAction() == MotionEvent.ACTION_DOWN){
//            swipeStartX = event.getX();
//            swipeStartY = event.getY();
//            return super.onTouchEvent(event);
//        }
//        else if (event.getAction() == MotionEvent.ACTION_UP){
//            Log.d("onTouchInGame", "actionUp!" );
//            swipeDeltaX = event.getX() - swipeStartX;
//            swipeDeltaY = event.getY() - swipeStartY;
//            swipeDeltaTime = (event.getEventTime() - event.getDownTime()) * 0.01f;
//            //Log.d("Swipe duration:", String.valueOf(swipeDeltaTime) + " - " + String.valueOf(swipeStartTime)
//            //        + " - " + String.valueOf(swipeDeltaTime - swipeStartTime));
//
//            player.movePlayer(swipeDeltaX/swipeDeltaTime, swipeDeltaY/swipeDeltaTime);
//
//            return super.onTouchEvent(event);
//        } else {
//            Log.d("onTouchInGame", "Touch returned false. This is weird and I don't think is possible. But hey, if you're reading this, then who's to say what's possible or not? :^)");
//            return super.onTouchEvent(event);
//        }
//
//    }

    //Makes the placer bounce off walls.
    public void checkBoundariesCollisions(){

        //Undecent, unpretty, hardcoded code
        if ((player.x - player.width/2) < Globals.BOUNDARY_WIDTH){
            player.x = Globals.BOUNDARY_WIDTH + player.width/2;
            player.setSpeedX(-player.speedX * 0.2);
        } else if ( player.x + player.width/2 > (Globals.GAME_WIDTH - Globals.BOUNDARY_WIDTH)){
            player.x = (Globals.GAME_WIDTH - Globals.BOUNDARY_WIDTH) - player.width/2;
            player.setSpeedX(-player.speedX * 0.2);
        } else if ((player.y - player.height/2) < Globals.BOUNDARY_WIDTH){
            player.y = Globals.BOUNDARY_WIDTH + player.height/2;
            player.setSpeedY(-player.speedY * 0.2);
        } else if (player.y + player.height/2 > (Globals.GAME_HEIGHT - Globals.BOUNDARY_WIDTH)){
            player.y = (Globals.GAME_HEIGHT - Globals.BOUNDARY_WIDTH) - player.height/2;
            player.setSpeedY(-player.speedY * 0.2);
        }
    }

    //Update function
    public void update(){

        switch (gameState){
            case 1:
                break;
            case 2:
                player.update();
                checkBoundariesCollisions();
                spawner.update(player.x, player.y, this);
                break;
            case 3:
                spawner.update(player.x, player.y, this);
                break;
            case 4:
                spawner.update(player.x, player.y, this);
                player.update();

                //This is....animation...looks good, innit?
                if (instructionsAnimationTimer < 170){
                    player.x = Globals.GAME_WIDTH/2;
                    player.y = Globals.GAME_HEIGHT/2;
                }
                else if  (instructionsAnimationTimer < 200){
                    player.movePlayer(2.5f, -0.7f);
                }
                else if (instructionsAnimationTimer < 280){

                }
                else if (instructionsAnimationTimer < 320){
                    player.movePlayer(-1f, 2f);
                }
                else if (instructionsAnimationTimer < 420){

                }
                else if (instructionsAnimationTimer < 460){
                    player.movePlayer(-1.5f, -0.5f);
                }
                else if (instructionsAnimationTimer < 510){

                }
                else if (instructionsAnimationTimer < 540){
                    player.movePlayer(-0.5f, -2.7f);
                }
                else if (instructionsAnimationTimer < 600){

                }
                else if (instructionsAnimationTimer < 630){
                    player.movePlayer(3.46f, 3.5f);
                }
                else if (instructionsAnimationTimer < 660){

                }
                else {
                    player.x = Globals.GAME_WIDTH /2;
                    player.y = Globals.GAME_HEIGHT / 2;
                    instructionsAnimationTimer = 0;
                }

                instructionsAnimationTimer++;

        }


        handleGameStateChanges();
    }

    //Custom behaviour for different state changes is coded here. Uses the
    public void handleGameStateChanges(){

        switch (nextGameState){

            //This happens if there is no pending gameState change.
            case 0:
                break;

            //This happens when the play button is pressed
            case 2:
                //if (soundPool.)
                //soundPool.play(musicId, 1, 1, 0, -1, 1);

//                try{
//                    mediaPlayer.prepare();
//                } catch (IOException e){
//                    e.printStackTrace();
//                }
                if (musicOn){
                    mediaPlayer.seekTo(0);
                    mediaPlayer.setVolume(1, 1);
                    mediaPlayer.start();
                }

                resetGame();
                gameState = 2;
                nextGameState = 0;
                break;

            //This happens when the player dies
            case 3:
                fadeoutCounter++;

                //soundPool.setVolume(musicId, 1 - 0.1f*fadeoutCounter, 1 - 0.1f*fadeoutCounter);
                mediaPlayer.setVolume(1 - 0.1f*fadeoutCounter, 1 - 0.1f*fadeoutCounter);
                //soundPool.stop(musicId);

                player.radius += fadeoutCounter * 15;
                player.paint.setAlpha(255-fadeoutCounter*50);

                for (TextButton element: gameOverMenuElements){
                    element.y = element.defaultY + gameStateTimer;
                }
                gameStateTimer *= 0.7;
                if (gameStateTimer <= 1){
                    gameState = 3;
                    nextGameState = 0;

                    //soundPool.stop(musicId);
                    mediaPlayer.setVolume(0, 0);
                    mediaPlayer.pause();

                    player.paint.setAlpha(255);
                    player.radius = 10;
                    fadeoutCounter = 0;

                    interstitialAdCounter++;
                    Random rnd = new Random();
                    if (interstitialAdCounter >= 2 && rnd.nextBoolean()){
                        context.runOnUiThread(new Runnable() {
                            @Override public void run() {
                                context.showInterstitialAd();
                                Log.d(getClass().getSimpleName(), "About to show interstitial ad.");
                            }
                        });
                        interstitialAdCounter = 0;
                        Log.d(getClass().getSimpleName(), "InterstitialAdCounter reset");
                    }


                    gameStateTimer = Globals.GAME_HEIGHT + 200;

                    SharedPreferences sharedPrefs = context.getSharedPreferences("GAME_PREFERENCES", Context.MODE_PRIVATE);
                    if (sharedPrefs.getInt("HIGH_SCORE", 0) < currentScore){
                        SharedPreferences.Editor prefEditor = sharedPrefs.edit();
                        prefEditor.putInt("HIGH_SCORE", currentScore);
                        prefEditor.apply();
                    }

                    try{
                        Games.Leaderboards.submitScore(googleApiClient, LEADERBOARD_ID, sharedPrefs.getInt("HIGH_SCORE", 999999));

                        Bundle scoreBundle = new Bundle();
                        scoreBundle.putInt(FirebaseAnalytics.Param.SCORE, currentScore);
                        ((LineMayhem)context).firebaseClient.logEvent(FirebaseAnalytics.Event.POST_SCORE, scoreBundle);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
                break;
            case 4:
                resetGame();
                player.y = 700;
                gameState = 4;
                nextGameState = 0;
                break;
        }
    }

    //Guess what this does
    public void resetGame(){
        spawner.reset();
        player.reset();
        currentScore = 0;
    }

    //Draw function
    @Override
    public void draw(Canvas canvas){
        //Log.d(getClass().getSimpleName(), String.valueOf(canvas.isHardwareAccelerated()));

        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        final int savedCanvasState = canvas.save();
        canvas.scale(scaleFactorX, scaleFactorY);

        //We decide what to draw according to the current game state
        switch (gameState){
            case 1:
                logoBounceTimer += 1;
                if (logoBounceTimer < 30){
                    canvas.drawBitmap(logoBitmap,-2, 60, logoPaint);
                } else if (logoBounceTimer < 40){
                    canvas.drawBitmap(logoBitmap,-2, 67, logoPaint);
                } else if (logoBounceTimer < 70){
                    canvas.drawBitmap(logoBitmap,-2, 74, logoPaint);
                } else if (logoBounceTimer < 90){
                    canvas.drawBitmap(logoBitmap,-2, 67, logoPaint);
                } else{
                    canvas.drawBitmap(logoBitmap,-2, 60, logoPaint);
                    logoBounceTimer = 0;
                }

                for (TextButton button: mainMenuElements){
                    if (button.getClass() == TextLogo.class){
                        if (logoBounceTimer < 30){
                            button.isPressed = 0;
                        } else if (logoBounceTimer < 40){
                            button.isPressed = 1;
                        } else if (logoBounceTimer < 70){
                            button.isPressed = 2;
                        } else if (logoBounceTimer < 90){
                            button.isPressed = 1;
                        } else{
                            logoBounceTimer = 0;
                        }
                    }
                    button.draw(canvas);
                }
                break;
            case 2:
                player.draw(canvas);
                spawner.draw(canvas);
                score.setText("Score: " + currentScore);
                score.draw(canvas);
                if (nextGameState == 3){
                    for (TextButton button: gameOverMenuElements){
                        if (button.statePointer == 6){
                            button.text = String.valueOf(currentScore);
                        }
                        button.draw(canvas);
                    }
                }
                break;
            case 3:
                spawner.draw(canvas);
                for (TextButton button: gameOverMenuElements){
                    if (button.statePointer == 6){
                        button.text = String.valueOf(currentScore);
                    }
                    button.draw(canvas);
                }
                break;
            case 4:

                for (TextButton button: instructionsMenuElements){
                    button.draw(canvas);
                }

                player.draw(canvas);
                spawner.draw(canvas);

        }

        //The game frame (game boundaries) are always drawn
        boundaries.draw(canvas);

        canvas.restoreToCount(savedCanvasState);

        invalidate();

    }
}
