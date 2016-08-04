package mightybarbet.swift;

import android.graphics.Canvas;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.Log;

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
    ArrayList<KillerHorizontalLine> horizontalLines = new ArrayList<>();
    ArrayList<KillerVerticalLine> verticalLines = new ArrayList<>();

    SoundPool soundPool;
    int soundId1, soundId2, soundId3, soundId4;
    AudioAttributes audioAttributes;
    int soundTimer = 0;
    MediaPlayer sound1, sound2, sound3;

    int timer = 100, timerReduction = 30; //0= horizontal line, 1= vertical line

    public Spawner (MainGameScript mainGame){
        soundTimer = 0;
        soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        soundId1 = soundPool.load(mainGame.getContext(), R.raw.line_effect_3, 1);
        soundId2 = soundPool.load(mainGame.getContext(), R.raw.line_explode1, 1);
        soundId3 = soundPool.load(mainGame.getContext(), R.raw.line_explode2, 1);
        soundId4 = soundPool.load(mainGame.getContext(), R.raw.line_explode3, 1);

//        sound1 = MediaPlayer.create(mainGame.getContext(), R.raw.line_explode1);
//        sound2 = MediaPlayer.create(mainGame.getContext(), R.raw.line_explode2);
//        sound3 = MediaPlayer.create(mainGame.getContext(), R.raw.line_explode3);
    }

    public void update(int playerX, int playerY, MainGameScript mainGame){
        soundTimer += 1;
        //if (sound1.)
        timer -= 1;

        //Every one second, we spawn a line and reset the timer
        if (timer <= 0 && horizontalLines.size() + verticalLines.size() < 4){
            if (rnd.nextBoolean()){
                horizontalLines.add(new KillerHorizontalLine(rnd.nextBoolean(), rnd.nextBoolean(), rnd.nextFloat() * 12f,playerY, soundPool, soundId1));
            } else {
                verticalLines.add(new KillerVerticalLine(rnd.nextBoolean(), rnd.nextBoolean(), 10f,playerX, soundPool, soundId1));
            }
            //mediaPlayer.start();

            //Each time we reset the timer, we set it lower than before
            timer = 140 - timerReduction;
            timerReduction += 3;

            if (timerReduction > 80){
                timerReduction = 80;
            }
            Log.d(getClass().getSimpleName(), "Timer reduction: " + timerReduction);
        }

        //Update lines
        for (KillerHorizontalLine line:horizontalLines) {
            line.update();
            if (line.state == 1){
                if (mainGame.gameState == 2){
                    if (line.direction){
                        if (line.startingSide && playerY < line.rate * (Globals.GAME_WIDTH - playerX) + line.startY){
                            Log.d("Spawner.update()", "HORIZONTAL UP LINE - PlayerPosition= (" + playerX + ", " + playerY + ") - Rate: " + line.rate + "- StartY: " + line.startY);
                            mainGame.nextGameState = 3;
                            playDestroySound();
                        } else if (!line.startingSide && playerY < line.rate * playerX + line.startY){
                            Log.d("Spawner.update()", "HORIZONTAL UP LINE - PlayerPosition= (" + playerX + ", " + playerY + ") - Rate: " + line.rate + "- StartY: " + line.startY);
                            mainGame.nextGameState = 3;
                            playDestroySound();
                        }
                    } else {
                        if (line.startingSide && playerY > line.rate * (Globals.GAME_WIDTH - playerX) + line.startY) {
                            Log.d("Spawner.update()", "HORIZONTAL DOWN LINE - PlayerPosition= (" + playerX + ", " + playerY + ") - Rate: " + line.rate + "- StartY: " + line.startY);
                            mainGame.nextGameState = 3;
                            playDestroySound();
                        } else if (!line.startingSide && playerY > line.rate * playerX + line.startY) {
                            Log.d("Spawner.update()", "HORIZONTAL DOWN LINE - PlayerPosition= (" + playerX + ", " + playerY + ") - Rate: " + line.rate + "- StartY: " + line.startY);
                            mainGame.nextGameState = 3;
                            playDestroySound();
                        }
                    }
                }
            }
            else if (line.state == 4){

                if (mainGame.gameState == 2 && mainGame.nextGameState == 0){
                    mainGame.currentScore += 1;
                }
                horizontalLines.remove(line);
                //mediaPlayer.stop();
            }
        }
        for (KillerVerticalLine line: verticalLines){
            line.update();
            if (line.state == 1) {
                if (mainGame.gameState == 2){
                    if (line.direction) {
                        if (line.startingSide && playerX > line.rate * playerY + line.startX) {
                            Log.d("Spawner.update()", "VERTICAL RIGHT LINE - PlayerPosition= (" + playerX + ", " + playerY + ") - Rate: " + line.rate + "- StartX: " + line.startX);
                            mainGame.nextGameState = 3;
                            playDestroySound();
                        } else if (!line.startingSide && playerX > line.rate * (Globals.GAME_HEIGHT - playerY) + line.startX) {
                            Log.d("Spawner.update()", "VERTICAL RIGHT LINE - PlayerPosition= (" + playerX + ", " + playerY + ") - Rate: " + line.rate + "- StartX: " + line.startX);
                            mainGame.nextGameState = 3;
                            playDestroySound();
                        }
                    } else {
                        if (line.startingSide && playerX < line.rate * playerY + line.startX) {
                            Log.d("Spawner.update()", "VERTICAL LEFT LINE - PlayerPosition= (" + playerX + ", " + playerY + ") - Rate: " + line.rate + "- StartX: " + line.startX);
                            mainGame.nextGameState = 3;
                            playDestroySound();
                        } else if (!line.startingSide && playerX < line.rate * (Globals.GAME_HEIGHT - playerY) + line.startX) {
                            Log.d("Spawner.update()", "VERTICAL RIGHT LINE - PlayerPosition= (" + playerX + ", " + playerY + ") - Rate: " + line.rate + "- StartX: " + line.startX);
                            mainGame.nextGameState = 3;
                            playDestroySound();
                        }
                    }
                }
            } else if (line.state == 4){
                if (mainGame.gameState == 2 && mainGame.nextGameState == 0){
                    mainGame.currentScore += 1;
                }
                //mediaPlayer.stop();
                verticalLines.remove(line);
            }
        }
    }

    public void playDestroySound(){
        float playbackSpeed = 1.5f;
        soundPool.play(soundId1, 0.6f, 0.6f, 0, 0, playbackSpeed);

        if (soundTimer < 96){
            Log.d(getClass().getSimpleName(), "played destroy sound1");
            soundPool.play(soundId1, 1, 1, 0, 0, 1);
            //sound1.start();
        } else if (soundTimer < 168){
            Log.d(getClass().getSimpleName(), "played destroy sound2");
            soundPool.play(soundId1, 1, 1, 0, 0, 1);
            //sound2.start();

        } else if (soundTimer < 384){
            Log.d(getClass().getSimpleName(), "played destroy sound3");
            soundPool.play(soundId1, 1, 1, 0, 0, 1);
            //sound3.start();
            soundTimer = 0;
        }
    }

    public void draw(Canvas canvas){
        for (KillerHorizontalLine line: horizontalLines){
            line.draw(canvas);
        }
        for (KillerVerticalLine line: verticalLines){
            line.draw(canvas);
        }
    }

    public void reset(){
        horizontalLines.clear();
        verticalLines.clear();
        timerReduction = 0;
    }
}
