package com.example.tberroa.girodicerapp.data;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class MissionStatus extends Application {

    private final String isMissionInProgress = "is_mission_in_progress";

    public MissionStatus() {
    }

    private SharedPreferences getSharedPreferences(Context context){
        return context.getSharedPreferences("mission_status", MODE_PRIVATE);
    }

    public boolean isMissionInProgress(Context context){
        return getSharedPreferences(context).getBoolean(isMissionInProgress, false);
    }

    public void setMissionStatus(Context context, boolean bool) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(isMissionInProgress, bool);
        editor.apply();
    }
}