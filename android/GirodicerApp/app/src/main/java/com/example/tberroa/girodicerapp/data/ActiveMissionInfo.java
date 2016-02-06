package com.example.tberroa.girodicerapp.data;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class ActiveMissionInfo extends Application {

    // keys
    private final String IS_MISSION_IN_PROGRESS = "is_mission_in_progress";
    private final String MISSION_PHASE = "mission_phase";
    private final String MISSION_NUMBER = "mission_number";
    private final String MISSION_DATA = "mission_data";
    private final String COMPLETED_DOWNLOADS = "completed_downloads";

    public ActiveMissionInfo() {
    }

    private SharedPreferences getSharedPreferences(Context context){
        return context.getSharedPreferences("active_mission_info", MODE_PRIVATE);
    }

    public boolean missionNotInProgress(Context context){
        return getSharedPreferences(context).getBoolean(IS_MISSION_IN_PROGRESS, true);
    }

    public int getMissionPhase(Context context){
        return getSharedPreferences(context).getInt(MISSION_PHASE, 0);
    }

    public int getMissionNumber(Context context){
        return getSharedPreferences(context).getInt(MISSION_NUMBER, 0);
    }

    public String getMissionData(Context context){ // as JSON of a Mission object
        return getSharedPreferences(context).getString(MISSION_DATA, "");
    }

    public int getCompletedDownloads(Context context){
        return getSharedPreferences(context).getInt(COMPLETED_DOWNLOADS, 0);
    }

    public void setMissionNotInProgress(Context context, boolean bool) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(IS_MISSION_IN_PROGRESS, bool);
        editor.apply();
    }

    public void setMissionPhase(Context context, int missionPhase){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(this.MISSION_PHASE, missionPhase);
        editor.apply();
    }

    public void setMissionNumber(Context context, int missionNumber){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(this.MISSION_NUMBER, missionNumber);
        editor.apply();
    }

    public void setMissionData(Context context, String missionData){ // as JSON of a Mission object
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(this.MISSION_DATA, missionData);
        editor.apply();
    }

    public void setCompletedDownloads(Context context, int count){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(COMPLETED_DOWNLOADS, count);
        editor.apply();
    }
}