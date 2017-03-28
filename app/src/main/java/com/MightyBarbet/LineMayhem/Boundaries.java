package com.MightyBarbet.LineMayhem;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;

/**
 * Created by Bensas on 30/05/16. (may)
 */
public class Boundaries extends GameObject {
    Paint paint;
    Wall[] walls = new Wall[4];

    public Boundaries(){
        paint = new Paint();
        walls[0] = new Wall(0, 0, Globals.BOUNDARY_WIDTH, Globals.GAME_HEIGHT);
        walls[1] = new Wall((Globals.GAME_WIDTH - Globals.BOUNDARY_WIDTH), 0, Globals.BOUNDARY_WIDTH, Globals.GAME_HEIGHT);
        walls[2] = new Wall(0, 0, Globals.GAME_WIDTH, 20);
        walls[3] = new Wall(0, (Globals.GAME_HEIGHT - Globals.BOUNDARY_WIDTH), Globals.GAME_WIDTH, Globals.BOUNDARY_WIDTH);
    }

    public class Wall extends GameObject {

        public Wall(int x, int y, int width, int height){
            this.x = (short)x;
            this.y = (short)y;
            this.width = width;
            this.height = height;
        }


        //Darw method
        public void draw(Canvas canvas){
            //canvas.drawBitmap(image, x, y, null);
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.FILL);

            canvas.drawRect(x, y, x+width, y+height, paint);
        }
    }

    //Draw method
    public void draw(Canvas canvas){
        for (Wall wall: walls){
            wall.draw(canvas);
        }
    }
    
}
