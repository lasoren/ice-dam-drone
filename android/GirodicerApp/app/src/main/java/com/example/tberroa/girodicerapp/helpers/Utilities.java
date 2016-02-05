package com.example.tberroa.girodicerapp.helpers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;

import com.example.tberroa.girodicerapp.data.PreviousMissionsInfo;
import com.example.tberroa.girodicerapp.data.Mission;
import com.example.tberroa.girodicerapp.data.UserInfo;
import com.example.tberroa.girodicerapp.services.ActiveMissionService;
import com.example.tberroa.girodicerapp.services.FetchPreviousMissionsIntentService;
import com.example.tberroa.girodicerapp.services.ImageTransferIntentService;
import com.example.tberroa.girodicerapp.services.ImageUploadIntentService;
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

    private static boolean isLandscape(Context context){
        boolean bool = false;
        if (getScreenWidth(context) > getScreenHeight(context)){
            bool = true;
        }
        return bool;
    }

    private static int getScreenWidth(Context context){
        return getScreenDimensions(context).x;
    }

    private static int getScreenHeight(Context context){
        return getScreenDimensions(context).y;
    }

    public static int getImageWidthGrid(Context context){
        return getScreenWidth(context)/ getSpanGrid(context);
    }

    public static int getImageHeightGrid(Context context){
        int imageHeight;
        int screenHeight = getScreenHeight(context);
        int span = getSpanGrid(context);
        if (isLandscape(context)){
            imageHeight = screenHeight/(span/2);
        }
        else{
            imageHeight = screenHeight/(span*2);
        }
        return imageHeight;
    }

    public static int getSpanGrid(Context context){
        int span;
        if (isLandscape(context)){
            span = 4;
        }
        else{
            span = 2;
        }
        return span;
    }

    public static int getSpacingGrid(Context context){
        return getScreenWidth(context)/(getSpanGrid(context)*12);
    }

    public static void startActiveMission(Context context){
        context.startService(new Intent(context, ActiveMissionService.class));
    }

    public static void startImageTransfer(Context context){
        String username = new UserInfo().getUsername(context);
        int missionNumber = (new PreviousMissionsInfo().getNumOfMissions(context))+1;
        context.startService(new Intent(context, ImageTransferIntentService.class)
                .putExtra("username", username)
                .putExtra("mission_number", missionNumber));
    }

    public static void startImageUpload(Context context){
        context.startService(new Intent(context, ImageUploadIntentService.class));
    }

    public static void fetchPreviousMissionsData(Context context, String username){
        Intent intent = new Intent(context, FetchPreviousMissionsIntentService.class);
        intent.putExtra("username", username);
        context.startService(intent);
    }

    public static ArrayList<Mission> getMissions(Context context){
        return new Gson().fromJson(new PreviousMissionsInfo().getMissions(context),
                new TypeToken<ArrayList<Mission>>(){}.getType());
    }
}
