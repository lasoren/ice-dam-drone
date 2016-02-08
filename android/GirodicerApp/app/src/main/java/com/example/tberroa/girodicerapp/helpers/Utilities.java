package com.example.tberroa.girodicerapp.helpers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.activities.ActiveMissionActivity;
import com.example.tberroa.girodicerapp.data.ActiveMissionInfo;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.data.PreviousMissionsInfo;
import com.example.tberroa.girodicerapp.data.Mission;
import com.example.tberroa.girodicerapp.dialogs.MessageDialog;
import com.example.tberroa.girodicerapp.services.DroneService;
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

    public static void startImageUploadService(Context context){
        context.startService(new Intent(context, ImageUploadIntentService.class));
    }

    public static ArrayList<Mission> getMissions(Context context){
        String jsonMissions = new PreviousMissionsInfo().getMissions(context);
        return new Gson().fromJson(jsonMissions, new TypeToken<ArrayList<Mission>>(){}.getType());
    }

    public static String ConstructImageURL(String username, int missionNumber, String imageName){
        return Params.CLOUD_URL +username+"/mission"+missionNumber+"/images/"+imageName;
    }

    public static String ConstructImageKey(String username, int missionNumber, String imageName){
        return username+"/mission"+missionNumber+"/images/"+imageName;
    }

    public static String validate(Bundle enteredInfo){
        String validation = "";

        // grab entered information
        String username = enteredInfo.getString("username", null);
        String password = enteredInfo.getString("password", null);
        String confirmPassword = enteredInfo.getString("confirm_password", null);
        String email = enteredInfo.getString("email", null);

        if (username != null){
            boolean tooShort = username.length() < 3;
            boolean tooLong = username.length() > 15;
            if (!username.matches("[a-zA-Z0-9]+") || tooShort || tooLong ) {
                validation = validation.concat("username");
            }
        }

        if (password != null){
            if (password.length() < 6 || password.length() > 20) {
                validation = validation.concat("password");
            }
        }

        if (confirmPassword != null){
            if (!confirmPassword.equals(password)) {
                validation = validation.concat("confirm_password");
            }
        }

        if (email != null){
            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                validation = validation.concat("email");
            }
        }
        return validation;
    }

    public static void AttemptMissionStart(Context context){

        PreviousMissionsInfo pMInfo = new PreviousMissionsInfo();
        String message;
        if (!pMInfo.isUpToDate(context) || pMInfo.isFetching(context)){
            message = context.getResources().getString(R.string.previous_missions_not_up_to_date);
            new MessageDialog(context, message);
        }
        else if (!new ActiveMissionInfo().missionNotInProgress(context)) {
            message = context.getResources().getString(R.string.mission_in_progress);
            new MessageDialog(context, message).getDialog().show();
        }
        else{
            context.startService(new Intent(context, DroneService.class));
            context.startActivity(new Intent(context, ActiveMissionActivity.class));
        }
    }

}
