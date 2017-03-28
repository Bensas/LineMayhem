package com.MightyBarbet.LineMayhem;

import android.graphics.Canvas;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Participant;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Bensas on 01/06/16. (June)
 *
 * This class instantiantes lines and circles, managing their starting position, whether they're
 * horizontal or vertical, the direction in which they explode, the speed at which they form, etc.
 */
public class Spawner {
    Random rnd = new Random();
    KillerHorizontalLine[] horizontalLines = new KillerHorizontalLine[4];
    KillerVerticalLine[] verticalLines = new KillerVerticalLine[4];
    int vLineCount;

    int lineBufferSize = 20, currentLineIndex;
    boolean[] lineTypes, lineDirections, lineStartingSides;
    float[] lineSpeeds, lineRates;
    short[] lineStartPos;

    int timer = 100, timerReduction = 30; //0= horizontal line, 1= vertical line

    public void update(int playerX, int playerY, MainGameScript mainGame){
        timer -= 1;
        if (mainGame.gameState == 2){
            //Every one second, we spawn a line and reset the timer
            if (timer <= 0){
                if (!mainGame.multiplayer || mainGame.isHost){
                    if (rnd.nextBoolean()){
                        //Log.d(getClass().getSimpleName(), "Generating Horizontal line...");
                        broadcastHorizontalLine(checkForCloseHorizontalETAs(createHorizontalLine(playerY)), mainGame);
                    } else {
                        //Log.d(getClass().getSimpleName(), "Generating Vertical line...");
                        broadcastVerticalLine(checkForCloseVerticalETAs(createVerticalLine(playerX)), mainGame);
                    }


                    //Each time we reset the timer, we set it lower than before
                    timer = 100 - timerReduction;
                    timerReduction += 3;
                    Log.d(getClass().getSimpleName(), "Time reduction: " + timerReduction);

                    //We limit the timer reduction (This is when the player reached max difficulty)
                    if (timerReduction > 56){
                        //Log.d(getClass().getSimpleName(), "Max difficulty reached!");
                        if (vLineCount == 4){
                            for (int i = 0; i < verticalLines.length && vLineCount == 4; i++){
                                if (verticalLines[i] != null){
                                    if (verticalLines[i].state == 0){
                                        verticalLines[i] = null;
                                        vLineCount = 3;
                                        Log.d("Spawnerr", "REMOVING ONE VERTICAL LINE");
                                    }
                                }
                            }
                        }
                        timerReduction = 56;
                    }
                }
            }
        }
        //We set a specific spawning pattern for the instructions menu
        else if (mainGame.gameState == 4){
            if (mainGame.instructionsAnimationTimer == 130){
                createVerticalLine(0).resetLineWithCustomAttributes(true, false, 10f, 300, 0, 0.3f);
            }
            else if (mainGame.instructionsAnimationTimer == 270){
                createHorizontalLine(0).resetLineWithCustomAttributes(false, true, 10f, 0, 500, 0.3f);
            }
            else if (mainGame.instructionsAnimationTimer == 410){
                createVerticalLine(0).resetLineWithCustomAttributes(false, true, 9f, 150, Globals.GAME_HEIGHT, 0.3f);
            }
            else if (mainGame.instructionsAnimationTimer == 500 ){
                createHorizontalLine(0).resetLineWithCustomAttributes(true, false, 8f, Globals.GAME_WIDTH, 600, -0.3f);
            }
        }


        //Update lines
        for (KillerHorizontalLine line:horizontalLines) {
            if (line != null) {
                line.update();
                if (line.state == 2){
                    //playDestroySound();
                    if (mainGame.gameState == 2 && mainGame.player.isAlive){
                        if (line.direction){
                            if (line.startingSide && playerY < line.rate * (Globals.GAME_WIDTH - playerX) + line.startY){
                                //Log.d("Spawner.update()", "HORIZONTAL UP LINE(right) - PlayerPosition= (" + playerX + ", " + playerY + ") - Rate: " + line.rate + "- StartY: " + line.startY + "Line state: " + line.state);
                                mainGame.player.isAlive = false;
                            } else if (!line.startingSide && playerY < line.rate * playerX + line.startY){
                                //Log.d("Spawner.update()", "HORIZONTAL UP LINE(left) - PlayerPosition= (" + playerX + ", " + playerY + ") - Rate: " + line.rate + "- StartY: " + line.startY + "Line state: " + line.state);
                                mainGame.player.isAlive = false;
                            }
                        } else {
                            if (line.startingSide && playerY > line.rate * (Globals.GAME_WIDTH - playerX) + line.startY) {
                                //Log.d("Spawner.update()", "HORIZONTAL DOWN LINE - PlayerPosition= (" + playerX + ", " + playerY + ") - Rate: " + (float)line.rate + "- StartY: " + line.startY + "Line state: " + line.state);
                                mainGame.player.isAlive = false;
                            } else if (!line.startingSide && playerY > line.rate * playerX + line.startY) {
                                //Log.d("Spawner.update()", "HORIZONTAL DOWN LINE - PlayerPosition= (" + playerX + ", " + playerY + ") - Rate: " + line.rate + "- StartY: " + line.startY + "Line state: " + line.state);
                                mainGame.player.isAlive = false;
                            }
                        }
                    }
                }
                else if (line.state == 4){
                    if (mainGame.gameState == 2 && mainGame.nextGameState == 0 && mainGame.player.isAlive){
                        mainGame.currentScore += 1;
                    }
                    line.state = 0;
                    line.rate = 0;
                }
            }
        }
        for (KillerVerticalLine line: verticalLines){
            if (line != null){
                line.update();
                if (line.state == 2) {
                    //playDestroySound();
                    if (mainGame.gameState == 2 && mainGame.player.isAlive){
                        if (line.direction) {
                            if (line.startingSide && playerX > line.rate * playerY + line.startX) {
                                //Log.d("Spawner.update()", "VERTICAL RIGHT LINE - PlayerPosition= (" + playerX + ", " + playerY + ") - Rate: " + line.rate + "- StartX: " + line.startX + "Line state: " + line.state);
                                mainGame.player.isAlive = false;
                            } else if (!line.startingSide && playerX > line.rate * (Globals.GAME_HEIGHT - playerY) + line.startX) {
                                //Log.d("Spawner.update()", "VERTICAL RIGHT LINE - PlayerPosition= (" + playerX + ", " + playerY + ") - Rate: " + line.rate + "- StartX: " + line.startX + "Line state: " + line.state);
                                mainGame.player.isAlive = false;
                            }
                        } else {
                            if (line.startingSide && playerX < line.rate * playerY + line.startX) {
                                //Log.d("Spawner.update()", "VERTICAL LEFT LINE - PlayerPosition= (" + playerX + ", " + playerY + ") - Rate: " + line.rate + "- StartX: " + line.startX + "Line state: " + line.state);
                                mainGame.player.isAlive = false;
                            } else if (!line.startingSide && playerX < line.rate * (Globals.GAME_HEIGHT - playerY) + line.startX) {
                                //Log.d("Spawner.update()", "VERTICAL RIGHT LINE - PlayerPosition= (" + playerX + ", " + playerY + ") - Rate: " + line.rate + "- StartX: " + line.startX + "Line state: " + line.state);
                                mainGame.player.isAlive = false;
                            }
                        }
                    }
                } else if (line.state == 4){
                    if (mainGame.gameState == 2 && mainGame.nextGameState == 0 && mainGame.player.isAlive){
                        mainGame.currentScore += 1;
                    }
                    line.state = 0;
                    line.rate = 0;
                }
            }
        }
    }

//    public void generateLineBuffer(){
//        for (int i = 0; i< lineBufferSize; i++){
//            if (rnd.nextBoolean()){
//                //Log.d(getClass().getSimpleName(), "Generating Horizontal line...");
//                checkForCloseHorizontalETAs(createHorizontalLine(playerY)), mainGame;
//            } else {
//                //Log.d(getClass().getSimpleName(), "Generating Vertical line...");
//                checkForCloseVerticalETAs(createVerticalLine(playerX)), mainGame);
//            }
//        }
//    }

