package com.example.tberroa.girodicerapp.data;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class PreviousMissionsInfo extends Application {
    
    private final String isFetching = "is_fetching";
    private final String isUpToDate = "is_up_to_date";
    private final String missions = "missions";
    private final String numberOfMissions = "number_of_missions";

    public PreviousMissionsInfo(){
    }

    private SharedPreferences getSharedPreferences(Context context){
        return context.getSharedPreferences("previous_missions_info", MODE_PRIVATE);
    }

    public Boolean isFetching(Context context) {
        return getSharedPreferences(context).getBoolean(isFetching, false);
    }

    public Boolean isUpToDate(Context context) {
        return getSharedPreferences(context).getBoolean(isUpToDate, false);
    }

    // individual missions are saved together an array of missions and packaged as a JSON string
    public String getMissions(Context context) {
        return getSharedPreferences(context).getString(missions, "");
    }

    public int getNumOfMissions(Context context) {
        return getSharedPreferences(context).getInt(numberOfMissions, 0);
    }

    public void setFetching(Context context, Boolean bool){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(isFetching, bool);
        editor.apply();
    }

    public void setUpToDate(Context context, Boolean bool){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(isUpToDate, bool);
        editor.apply();
    }

    public void setMissions(Context context, String missions){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(this.missions, missions);
        editor.apply();
    }

    public void setNumOfMissions(Context context, int num){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(numberOfMissions, num);
        editor.apply();
    }
}
