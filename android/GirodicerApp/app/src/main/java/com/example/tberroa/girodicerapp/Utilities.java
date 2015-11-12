package com.example.tberroa.girodicerapp;


import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

public class Utilities {
    private Context context;

    public Utilities(){
    }

    public int getScreenWidth(Context context){
        this.context = context;
        Point screenDimensions = getScreenDimensions();
        int width = screenDimensions.x;
        return width;
    }

    public int getScreenHeight(Context context){
        this.context = context;
        Point screenDimensions = getScreenDimensions();
        int height = screenDimensions.y;
        return height;
    }

    private Point getScreenDimensions(){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point screenDimensions = new Point();
        display.getSize(screenDimensions);
        return screenDimensions;
    }
}
