package com.example.tberroa.girodicerapp.helpers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

import com.example.tberroa.girodicerapp.data.PreviousMissionsInfo;
import com.example.tberroa.girodicerapp.data.Mission;
import com.example.tberroa.girodicerapp.services.AmazonS3IntentService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

final public class Utilities {

    private Utilities(){
    }

    private static Point getScreenDimensions(Context context){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point screenDimensions = new Point();
        display.getSize(screenDimensions);
        return screenDimensions;
    }

    public static int getScreenWidth(Context context){
        return getScreenDimensions(context).x;
    }

    public static int getScreenHeight(Context context){
        return getScreenDimensions(context).y;
    }

    public static void fetchPreviousMissionsData(Context context, String username){
        Intent intent = new Intent(context, AmazonS3IntentService.class);
        intent.putExtra("username", username);
        context.startService(intent);
    }

    public static ArrayList<Mission> getMissions(Context context){
        return new Gson().fromJson(new PreviousMissionsInfo().getMissions(context),
                new TypeToken<ArrayList<Mission>>(){}.getType());
    }
}