    public KillerHorizontalLine createHorizontalLine(int playerY){
        for (KillerHorizontalLine line: horizontalLines){
            if (line != null) {
                if (line.state == 0){
                    line.resetLine(playerY, rnd);
                    line.state = 1;
                    return  line;
                }
            }
        }
        //Log.d("CreateHorizontalLine", "No inactive(available) lines found. :(");
        return  null;
    }

    public KillerVerticalLine createVerticalLine(int playerX){
        for (KillerVerticalLine line: verticalLines){
            if (line != null){
                if (line.state == 0){
                    line.resetLine(playerX, rnd);
                    line.state = 1;
                    return line;
                }
            }
        }
        //Log.d("CreateVerticalLine", "No inactive(available) lines found. :(");
        return null;
    }

    public void broadcastHorizontalLine(KillerHorizontalLine line, MainGameScript mainGame){
        if (!mainGame.multiplayer || line.state == 0)
            return;
        ByteBuffer buffer = ByteBuffer.allocate(22);
        buffer.putChar('L');
        buffer.putChar('H');
        buffer.putShort(line.startingSide?(short)1:(short)0);
        buffer.putShort(line.direction?(short)1:(short)0);
        buffer.putFloat(line.speed);
        buffer.putShort((short)line.startX);
        buffer.putShort((short)line.startY);
        buffer.putFloat(line.rate);
        mainGame.mMovMsgBuf = buffer.array();
        //Log.d("BroadcastLine", "Line buffer successfully created");
        //Games.RealTimeMultiplayer.sendUnreliableMessageToOthers(mainGame.googleApiClient, mainGame.mMovMsgBuf, mainGame.context.mRoomId);
        // Send to every other participant.
        for (Participant p : mainGame.context.mParticipants) {
            if (p.getParticipantId().equals(mainGame.context.mMyId))
                continue;
            if (p.getStatus() != Participant.STATUS_JOINED)
                continue;
            else
                // final score notification must be sent via reliable message
                Games.RealTimeMultiplayer.sendReliableMessage(mainGame.googleApiClient, null, mainGame.mMovMsgBuf,
                        mainGame.context.mRoomId, p.getParticipantId());
        }
        buffer.clear();
    }

