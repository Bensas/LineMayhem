package mightybarbet.swift;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;

/**
 * Created by Bensas on 30/05/16. (may)
 */
public class Boundaries extends GameObject {
    ArrayList<Wall> walls = new ArrayList<>();

    public Boundaries(){
        walls.add(new Wall(0, 0, Globals.BOUNDARY_WIDTH, Globals.GAME_HEIGHT));
        walls.add(new Wall((Globals.GAME_WIDTH - Globals.BOUNDARY_WIDTH), 0, Globals.BOUNDARY_WIDTH, Globals.GAME_HEIGHT));
        walls.add(new Wall(0, 0, Globals.GAME_WIDTH, 20));
        walls.add(new Wall(0, (Globals.GAME_HEIGHT - Globals.BOUNDARY_WIDTH), Globals.GAME_WIDTH, Globals.BOUNDARY_WIDTH));
    }

    public class Wall extends GameObject {

        public Wall(int x, int y, int width, int height){
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }


        //Darw method
        public void draw(Canvas canvas){
            //canvas.drawBitmap(image, x, y, null);
            Paint paint = new Paint();
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
