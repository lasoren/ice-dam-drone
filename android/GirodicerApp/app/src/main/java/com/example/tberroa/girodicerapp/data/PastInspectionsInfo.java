package com.example.tberroa.girodicerapp.data;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class PastInspectionsInfo extends Application {

    // keys
    private final String IS_FETCHING = "is_fetching";
    private final String IS_UP_TO_DATE = "is_up_to_date";
    private final String MISSIONS = "missions";
    private final String NUMBER_OF_MISSIONS = "number_of_missions";

    public PastInspectionsInfo(){
    }

    private SharedPreferences getSharedPreferences(Context context){
        return context.getSharedPreferences("previous_missions_info", MODE_PRIVATE);
    }

    public Boolean isFetching(Context context) {
        return getSharedPreferences(context).getBoolean(IS_FETCHING, false);
    }

    public Boolean isUpToDate(Context context) {
        return getSharedPreferences(context).getBoolean(IS_UP_TO_DATE, false);
    }

    // individual MISSIONS are saved together an array of MISSIONS and packaged as a JSON string
    public String getMissions(Context context) {
        return getSharedPreferences(context).getString(MISSIONS, "");
    }

    public int getNumOfMissions(Context context) {
        return getSharedPreferences(context).getInt(NUMBER_OF_MISSIONS, 0);
    }

    public void setFetching(Context context, Boolean bool){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(IS_FETCHING, bool);
        editor.apply();
    }

    public void setUpToDate(Context context, Boolean bool){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(IS_UP_TO_DATE, bool);
        editor.apply();
    }

    public void setMissions(Context context, String missions){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(MISSIONS, missions);
        editor.apply();
    }

    public void setNumOfMissions(Context context, int num){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(NUMBER_OF_MISSIONS, num);
        editor.apply();
    }

    public void clearAll(Context context){
        setFetching(context, false);
        setUpToDate(context, false);
        setMissions(context, "");
        setNumOfMissions(context, 0);
    }
}
