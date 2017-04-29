package com.MightyBarbet.LineMayhem;

//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Canvas;
//import android.graphics.Paint;
//import android.util.Log;
//
///**
// * Created by Bensas on 3/4/17.
// */
//public class NotificationsButton {
//    Bitmap bitmap;
//    Paint paint;
//    boolean isVisible = false;
//    int x, y;
//    //States are 0: invisible, 1: appearing, 2: visible
//    int state;
//    public NotificationsButton(Context context){
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inScaled = false;
//        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.notifications_icon_smaller, options);
//        this.x = Globals.GAME_WIDTH - Globals.BOUNDARY_WIDTH - bitmap.getWidth()/2 - 15;
//        this.y = Globals.GAME_HEIGHT - Globals.BOUNDARY_WIDTH - bitmap.getHeight()/2 - 15;
//        Log.d(getClass().getSimpleName(), bitmap.getWidth() + " - " + bitmap.getHeight());
//        paint = new Paint();
//    }
//
//    public void draw(Canvas canvas){
//        canvas.drawBitmap(bitmap, x - bitmap.getWidth()/2, y - bitmap.getHeight()/2, paint);
//    }
//}
