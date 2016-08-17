package com.MightyBarbet.LineMayhem;

import android.graphics.Canvas;
import android.media.AudioManager;
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

    int timer = 100, timerReduction = 30; //0= horizontal line, 1= vertical line

    public void update(int playerX, int playerY, MainGameScript mainGame){
        timer -= 1;

        if (mainGame.gameState == 2){
            //Every one second, we spawn a line and reset the timer
            if (timer <= 0){
                if (rnd.nextBoolean()){
                    checkForCloseHorizontalETAs(createHorizontalLine(playerY));
                } else {
                    checkForCloseVerticalETAs(createVerticalLine(playerX));
                }

                //Each time we reset the timer, we set it lower than before
                timer = 110 - timerReduction;
                timerReduction += 3;

                //We limit the timer reduction (This is when the player reached max difficulty)
                if (timerReduction > 66){
                    //Log.d(getClass().getSimpleName(), "Max difficulty reached!");
                    if (verticalLines.size() == 4){
                        verticalLines.remove(createVerticalLine(playerX));
                    }
                    timerReduction = 66;
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
            line.update();
            if (line.state == 2){
                //playDestroySound();
                if (mainGame.gameState == 2){
                    if (line.direction){
                        if (line.startingSide && playerY < line.rate * (Globals.GAME_WIDTH - playerX) + line.startY){
                            Log.d("Spawner.update()", "HORIZONTAL UP LINE - PlayerPosition= (" + playerX + ", " + playerY + ") - Rate: " + line.rate + "- StartY: " + line.startY);
                            mainGame.nextGameState = 3;
                        } else if (!line.startingSide && playerY < line.rate * playerX + line.startY){
                            Log.d("Spawner.update()", "HORIZONTAL UP LINE - PlayerPosition= (" + playerX + ", " + playerY + ") - Rate: " + line.rate + "- StartY: " + line.startY);
                            mainGame.nextGameState = 3;
                        }
                    } else {
                        if (line.startingSide && playerY > line.rate * (Globals.GAME_WIDTH - playerX) + line.startY) {
                            Log.d("Spawner.update()", "HORIZONTAL DOWN LINE - PlayerPosition= (" + playerX + ", " + playerY + ") - Rate: " + line.rate + "- StartY: " + line.startY);
                            mainGame.nextGameState = 3;
                        } else if (!line.startingSide && playerY > line.rate * playerX + line.startY) {
                            Log.d("Spawner.update()", "HORIZONTAL DOWN LINE - PlayerPosition= (" + playerX + ", " + playerY + ") - Rate: " + line.rate + "- StartY: " + line.startY);
                            mainGame.nextGameState = 3;
                        }
                    }
                }
            }
            else if (line.state == 4){
                if (mainGame.gameState == 2 && mainGame.nextGameState == 0){
                    mainGame.currentScore += 1;
                }
                line.state = 0;
                line.rate = 0;
            }
        }
        for (KillerVerticalLine line: verticalLines){
            line.update();
            if (line.state == 2) {
                //playDestroySound();
                if (mainGame.gameState == 2){
                    if (line.direction) {
                        if (line.startingSide && playerX > line.rate * playerY + line.startX) {
                            Log.d("Spawner.update()", "VERTICAL RIGHT LINE - PlayerPosition= (" + playerX + ", " + playerY + ") - Rate: " + line.rate + "- StartX: " + line.startX);
                            mainGame.nextGameState = 3;
                        } else if (!line.startingSide && playerX > line.rate * (Globals.GAME_HEIGHT - playerY) + line.startX) {
                            Log.d("Spawner.update()", "VERTICAL RIGHT LINE - PlayerPosition= (" + playerX + ", " + playerY + ") - Rate: " + line.rate + "- StartX: " + line.startX);
                            mainGame.nextGameState = 3;
                        }
                    } else {
                        if (line.startingSide && playerX < line.rate * playerY + line.startX) {
                            Log.d("Spawner.update()", "VERTICAL LEFT LINE - PlayerPosition= (" + playerX + ", " + playerY + ") - Rate: " + line.rate + "- StartX: " + line.startX);
                            mainGame.nextGameState = 3;
                        } else if (!line.startingSide && playerX < line.rate * (Globals.GAME_HEIGHT - playerY) + line.startX) {
                            Log.d("Spawner.update()", "VERTICAL RIGHT LINE - PlayerPosition= (" + playerX + ", " + playerY + ") - Rate: " + line.rate + "- StartX: " + line.startX);
                            mainGame.nextGameState = 3;
                        }
                    }
                }
            } else if (line.state == 4){
                if (mainGame.gameState == 2 && mainGame.nextGameState == 0){
                    mainGame.currentScore += 1;
                }
                line.state = 0;
                line.rate = 0;
            }
        }
    }

    public KillerHorizontalLine createHorizontalLine(int playerY){
        for (KillerHorizontalLine line: horizontalLines){
            if (line.state == 0){
                line.resetLine(playerY, rnd);
                line.state = 1;
                return  line;
            }
        }
        //Log.d("CreateHorizontalLine", "No inactive(available) lines found. :(");
        return  null;
    }

    public KillerVerticalLine createVerticalLine(int playerX){
        for (KillerVerticalLine line: verticalLines){
            if (line.state == 0){
                line.resetLine(playerX, rnd);
                line.state = 1;
                return line;
            }
        }
        //Log.d("CreateVerticalLine", "No inactive(available) lines found. :(");
        return null;
    }


    //We use this method to avoid two opposite-direction lines exploding (roughly) at the same time in a way that
    //makes it impossible for the player to survive.
    public void checkForCloseHorizontalETAs(KillerHorizontalLine line){
        for (KillerHorizontalLine horizontalLine: horizontalLines){
            if (horizontalLine != line && horizontalLine.direction != line.direction && Math.abs(horizontalLine.ETA - line.ETA) < (long)600000000){
                line.state = 0;
                line.rate = 0;
                //Log.d(getClass().getSimpleName(), "CLOSE ETAs DETECTED FOR TWO HORIZONTAL LINES!" + horizontalLine.ETA + " - " + line.ETA + " - Abs value: " + Math.abs(horizontalLine.ETA - line.ETA));
            }
        }

    }

    public void checkForCloseVerticalETAs(KillerVerticalLine line){
        for (KillerVerticalLine verticalLine: verticalLines){
            if (verticalLine != line && verticalLine.direction != line.direction && Math.abs(verticalLine.ETA - line.ETA) < (long)600000000){
                line.state = 0;
                line.rate = 0;
                //Log.d(getClass().getSimpleName(), "CLOSE ETAs DETECTED FOR TWO VERTICAL LINES!" + verticalLine.ETA + " - " + line.ETA + " - Abs value: " + Math.abs(verticalLine.ETA - line.ETA));
            }
        }
    }


    public void draw(Canvas canvas){
        //int counter = 0;
        for (KillerHorizontalLine line: horizontalLines){
            line.draw(canvas);
            //Log.d("Spawner.draw()", "Drew line number " + counter + ". Rate: " + line.rate + ". State: " + line.state);
            //counter++;
        }
        for (KillerVerticalLine line: verticalLines){
            line.draw(canvas);
            //Log.d("Spawner.draw()", "Drew line number " + counter + ". Rate: " + line.rate + ". State: " + line.state);
            //counter++;
        }
    }

    public void reset(){
        verticalLines.clear();
        horizontalLines.clear();
        for (int i = 0; i < 4; i++){
            verticalLines.add(new KillerVerticalLine());
            horizontalLines.add(new KillerHorizontalLine());
        }
        timerReduction = 0;
    }
}
