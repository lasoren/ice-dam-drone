package com.example.tberroa.girodicerapp.data;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class ActiveMissionStatus extends Application {

    private final String isMissionInProgress = "is_mission_in_progress";

    // phases: 0=inactive, 1=active, 2=transfer, 3=upload, 5=waiting for downloads to complete
    private final String missionPhase = "mission_phase";
    private final String missionNumber = "mission_number";
    private final String missionData = "mission_data";
    private final String completedDownloads = "completed_downloads";

    public ActiveMissionStatus() {
    }

    private SharedPreferences getSharedPreferences(Context context){
        return context.getSharedPreferences("mission_status", MODE_PRIVATE);
    }

    public boolean missionNotInProgress(Context context){
        return getSharedPreferences(context).getBoolean(isMissionInProgress, true);
    }

    public int getMissionPhase(Context context){
        return getSharedPreferences(context).getInt(missionPhase, 0);
    }

    public int getMissionNumber(Context context){
        return getSharedPreferences(context).getInt(missionNumber, 0);
    }

    public String getMissionData(Context context){ // as JSON of a Mission object
        return getSharedPreferences(context).getString(missionData, "");
    }

    public int getCompletedDownloads(Context context){
        return getSharedPreferences(context).getInt(completedDownloads, 0);
    }

    public void setMissionNotInProgress(Context context, boolean bool) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(isMissionInProgress, bool);
        editor.apply();
    }

    public void setMissionPhase(Context context, int missionPhase){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(this.missionPhase, missionPhase);
        editor.apply();
    }

    public void setMissionNumber(Context context, int missionNumber){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(this.missionNumber, missionNumber);
        editor.apply();
    }

    public void setMissionData(Context context, String missionData){ // as JSON of a Mission object
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(this.missionData, missionData);
        editor.apply();
    }

    public void setCompletedDownloads(Context context, int count){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(completedDownloads, count);
        editor.apply();
    }
}