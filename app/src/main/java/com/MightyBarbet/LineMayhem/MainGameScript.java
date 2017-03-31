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
import android.media.PlaybackParams;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;


import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.data.Freezable;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.games.Game;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Bensas on 5/5/15. Actually worked on it on 30/05/2016 (may)
 */
public class MainGameScript extends SurfaceView implements SurfaceHolder.Callback{

    private final static String LEADERBOARD_ID = "CgkIlaKbopsaEAIQAA";
    public LineMayhem context;
    public MediaPlayer mediaPlayer;
    public int fadeoutCounter = 0;
    public boolean musicOn = true;

    public GoogleApiClient googleApiClient;
    public Player player;
    public Player[] otherPlayers;
    public Spawner spawner;
    public int currentScore = 0;
    public boolean multiplayer = false;
    //If this device is the host, then it must send the line information to the rest of the players,
    //not just it's current position
    public boolean isHost = false;
    //GameState variable: 1 = main menu, 2 = in game, 3 = end game screen, 4 = instructions page 1, 5 = multiplayer menu;
    public int gameState = 1, nextGameState = 0, gameStateTimer = Globals.GAME_HEIGHT + 200;
    // Message buffer for sending position message and new line message
    byte[] mMsgBuf;
    byte[] mMovMsgBuf;
    //Every time the player's speed is 0, its position is sent; When it dies, it's final position and score are sent
    boolean hasBroadcastedPosition = false, hasBroadcastedFinalPosition = false;
    float scaleFactorX;
    float scaleFactorY;
    TextButton[] mainMenuElements = new TextButton[6];
    int logoBounceTimer = 0;
    ArrayList<TextButton> multiplayerMenuElements = new ArrayList<>();
    TextButton[] multiplayerGameOverMenuElements = new TextButton[12];
    TextButton[] gameOverMenuElements = new TextButton[8];
    int interstitialAdCounter;
    ArrayList<TextButton> instructionsMenuElements = new ArrayList<>();
    int instructionsAnimationTimer;
    Paint instructionsMenuPaint;
    TextButton score;
    LoadingIndicator loadingIndicator;
    NotificationsButton notificationsButton;
    ArrayList<Notification> notifications;
    int nameTagDisplayTimer = 300;
    boolean gameOverScreenReady = false;
    private MainThread thread;
    //Variables for swipe interpretation
    private float swipeStartX, swipeStartY, swipeDeltaX, swipeDeltaY, swipeDeltaTime;
    private Bitmap logoBitmap;
    private Paint logoPaint;
    private Boundaries boundaries;

