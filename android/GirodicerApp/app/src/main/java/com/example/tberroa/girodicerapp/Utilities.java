package com.example.tberroa.girodicerapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

final public class Utilities {

    private Utilities(){
    }

    public static int getScreenWidth(Context context){
        return getScreenDimensions(context).x;
    }

    public static int getScreenHeight(Context context){
        return getScreenDimensions(context).y;
    }

    private static Point getScreenDimensions(Context context){
        WindowManager wm = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point screenDimensions = new Point();
        display.getSize(screenDimensions);
        return screenDimensions;
    }

    public static void fetchMetaData(Context context, String username){
        Intent intent = new Intent(context, AmazonS3IntentService.class);
        intent.putExtra("username", username);
        context.startService(intent);
    }

    public static ProgressDialog progressDialog(Context context, String title){
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(title);
        return progressDialog;
    }

    public static ArrayList<Mission> getMissions(Context appContext){
        BucketInfo bucketInfo = new BucketInfo();
        String jsonString = bucketInfo.getMissions(appContext);
        Gson gson = new Gson();
        Type listOfMissions = new TypeToken<ArrayList<Mission>>(){}.getType();
        return gson.fromJson(jsonString, listOfMissions);
    }
}
