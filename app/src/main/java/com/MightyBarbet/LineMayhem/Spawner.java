package com.MightyBarbet.LineMayhem;

import android.graphics.Canvas;
import android.graphics.Paint;
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

    int lineBufferSize = 20;
    int currentLineBuffer = 0, nextLineBuffer = 0;
    LineBuffer[] lineBuffers = new LineBuffer[]{
            new LineBuffer(lineBufferSize),
            new LineBuffer(lineBufferSize)
    };
    int lineBufferIndex = 0;

    int timer = 150, timerReduction = 30; //0= horizontal line, 1= vertical line

    public void update(int playerX, int playerY, MainGameScript mainGame){
        timer -= 1;
        if (mainGame.gameState == 2){
            //Every one second, we spawn a line and reset the timer
            if (timer <= 0){
                if (!mainGame.multiplayer || mainGame.isHost){
                    if (!mainGame.multiplayer){
                        if (rnd.nextBoolean()){
                            //Log.d(getClass().getSimpleName(), "Generating Horizontal line...");
                            broadcastHorizontalLine(checkForCloseHorizontalETAs(createHorizontalLine(playerY, System.nanoTime(), mainGame)), mainGame);
                        } else {
                            //Log.d(getClass().getSimpleName(), "Generating Vertical line...");
                            broadcastVerticalLine(checkForCloseVerticalETAs(createVerticalLine(playerX, System.nanoTime(), mainGame)), mainGame);
                        }
                    } else {
                        if (lineBuffers[currentLineBuffer].lineTypes[lineBufferIndex] == 1){
                            broadcastHorizontalLine(checkForCloseHorizontalETAs(createHorizontalLine(playerY, System.currentTimeMillis(), mainGame)), mainGame);
                        } else {
                            broadcastVerticalLine(checkForCloseVerticalETAs(createVerticalLine(playerX, System.currentTimeMillis(), mainGame)), mainGame);
                        }
                    }

                    Log.d("Spawner", "Line created");

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
                                        Log.d("Spawner", "REMOVING ONE VERTICAL LINE");
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
                createVerticalLine(0, System.nanoTime(), mainGame).resetLineWithCustomAttributes(true, false, 10f, 300, 0, 0.3f);
            }
            else if (mainGame.instructionsAnimationTimer == 270){
                createHorizontalLine(0, System.nanoTime(), mainGame).resetLineWithCustomAttributes(false, true, 10f, 0, 500, 0.3f);
            }
            else if (mainGame.instructionsAnimationTimer == 410){
                createVerticalLine(0, System.nanoTime(), mainGame).resetLineWithCustomAttributes(false, true, 9f, 150, Globals.GAME_HEIGHT, 0.3f);
            }
            else if (mainGame.instructionsAnimationTimer == 500 ){
                createHorizontalLine(0, System.nanoTime(), mainGame).resetLineWithCustomAttributes(true, false, 8f, Globals.GAME_WIDTH, 600, -0.3f);
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

    public void resetTimer(){
        //Each time we reset the timer, we set it lower than before
        timer = 100 - timerReduction;
        timerReduction += 3;

        if (timerReduction > 56){
            //Log.d(getClass().getSimpleName(), "Max difficulty reached!");
            if (vLineCount == 4){
                for (int i = 0; i < verticalLines.length && vLineCount == 4; i++){
                    if (verticalLines[i] != null){
                        if (verticalLines[i].state == 0){
                            verticalLines[i] = null;
                            vLineCount = 3;
                            Log.d("Spawner", "REMOVING ONE VERTICAL LINE");
                        }
                    }
                }
            }
            timerReduction = 56;
        }
    }
    public void checkForLineBufferGenerationAndBufferSwitch(MainGameScript mainGame){
        if (lineBufferIndex == lineBufferSize/2 && mainGame.isHost){
            Log.d("Spawner", "Generating new line buffer!");
            generateAndBroadcastLineBuffer(mainGame);
        } else if (lineBufferIndex == lineBufferSize - 1){
            lineBufferIndex = 0;
            currentLineBuffer = currentLineBuffer==0?1:0;
            Log.d("Spawner", "Switched to the line buffer " + currentLineBuffer + "!");
        }
    }

    public void generateAndBroadcastLineBuffer(MainGameScript mainGame){
        if (!mainGame.multiplayer)
            return;
        Log.d("Spawner", "Generating line atributes...");

        for (int i = 0; i< lineBufferSize; i++){
            lineBuffers[nextLineBuffer].lineTypes[i] = rnd.nextBoolean()?(short)1:0;
            lineBuffers[nextLineBuffer].lineDirections[i] = rnd.nextBoolean()?(short)1:0;
            lineBuffers[nextLineBuffer].lineStartingSides[i] = rnd.nextBoolean()?(short)1:0;
            lineBuffers[nextLineBuffer].lineSpeedRnds[i] = rnd.nextFloat();
            lineBuffers[nextLineBuffer].lineRateFloatRnds[i] = rnd.nextFloat();
            lineBuffers[nextLineBuffer].lineStartRnds[i] = lineBuffers[nextLineBuffer].lineTypes[i]==1?(short)rnd.nextInt(400):(short)rnd.nextInt(350);
        }

        //2 bytes for the 'L' character. Short size = 2 bytes, float size = 4 bytes
        ByteBuffer buffer = ByteBuffer.allocate(2 + (4 * lineBufferSize * 2) + (2 * lineBufferSize * 4));
        buffer.putChar('L');
        for (int i = 0; i < lineBufferSize; i++){
            buffer.putShort(lineBuffers[nextLineBuffer].lineTypes[i]);
            buffer.putShort(lineBuffers[nextLineBuffer].lineDirections[i]);
            buffer.putShort(lineBuffers[nextLineBuffer].lineStartingSides[i]);
            buffer.putFloat(lineBuffers[nextLineBuffer].lineSpeedRnds[i]);
            buffer.putFloat(lineBuffers[nextLineBuffer].lineRateFloatRnds[i]);
            buffer.putShort(lineBuffers[nextLineBuffer].lineStartRnds[i]);
        }
        for (short attribute:lineBuffers[nextLineBuffer].lineTypes){
            Log.d("Spawner", "Line type: " + attribute);
        }
        mainGame.mMovMsgBuf = buffer.array();
        nextLineBuffer = nextLineBuffer==0?1:0;
        Log.d("Spawner", "Next line buffer: " + nextLineBuffer);
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

    public KillerHorizontalLine createHorizontalLine(int playerY, long startTime, MainGameScript mainGame){
        for (KillerHorizontalLine line: horizontalLines){
            if (line != null) {
                if (line.state == 0){
                    if (mainGame.multiplayer){
//                        Log.d("Spawner", "Cerating vertical line...:" + lineBuffers[currentLineBuffer].lineStartingSides[lineBufferIndex] + "\n" +
//                                lineBuffers[currentLineBuffer].lineDirections[lineBufferIndex] + "\n" +
//                                lineBuffers[currentLineBuffer].lineSpeedRnds[lineBufferIndex] + "\n" +
//                                lineBuffers[currentLineBuffer].lineRateFloatRnds[lineBufferIndex] + "\n" +
//                                lineBuffers[currentLineBuffer].lineStartRnds[lineBufferIndex]);

                        line.resetLineWithCustomAttributes(lineBuffers[currentLineBuffer].lineStartingSides[lineBufferIndex] == 1,
                                lineBuffers[currentLineBuffer].lineDirections[lineBufferIndex] == 1,
                                lineBuffers[currentLineBuffer].lineSpeedRnds[lineBufferIndex],
                                lineBuffers[currentLineBuffer].lineRateFloatRnds[lineBufferIndex],
                                lineBuffers[currentLineBuffer].lineStartRnds[lineBufferIndex],
                                playerY,
                                startTime);
                        Log.d("Spawner", "Cerating horizontal line...:" + line.startingSide + "\n" +
                                line.direction + "\n" +
                                line.speed + "\n" +
                                line.rate + "\n" +
                                line.startY);
                        lineBufferIndex++;
                        checkForLineBufferGenerationAndBufferSwitch(mainGame);
                        //Log.d("Spawner", "Horizontal line created");
                    } else {
                        line.resetLine(playerY, rnd);
                    }
                    line.state = 1;
                    return  line;
                }
            }
        }
        //Log.d("CreateHorizontalLine", "No inactive(available) lines found. :(");
        return  null;
    }

    public KillerVerticalLine createVerticalLine(int playerX, long startTime, MainGameScript mainGame){
        for (KillerVerticalLine line: verticalLines){
            if (line != null){
                if (line.state == 0){
                    if (mainGame.multiplayer){
//                        Log.d("Spawner", "Cerating vertical line...:" + lineBuffers[currentLineBuffer].lineStartingSides[lineBufferIndex] + "\n" +
//                                lineBuffers[currentLineBuffer].lineDirections[lineBufferIndex] + "\n" +
//                                lineBuffers[currentLineBuffer].lineSpeedRnds[lineBufferIndex] + "\n" +
//                                lineBuffers[currentLineBuffer].lineRateFloatRnds[lineBufferIndex] + "\n" +
//                                lineBuffers[currentLineBuffer].lineStartRnds[lineBufferIndex]);

                        line.resetLineWithCustomAttributes(lineBuffers[currentLineBuffer].lineStartingSides[lineBufferIndex] == 1,
                                lineBuffers[currentLineBuffer].lineDirections[lineBufferIndex] == 1,
                                lineBuffers[currentLineBuffer].lineSpeedRnds[lineBufferIndex],
                                lineBuffers[currentLineBuffer].lineRateFloatRnds[lineBufferIndex],
                                lineBuffers[currentLineBuffer].lineStartRnds[lineBufferIndex],
                                playerX,
                                startTime);
                        Log.d("Spawner", "Cerating vertical line...:" + line.startingSide + "\n" +
                                line.direction + "\n" +
                                line.speed + "\n" +
                                line.rate + "\n" +
                                line.startX);
                        lineBufferIndex++;
                        checkForLineBufferGenerationAndBufferSwitch(mainGame);
                        //Log.d("Spawner", "Vertical line created");
                    } else {
                        line.resetLine(playerX, rnd);
                    }
                    line.state = 1;
                    return  line;
                }
            }
        }
        //Log.d("CreateVerticalLine", "No inactive(available) lines found. :(");
        return null;
    }

    public void broadcastHorizontalLine(KillerHorizontalLine line, MainGameScript mainGame){
        //Log.d("Spawner", "About to broadcast Horizontal line");
        if (!mainGame.multiplayer)
            return;
        if (line.state == 0){
            ByteBuffer buffer2 = ByteBuffer.allocate(2);
            buffer2.putChar('n');
            mainGame.mMovMsgBuf = buffer2.array();
            buffer2.clear();
        } else {
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.putChar('l');
            buffer.putShort(mainGame.player.y);
//            buffer.putLong(line.startTime);
            mainGame.mMovMsgBuf = buffer.array();
            buffer.clear();
        }

        //Log.d("BroadcastLine", "Line buffer successfully created");
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
    }

    public void broadcastVerticalLine(KillerVerticalLine line, MainGameScript mainGame){
        //Log.d("Spawner", "About to broadcast Vertical line");
        if (!mainGame.multiplayer)
            return;
        if (line.state == 0){
            ByteBuffer buffer2 = ByteBuffer.allocate(2);
            buffer2.putChar('n');
            mainGame.mMovMsgBuf = buffer2.array();
            buffer2.clear();
        } else {
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.putChar('l');
            buffer.putShort(mainGame.player.x);
//            buffer.putLong(line.startTime);
            mainGame.mMovMsgBuf = buffer.array();
            buffer.clear();
        }
        //Log.d("BroadcastLine", "Line buffer successfully created");
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
    }


    //We use this method to avoid two opposite-direction lines exploding (roughly) at the same time in a way that
    //makes it impossible for the player to survive.
    public KillerHorizontalLine checkForCloseHorizontalETAs(KillerHorizontalLine line){
        for (KillerHorizontalLine horizontalLine: horizontalLines){
            if (horizontalLine != null){
                if (horizontalLine != line && horizontalLine.direction != line.direction && Math.abs(horizontalLine.ETA - line.ETA) < (long)600){
                    line.state = 0;
                    line.rate = 0;
                    Log.d(getClass().getSimpleName(), "CLOSE ETAs DETECTED FOR TWO HORIZONTAL LINES!" + horizontalLine.ETA + " - " + line.ETA + " - Abs value: " + Math.abs(horizontalLine.ETA - line.ETA));
                }
            }
        }
        //Log.d("Spawner", "Checking close ETAs");
        return line;
    }

    public KillerVerticalLine checkForCloseVerticalETAs(KillerVerticalLine line){
        for (KillerVerticalLine verticalLine: verticalLines){
            if (verticalLine != null){
                if (verticalLine != line && verticalLine.direction != line.direction && Math.abs(verticalLine.ETA - line.ETA) < (long)600){
                    line.state = 0;
                    line.rate = 0;
                    Log.d(getClass().getSimpleName(), "CLOSE ETAs DETECTED FOR TWO VERTICAL LINES!" + verticalLine.ETA + " - " + line.ETA + " - Abs value: " + Math.abs(verticalLine.ETA - line.ETA));
                }
            }
        }
        //Log.d("Spawner", "Checking close ETAs");
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

    public void reset(MainGameScript mainGame){
        for (int i = 0; i < 4; i++){
            verticalLines[i] = new KillerVerticalLine(this);
            horizontalLines[i] = new KillerHorizontalLine(this);
        }
        for (LineBuffer buffer: lineBuffers){
            buffer = new LineBuffer(lineBufferSize);
            currentLineBuffer = 0;
            nextLineBuffer = 0;
            lineBufferIndex = 0;
        }
        if (mainGame.multiplayer && mainGame.isHost)
            generateAndBroadcastLineBuffer(mainGame);
        vLineCount = 4;
        timerReduction = 0;
        timer = 100;
    }
}