    //MainScript constructor
    public MainGameScript(LineMayhem context, GoogleApiClient googleApiClient){
        super(context);

        this.context = context;
        this.googleApiClient = googleApiClient;

        getHolder().addCallback(this);
        setFocusable(true);

        thread = new MainThread(getHolder(), this);

        logoBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.logo), 720, 352, false);
        logoPaint = new Paint();

        mediaPlayer = MediaPlayer.create(context, R.raw.music);
        mediaPlayer.setLooping(true);
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

            Games.Leaderboards.loadCurrentPlayerLeaderboardScore(googleApiClient, LEADERBOARD_ID, LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC).setResultCallback(new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
                @Override
                public void onResult(Leaderboards.LoadPlayerScoreResult arg0) {
                    if (arg0.getScore() != null){
                        SharedPreferences sharedPrefs = context.getSharedPreferences("GAME_PREFERENCES", Context.MODE_PRIVATE);
                        SharedPreferences.Editor prefEditor = sharedPrefs.edit();
                        if (sharedPrefs.getInt("HIGH_SCORE", 0) < (int)arg0.getScore().getRawScore()){
                            mainMenuElements[0].setText("Highscore: " + arg0.getScore().getRawScore());
                            prefEditor.putInt("HIGH_SCORE", (int)arg0.getScore().getRawScore());
                            prefEditor.apply();
                            prefEditor.commit();
                        } else {
                            Games.Leaderboards.submitScore(googleApiClient, LEADERBOARD_ID, sharedPrefs.getInt("HIGH_SCORE", 0));
                        }
                    }
                }
            });
            Games.Leaderboards.loadCurrentPlayerLeaderboardScore(googleApiClient, LEADERBOARD_ID, LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC);

            scaleFactorX = getWidth()/(Globals.GAME_WIDTH * 1.0f);
            scaleFactorY = getHeight() / (Globals.GAME_HEIGHT * 1.0f);

            SharedPreferences sharedPrefs = context.getSharedPreferences("GAME_PREFERENCES", Context.MODE_PRIVATE);

            int highscore = sharedPrefs.getInt("HIGH_SCORE", 0);
            Log.d("surfaceCreated", "storedHighscore: " + highscore);
            mainMenuElements[0] = new TextButton(Globals.GAME_WIDTH/2 , 430, 35, "Highscore: " + highscore , Paint.Align.CENTER, false, 0, getContext());
            mainMenuElements[1] = new TextButton(Globals.GAME_WIDTH/2, 600, 50, getResources().getString(R.string.button_play), Paint.Align.CENTER, true, 2, getContext());
            mainMenuElements[2] = new TextButton(Globals.GAME_WIDTH/2, 725, 50, getResources().getString(R.string.button_multiplayer), Paint.Align.CENTER, true, 5, getContext());
            mainMenuElements[3] = new TextButton(Globals.GAME_WIDTH/2, 850, 50, getResources().getString(R.string.button_instructions), Paint.Align.CENTER, true, 4, getContext());
            mainMenuElements[4] = new TextButton(Globals.GAME_WIDTH/2, 975, 50, getResources().getString(R.string.button_highscores), Paint.Align.CENTER, true, 0, getContext());
            musicOn = sharedPrefs.getBoolean("MUSIC_ON", true);
            mainMenuElements[5] = new TextButton(Globals.GAME_WIDTH/2, 1125, 30, musicOn? "Music: ON" : "Music: OFF", Paint.Align.CENTER, true, 0, getContext());

            multiplayerMenuElements.add(new TextButton(Globals.GAME_WIDTH/2, 600, 50, getResources().getString(R.string.button_quick_game), Paint.Align.CENTER, true, 0, getContext()));
            multiplayerMenuElements.add(new TextButton(Globals.GAME_WIDTH/2, 850, 50, getResources().getString(R.string.button_invite_players), Paint.Align.CENTER, true, 0, getContext()));
            multiplayerMenuElements.add(new TextButton(Globals.GAME_WIDTH/2, 1100, 50, getResources().getString(R.string.button_my_invites), Paint.Align.CENTER, true, 0, getContext()));


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

            gameOverMenuElements[0] = new TextButton(Globals.GAME_WIDTH/2, 220, 120, "GAME", Paint.Align.CENTER, false, 0, getContext());
            gameOverMenuElements[1] = new TextButton(Globals.GAME_WIDTH/2, 350, 120, "OVER", Paint.Align.CENTER, false, 0, getContext());
            gameOverMenuElements[2] = new TextButton(Globals.GAME_WIDTH/2, 500, 50, "You survived", Paint.Align.CENTER, false, 0, getContext());
            gameOverMenuElements[3] = new TextButton(Globals.GAME_WIDTH/2, 595, 65, "0", Paint.Align.CENTER, false, 6, getContext()); //*NatGeo voice* The score element is distinguished by it's unlikely statePointer
            gameOverMenuElements[4] = new TextButton(Globals.GAME_WIDTH/2, 680, 50, "killer Lines!", Paint.Align.CENTER, false, 0, getContext());
            gameOverMenuElements[5] = new TextButton(Globals.GAME_WIDTH/2, 865, 50, getResources().getString(R.string.button_highscores), Paint.Align.CENTER, true, 0, getContext());
            gameOverMenuElements[6] = new TextButton(Globals.GAME_WIDTH/2, 1000, 70, "Play Again", Paint.Align.CENTER, true, 2, getContext());
            gameOverMenuElements[7] = new TextButton(Globals.GAME_WIDTH/2, 1150, 70, "Main Menu", Paint.Align.CENTER, true, 1, getContext());

            multiplayerGameOverMenuElements[0] = new TextButton(Globals.GAME_WIDTH/2, 200, 120, "GAME", Paint.Align.CENTER, false, 0, getContext());
            multiplayerGameOverMenuElements[1] = new TextButton(Globals.GAME_WIDTH/2, 330, 120, "OVER", Paint.Align.CENTER, false, 0, getContext());
            multiplayerGameOverMenuElements[2] = new TextButton(Globals.GAME_WIDTH/2, 430, 40, "You survived", Paint.Align.CENTER, false, 0, getContext());
            multiplayerGameOverMenuElements[3] = new TextButton(Globals.GAME_WIDTH/2, 520, 55, "0", Paint.Align.CENTER, false, 6, getContext()); //*NatGeo voice* The score element is distinguished by it's unlikely statePointer
            multiplayerGameOverMenuElements[4] = new TextButton(Globals.GAME_WIDTH/2, 590, 40, "killer Lines!", Paint.Align.CENTER, false, 0, getContext());
            multiplayerGameOverMenuElements[5] = new TextButton(Globals.GAME_WIDTH/2, 865, 50, getResources().getString(R.string.button_highscores), Paint.Align.CENTER, true, 0, getContext());
            multiplayerGameOverMenuElements[6] = new TextButton(Globals.GAME_WIDTH/2, 1000, 60, getResources().getString(R.string.button_ready), Paint.Align.CENTER, true, 0, getContext());
            multiplayerGameOverMenuElements[7] = new TextButton(Globals.GAME_WIDTH/2, 1150, 70, "Main Menu", Paint.Align.CENTER, true, 1, getContext());
            multiplayerGameOverMenuElements[8] = new TextButton(Globals.GAME_WIDTH/2, 640, 38, "", Paint.Align.CENTER, false, 0, getContext());
            multiplayerGameOverMenuElements[9] = new TextButton(Globals.GAME_WIDTH/2, 640 + 60, 38, "", Paint.Align.CENTER, false, 0, getContext());
            multiplayerGameOverMenuElements[10] = new TextButton(Globals.GAME_WIDTH/2, 640 + 120, 38, "", Paint.Align.CENTER, false, 0, getContext());
            multiplayerGameOverMenuElements[11] = new TextButton(Globals.GAME_WIDTH/2, 1250, 30, "Waiting for other players...", Paint.Align.CENTER, false, 0, getContext());


            notificationsButton = new NotificationsButton(context);
            score = new TextButton(Globals.BOUNDARY_WIDTH + 10, Globals.BOUNDARY_WIDTH + 40, 35, "Score: " + currentScore, Paint.Align.LEFT, false, 0, context);

            loadingIndicator = new LoadingIndicator(getContext());

            //Instantiate player and background
            player = new Player(getContext(), "white", "", "");
            otherPlayers = new Player[3];

            //Create boundaries
            boundaries = new Boundaries();

            spawner = new Spawner();
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
        if (mediaPlayer.isPlaying()){
            mediaPlayer.pause();
        } else {

        }

        int counter = 0;
        boolean retry = true;
        while(retry && counter < 1000){
            counter++;
            try{
                thread.setIsRunning(false);
                thread.join();
                retry = false;

            }catch(InterruptedException e){e.printStackTrace();}
        }
        Log.d(getClass().getSimpleName(), "Thread " + thread.getState());
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
                        if (multiplayer)
                            broadcastPlayerMovement(swipeDeltaX/swipeDeltaTime, swipeDeltaY/swipeDeltaTime);

                        return true;
                    }
                    return true;

                case 3:
                    if (!multiplayer){
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
                    } else {
                        if (event.getAction() == MotionEvent.ACTION_DOWN){
                            for (TextButton button: multiplayerGameOverMenuElements){
                                if (button.isPressable){
                                    if (button.boundaries.contains((int)touchX, (int)touchY)){
                                        button.isPressed = 1;
                                    }
                                }
                            }
                        }
                        if (event.getAction() == MotionEvent.ACTION_UP){
                            for (TextButton button: multiplayerGameOverMenuElements){
                                if (button.isPressable){
                                    button.isPressed = 0;
                                    if (button.boundaries.contains((int)touchX, (int)touchY)){
                                        handleTextButton(button, getContext());
                                    }
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
                case 5:
                    if (event.getAction() == MotionEvent.ACTION_DOWN){
                        for (TextButton button: multiplayerMenuElements){
                            if (button.isPressable){
                                if (button.boundaries.contains((int)touchX, (int)touchY)){
                                    button.isPressed = 1;
                                }
                            }
                        }
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP){
                        for (TextButton button: multiplayerMenuElements){
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
                SharedPreferences sharedPrefs = context.getSharedPreferences("GAME_PREFERENCES", Context.MODE_PRIVATE);
                SharedPreferences.Editor prefEditor = sharedPrefs.edit();
                prefEditor.putBoolean("MUSIC_ON", musicOn);
                prefEditor.apply();
            }
            else if (button.text.equals("Quick Game")){
                Log.d("dasdsa", "About to start quick game");

                if (googleApiClient != null && !googleApiClient.isConnected()){
                    googleApiClient.connect();
                } else {
                    try{
                        Log.d("dasdsa", "Play services connected. About to start quick game");
                        ((LineMayhem)context).startQuickGame();
                    } catch (Exception e){
                        e.printStackTrace();
                        Toast toast = new Toast(context);
                        toast.makeText(context, "Google Play Services is not connected :(", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            }
            else if (button.text.equals("Invite Players")){
                Log.d("dasdsa", "About to start Invite window");

                if (googleApiClient != null && !googleApiClient.isConnected()){
                    googleApiClient.connect();
                } else {
                    try{
                        Log.d("dasdsa", "Play services connected. About to start Invite window");
                        ((LineMayhem)context).invitePlayers();
                    } catch (Exception e){
                        e.printStackTrace();
                        Toast toast = new Toast(context);
                        toast.makeText(context, "Google Play Services is not connected :(", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            }
            else if (button.text.equals("My Invites")){
                Log.d("dasdsa", "About to start ViewInvites window");

                if (googleApiClient != null && !googleApiClient.isConnected()){
                    googleApiClient.connect();
                } else {
                    try{
                        Log.d("dasdsa", "Play services connected. About to start ViewInvites window");
                        ((LineMayhem)context).viewInvites();
                    } catch (Exception e){
                        e.printStackTrace();
                        Toast toast = new Toast(context);
                        toast.makeText(context, "Google Play Services is not connected :(", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            }
            else if (button.text.equals("Play Again?")){
                Log.d("dasdsa", "About to send PlayerReady message to " + ((LineMayhem)context).mParticipants.get(0).getDisplayName());

                if (googleApiClient != null && !googleApiClient.isConnected()){
                    googleApiClient.connect();
                } else {
                    try{
                        if (!isHost){
                            ByteBuffer buffer = ByteBuffer.allocate(2);
                            buffer.putChar('S');
                            mMsgBuf = buffer.array();
                            Log.d("dasdsa", "Sending playerReady message to " + ((LineMayhem)context).mParticipants.get(0).getDisplayName());
                            Games.RealTimeMultiplayer.sendReliableMessage(googleApiClient, null, mMsgBuf, ((LineMayhem)context).mRoomId, ((LineMayhem)context).mParticipants.get(0).getParticipantId());
                            Log.d("dasdsa", "PlayerReady message sent to " + ((LineMayhem)context).mParticipants.get(0).getDisplayName());
                            loadingIndicator.setButton(button);
                            loadingIndicator.isVisible = true;
                            buffer.clear();
                        } else {
                            player.isReady = true;
                            boolean allPlayersReady = true;
                            for (Player player:otherPlayers)
                                if (player != null)
                                    if (!player.isReady)
                                        allPlayersReady = false;
                            if (allPlayersReady){
                                resetGame();
                                ByteBuffer buffer = ByteBuffer.allocate(2);
                                buffer.putChar('S');
                                mMsgBuf = buffer.array();
                                // Send to every other participant.
                                for (Participant p : ((LineMayhem)context).mParticipants) {
                                    if (p.getParticipantId().equals(((LineMayhem)context).mMyId))
                                        continue;
                                    if (p.getStatus() != Participant.STATUS_JOINED)
                                        continue;
                                    else
                                        // final score notification must be sent via reliable message
                                        Games.RealTimeMultiplayer.sendReliableMessage(googleApiClient, null, mMsgBuf,
                                                ((LineMayhem)context).mRoomId, p.getParticipantId());
                                }
                                buffer.clear();
                                loadingIndicator.isVisible = false;
                                nextGameState = 2;
                            } else {
                                loadingIndicator.setButton(button);
                                loadingIndicator.isVisible = true;
                            }

                        }
                    } catch (Exception e){
                        e.printStackTrace();
                        Toast toast = new Toast(context);
                        toast.makeText(context, "Google Play Services is not connected :(", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            }
        } else {
            nextGameState = button.statePointer;
        }
    }

    //Makes the player bounce off walls.
    public void checkBoundariesCollisions(){
        //Undecent, unpretty, hardcoded code
        if ((player.x - player.width/2) < Globals.BOUNDARY_WIDTH){
            player.x = (short)(Globals.BOUNDARY_WIDTH + player.width/2);
            player.setSpeedX(-player.speedX * 0.2);
        } else if ( player.x + player.width/2 > (Globals.GAME_WIDTH - Globals.BOUNDARY_WIDTH)){
            player.x = (short)((Globals.GAME_WIDTH - Globals.BOUNDARY_WIDTH) - player.width/2);
            player.setSpeedX(-player.speedX * 0.2);
        } else if ((player.y - player.height/2) < Globals.BOUNDARY_WIDTH){
            player.y = (short)(Globals.BOUNDARY_WIDTH + player.height/2);
            player.setSpeedY(-player.speedY * 0.2);
        } else if (player.y + player.height/2 > (Globals.GAME_HEIGHT - Globals.BOUNDARY_WIDTH)){
            player.y = (short)((Globals.GAME_HEIGHT - Globals.BOUNDARY_WIDTH) - player.height/2);
            player.setSpeedY(-player.speedY * 0.2);
        }

        if (multiplayer){
            for (Player player: otherPlayers){
                if (player != null){
                    //Undecent, unpretty, hardcoded code
                    if ((player.x - player.width/2) < Globals.BOUNDARY_WIDTH){
                        player.x = (short)(Globals.BOUNDARY_WIDTH + player.width/2);
                        player.setSpeedX(-player.speedX * 0.2);
                    } else if ( player.x + player.width/2 > (Globals.GAME_WIDTH - Globals.BOUNDARY_WIDTH)){
                        player.x = (short)((Globals.GAME_WIDTH - Globals.BOUNDARY_WIDTH) - player.width/2);
                        player.setSpeedX(-player.speedX * 0.2);
                    } else if ((player.y - player.height/2) < Globals.BOUNDARY_WIDTH){
                        player.y = (short)(Globals.BOUNDARY_WIDTH + player.height/2);
                        player.setSpeedY(-player.speedY * 0.2);
                    } else if (player.y + player.height/2 > (Globals.GAME_HEIGHT - Globals.BOUNDARY_WIDTH)){
                        player.y = (short)((Globals.GAME_HEIGHT - Globals.BOUNDARY_WIDTH) - player.height/2);
                        player.setSpeedY(-player.speedY * 0.2);
                    }
                }
            }
        }
    }

    public Player getOtherPlayer(String playerID){
        for (Player player: otherPlayers){
            if (player != null){
                //Log.d("getOtherPlayer()", player.ign);
            }
            if (player != null && player.id.equals(playerID))
                return player;
        }
        //Log.d("GetOtherPlayer", "Participant ID does not coincide with any of the room's players");
        return null;
    }

    //Sends the player's position to other players in the game
    public void broadcastPlayerPosition(){
        ByteBuffer buffer = ByteBuffer.allocate(6);
        buffer.putChar('P');
        buffer.putShort(player.x);
        buffer.putShort(player.y);
        mMsgBuf = buffer.array();
        if (nextGameState != 3){
            Log.d("BroadcastPosition", "Broadcasting position.");

            Games.RealTimeMultiplayer.sendUnreliableMessageToOthers(googleApiClient, mMsgBuf, context.mRoomId);
        }
        buffer.clear();
    }

    public void broadcastPlayerScoreAndFinalPosition(){
        Log.d("BroadcastScore", "Broadcasting score.");
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putChar('F');
        buffer.putShort(player.x);
        buffer.putShort(player.y);
        buffer.putShort((short)currentScore);
        mMsgBuf = buffer.array();
        // Send to every other participant.
        for (Participant p : context.mParticipants) {
            if (p.getParticipantId().equals(context.mMyId))
                continue;
            if (p.getStatus() != Participant.STATUS_JOINED)
                continue;
            else
                // final score notification must be sent via reliable message
                Games.RealTimeMultiplayer.sendReliableMessage(googleApiClient, null, mMsgBuf,
                        context.mRoomId, p.getParticipantId());
        }
        buffer.clear();
    }


    public void broadcastPlayerMovement(float movementX, float movementY){
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.putChar('M');
        buffer.putFloat(movementX);
        buffer.putFloat(movementY);
        mMovMsgBuf = buffer.array();
        Games.RealTimeMultiplayer.sendUnreliableMessageToOthers(googleApiClient, mMovMsgBuf, context.mRoomId);
        buffer.clear();
    }

    //Update function
    public void update(){
        switch (gameState){
            case 1:
                break;
            case 2:
                player.update();
                if (multiplayer){

                    //We broadcast the player's position every time it stops moving
                    if (player.speedX == 0 && player.speedY == 0 && player.isAlive) {
                        if (!hasBroadcastedPosition) {
                            broadcastPlayerPosition();
                            hasBroadcastedPosition = true;
                        }
                    } else {
                        hasBroadcastedPosition = false;
                    }

                    //Update code for player
                    if (!player.isAlive){
                        if (!hasBroadcastedFinalPosition){
                            broadcastPlayerScoreAndFinalPosition();
                            hasBroadcastedFinalPosition = true;
                        }
                        if (!player.hasExploded){
                            if(player.fadeoutCounter*40 >= 255){
                                player.fadeoutCounter = 0;
                                player.paint.setAlpha(0);
                                //Move the following line outside of the if block
                                boolean allPlayersDead = true;
                                for (Player player:otherPlayers)
                                    if (player != null){
                                        Log.d("Update", "Player " + player.ign + " isAlive: " + player.isAlive);
                                        if (player.isAlive)
                                            allPlayersDead = false;
                                    }
                                if (allPlayersDead){
                                    nextGameState = 3;
                                    Log.d("Update", "Calling game over screen...");
                                }
                                player.hasExploded = true;
                                player.isReady = false;
                            } else {
                                player.fadeoutCounter++;
                                player.radius += player.fadeoutCounter * 15;
                                player.paint.setAlpha(255-player.fadeoutCounter*40);
                            }
                        }
                    }

                    //Update code for other players
                    for (Player player: otherPlayers){
                        if (player != null){
                            player.update();
                            if (!player.isAlive){
                                if (!player.hasExploded){
                                    if(player.fadeoutCounter*40 >= 255){
                                        player.fadeoutCounter = 0;
                                        player.paint.setAlpha(0);
                                        player.hasExploded = true;
                                        //Log.d("updateOtherPlayers", "Alpha should be stuck at 0 for " + player.ign + "- Alpha: " + player.paint.getAlpha());
                                    } else {
                                        player.fadeoutCounter++;
                                        //Log.d("updateOtherPlayers", "Lowering alpha for " + player.ign + "- Alpha: " + player.paint.getAlpha());
                                        player.radius += player.fadeoutCounter * 15;
                                        player.paint.setAlpha(255-player.fadeoutCounter*40);
                                    }
                                }
                            }
                        }
                    }
                }
                else if (!player.isAlive) {
                    Log.d("Update", "Not playing in multiplayer mode!");
                    nextGameState = 3;
                    player.isAlive = true;
                }
                checkBoundariesCollisions();

                spawner.update(player.x, player.y, this);
                break;
            case 3:
                if (multiplayer && !isHost && otherPlayers[0] != null && otherPlayers[0].isReady){
                    loadingIndicator.isVisible = false;
                    resetGame();
                    otherPlayers[0].isReady = false;
                    player.isReady = false;
                    nextGameState = 2;
                }
                spawner.update(player.x, player.y, this);
                break;
            case 4:
                spawner.update(player.x, player.y, this);
                player.update();

                //This....animation code...is good, innit?
                if (instructionsAnimationTimer < 170){
                    player.x = Globals.GAME_WIDTH/2;
                    player.y = Globals.GAME_HEIGHT/2;
                }
                else if  (instructionsAnimationTimer < 200){
                    player.movePlayer(2.5f, -0.7f);
                } else if (instructionsAnimationTimer < 280){}
                else if (instructionsAnimationTimer < 320){
                    player.movePlayer(-1f, 2f);
                } else if (instructionsAnimationTimer < 420){}
                else if (instructionsAnimationTimer < 460){
                    player.movePlayer(-1.5f, -0.5f);
                } else if (instructionsAnimationTimer < 510){}
                else if (instructionsAnimationTimer < 540){
                    player.movePlayer(-0.5f, -2.7f);
                } else if (instructionsAnimationTimer < 600){}
                else if (instructionsAnimationTimer < 630){
                    player.movePlayer(3.46f, 3.5f);
                } else if (instructionsAnimationTimer < 660){}
                else {
                    player.x = Globals.GAME_WIDTH /2;
                    player.y = Globals.GAME_HEIGHT / 2;
                    instructionsAnimationTimer = 0;
                }
                instructionsAnimationTimer++;
                break;
            case 5:
                if (googleApiClient != null && !googleApiClient.isConnected())
                    googleApiClient.connect();
                if (multiplayer && !isHost && otherPlayers[0] != null && otherPlayers[0].isReady){
                    loadingIndicator.isVisible = false;
                    nextGameState = 2;
                    otherPlayers[0].isReady = false;
                    player.isReady = false;
                }
                break;

        }
        handleGameStateChanges();
    }

    //Custom behaviour for different state changes is coded here. Uses the
    public void handleGameStateChanges(){
        switch (nextGameState){
            //This happens if there is no pending gameState change.
            case 0:
                break;
            case 1:
                mediaPlayer.pause();
                if (multiplayer && googleApiClient.isConnected()){
                    context.leaveRoom();
                    player.isReady = false;
                    otherPlayers = new Player[3];
                }
                multiplayer = false;
                isHost = false;
                otherPlayers = new Player[3];
                gameState = 1;
                nextGameState = 0;
                break;
            //This happens when the play button is pressed
            case 2:
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
                player.fadeoutCounter++;

                mediaPlayer.setVolume(1 - 0.1f*player.fadeoutCounter, 1 - 0.1f*player.fadeoutCounter);

                player.radius += player.fadeoutCounter * 15;
                player.paint.setAlpha(255-player.fadeoutCounter*40);

                for (TextButton element: gameOverMenuElements){
                    element.y = element.defaultY + gameStateTimer;
                }
                gameStateTimer *= 0.7;

                if (gameStateTimer <= 1){
                    gameState = 3;
                    nextGameState = 0;

                    mediaPlayer.setVolume(0, 0);
                    mediaPlayer.pause();
                    loadingIndicator.isVisible = false;

                    player.paint.setAlpha(255);
                    player.radius = 10;
                    player.fadeoutCounter = 0;

                    for (Player player:otherPlayers){
                        if (player != null){
                            player.paint.setAlpha(255);
                            player.fadeoutCounter = 0;
                            player.radius = 10;
                        }
                    }

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


                    SharedPreferences sharedPrefs = context.getSharedPreferences("GAME_PREFERENCES", Context.MODE_PRIVATE);
                    SharedPreferences.Editor prefEditor = sharedPrefs.edit();
                    if (sharedPrefs.getInt("HIGH_SCORE", 0) < currentScore){
                        prefEditor.putInt("HIGH_SCORE", currentScore);
                        prefEditor.apply();
                        prefEditor.commit();
                    }

                    Games.Leaderboards.submitScore(googleApiClient, LEADERBOARD_ID, sharedPrefs.getInt("HIGH_SCORE", 0));
                    Log.d("GameOverScreen", "Score submitted to google play: " + sharedPrefs.getInt("HIGH_SCORE", 0));
                    //Games.Leaderboards.loadCurrentPlayerLeaderboardScore(googleApiClient, LEADERBOARD_ID, LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC);

                    gameStateTimer = Globals.GAME_HEIGHT + 200;
                }
                break;
            case 4:
                resetGame();
                player.y = 700;
                gameState = 4;
                nextGameState = 0;
                break;
            case 5:
                isHost = false;
                multiplayer = true;
                gameState = 5;
                nextGameState = 0;
                break;
        }
    }

    //Guess what this does
    public void resetGame(){
        spawner.reset(this);
        player.reset();
        currentScore = 0;
        player.isAlive = true;
        player.hasExploded = false;
        if (!multiplayer)
            player.setSkin("White");
        for (int i=0; i<otherPlayers.length; i++){
            if (otherPlayers[i] != null){
                otherPlayers[i].isReady = false;
                otherPlayers[i].isAlive = true;
                otherPlayers[i].hasExploded = false;
            }
        }
        nameTagDisplayTimer = 200;

        for (int i = 8; i < 11; i++){
            multiplayerGameOverMenuElements[i].setText("");
        }
        gameOverScreenReady = false;

        hasBroadcastedFinalPosition = false;

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
                    button.draw(canvas);
                }

                if (notificationsButton.isVisible)
                    notificationsButton.draw(canvas);
                break;
            case 2:
                player.draw(canvas);
                spawner.draw(canvas);

                if (nameTagDisplayTimer >= 0)
                    nameTagDisplayTimer--;
                if(multiplayer) {
                    for (Player player: otherPlayers){
                        if (player != null && player.id != null){
                            player.draw(canvas);
                            if (nameTagDisplayTimer >= 0){
                                player.nameTag.draw(canvas);
                            }
                        }
                    }
                }

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
                if (multiplayer){
                    if (!gameOverScreenReady){
                        if (multiplayerGameOverMenuElements.length > 8){
                            int i = 8;
                            for (Player player: otherPlayers){
                                if (player != null){
                                    multiplayerGameOverMenuElements[i].setText(player.ign + " survived " + player.score + " killer lines!");
                                    multiplayerGameOverMenuElements[i].setColor(player.paint.getColor());
                                    multiplayerGameOverMenuElements[i].setFontSize(38 - player.ign.length());
                                }
                                i++;
                            }
                        }
                        gameOverScreenReady = true;
                    }

                    for (TextButton button: multiplayerGameOverMenuElements){
                        if (button.statePointer == 6){
                            button.text = String.valueOf(currentScore);
                        }
                        if (button.text.equals("Waiting for other players...")){
                            if (loadingIndicator.isVisible){
                                button.draw(canvas);
                            }
                        } else {
                            button.draw(canvas);
                        }
                    }
                    if (loadingIndicator.isVisible){
                        loadingIndicator.draw(canvas);
                    }
                } else {
                    for (TextButton button: gameOverMenuElements){
                        if (button.statePointer == 6){
                            button.text = String.valueOf(currentScore);
                        }
                        button.draw(canvas);
                    }
                }

                if (notificationsButton.isVisible)
                    notificationsButton.draw(canvas);
                break;
            case 4:

                for (TextButton button: instructionsMenuElements){
                    button.draw(canvas);
                }

                player.draw(canvas);
                spawner.draw(canvas);
                if (notificationsButton.isVisible)
                    notificationsButton.draw(canvas);
                break;
            case 5:
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
                for (TextButton button: multiplayerMenuElements){
                    button.draw(canvas);
                }
                if (notificationsButton.isVisible)
                    notificationsButton.draw(canvas);

                if (loadingIndicator.isVisible)
                    loadingIndicator.draw(canvas);
                break;
        }

        //The game frame (game boundaries) is (are) always drawn
        boundaries.draw(canvas);

        canvas.restoreToCount(savedCanvasState);

        //I'm not sure is this is crucial, but it should be tested on Jellybean devices, that's where it might break if
        //invalidate();

    }
}