    public void broadcastVerticalLine(KillerVerticalLine line, MainGameScript mainGame){
        if (!mainGame.multiplayer || line.state == 0)
            return;
        ByteBuffer buffer = ByteBuffer.allocate(22);
        buffer.putChar('L');
        buffer.putChar('V');
        buffer.putShort(line.startingSide?(short)1:(short)0);
        buffer.putShort(line.direction?(short)1:(short)0);
        buffer.putFloat(line.speed);
        buffer.putShort((short)line.startX);
        buffer.putShort((short)line.startY);
        buffer.putFloat(line.rate);
        mainGame.mMovMsgBuf = buffer.array();
        //Log.d("BroadcastLine", "Line buffer successfully created");
        //Games.RealTimeMultiplayer.sendUnreliableMessageToOthers(mainGame.googleApiClient, mainGame.mMovMsgBuf, mainGame.context.mRoomId);
        // Send to every other participant.
        for (Participant p : mainGame.context.mParticipants) {
            if (p.getParticipantId().equals(mainGame.context.mMyId))
                continue;
            if (p.getStatus() != Participant.STATUS_JOINED)
                continue;
            else
                // final score notification must be sent via reliable message
                Games.RealTimeMultiplayer.sendReliableMessage(mainGame.googleApiClient, null, mainGame.mMovMsgBuf,
                        mainGame.context.mRoomId, p.getParticipantId());
        }
        buffer.clear();
    }


    //We use this method to avoid two opposite-direction lines exploding (roughly) at the same time in a way that
    //makes it impossible for the player to survive.
    public KillerHorizontalLine checkForCloseHorizontalETAs(KillerHorizontalLine line){
        for (KillerHorizontalLine horizontalLine: horizontalLines){
            if (horizontalLine != null){
                if (horizontalLine != line && horizontalLine.direction != line.direction && Math.abs(horizontalLine.ETA - line.ETA) < (long)600000000){
                    line.state = 0;
                    line.rate = 0;
                    //Log.d(getClass().getSimpleName(), "CLOSE ETAs DETECTED FOR TWO HORIZONTAL LINES!" + horizontalLine.ETA + " - " + line.ETA + " - Abs value: " + Math.abs(horizontalLine.ETA - line.ETA));
                }
            }
        }
        return line;
    }

    public KillerVerticalLine checkForCloseVerticalETAs(KillerVerticalLine line){
        for (KillerVerticalLine verticalLine: verticalLines){
            if (verticalLine != null){
                if (verticalLine != line && verticalLine.direction != line.direction && Math.abs(verticalLine.ETA - line.ETA) < (long)600000000){
                    line.state = 0;
                    line.rate = 0;
                    //Log.d(getClass().getSimpleName(), "CLOSE ETAs DETECTED FOR TWO VERTICAL LINES!" + verticalLine.ETA + " - " + line.ETA + " - Abs value: " + Math.abs(verticalLine.ETA - line.ETA));
                }
            }
        }
        return line;
    }


    public void draw(Canvas canvas){
        //int counter = 0;
        for (KillerHorizontalLine line: horizontalLines){
            if (line != null)
                line.draw(canvas);
            //Log.d("Spawner.draw()", "Drew line number " + counter + ". Rate: " + line.rate + ". State: " + line.state);
            //counter++;
        }
        for (KillerVerticalLine line: verticalLines){
            if (line != null)
                line.draw(canvas);
            //Log.d("Spawner.draw()", "Drew line number " + counter + ". Rate: " + line.rate + ". State: " + line.state);
            //counter++;
        }
    }

    public void reset(){
        for (int i = 0; i < 4; i++){
            verticalLines[i] = new KillerVerticalLine();
            horizontalLines[i] = new KillerHorizontalLine();
        }
        vLineCount = 4;
        timerReduction = 0;
        timer = 100;
    }
}
