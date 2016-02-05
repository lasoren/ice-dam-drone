package com.example.tberroa.girodicerapp.data;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

public class MissionStatus extends Application {

    private final String isMissionInProgress = "is_mission_in_progress";
    // 0=inactive, 1=active, 2=transfer, 3=upload
    private final String missionPhase = "mission_phase";
    private final String username = "username";
    private final String missionNumber = "mission_number";
    private final String numberOfAerials = "number_of_aerials";
    private final String numberOfThermals = "number_of_thermals";
    private final String numberOfIceDams = "number_of_icedams";
    private final String numberOfSalts = "number_of_salts";

    public MissionStatus() {
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

    public String getUsername(Context context){
        return getSharedPreferences(context).getString(username, "");
    }

    public int getMissionNumber(Context context){
        return getSharedPreferences(context).getInt(missionNumber, 0);
    }

    public int getNumberOfAerials(Context context){
        return getSharedPreferences(context).getInt(numberOfAerials, 0);
    }

    public int getNumberOfThermals(Context context){
        return getSharedPreferences(context).getInt(numberOfThermals, 0);
    }

    public int getNumberOfIceDams(Context context){
        return getSharedPreferences(context).getInt(numberOfIceDams, 0);
    }

    public int getNumberOfSalts(Context context){
        return getSharedPreferences(context).getInt(numberOfSalts, 0);
    }

    public void setMissionNotInProgress(Context context, boolean bool) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(isMissionInProgress, bool);
        editor.apply();
    }

    public void setMissionPhase(Context context, int num){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(missionPhase, num);
        editor.apply();
    }

    public void setUsername(Context context, String user){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(username, user);
        editor.apply();
    }

    public void setMissionNumber(Context context, int missionNum){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(missionNumber, missionNum);
        editor.apply();
    }

    public void setNumberOfAerials(Context context, int numOfAerials){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(numberOfAerials, numOfAerials);
        editor.apply();
    }

    public void setNumberOfThermals(Context context, int numOfThermals){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(numberOfThermals, numOfThermals);
        editor.apply();
    }

    public void setNumberOfIceDams(Context context, int numOfIceDams){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(numberOfIceDams, numOfIceDams);
        editor.apply();
    }

    public void setNumberOfSalts(Context context, int numOfSalts){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(numberOfSalts, numOfSalts);
        editor.apply();
    }
}