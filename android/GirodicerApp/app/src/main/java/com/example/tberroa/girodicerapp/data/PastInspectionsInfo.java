package com.example.tberroa.girodicerapp.data;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class PastInspectionsInfo extends Application {

    // keys
    private final String IS_UP_TO_DATE = "is_up_to_date";
    private final String IS_UPDATING = "is_updating";

    public PastInspectionsInfo(){
    }

    private SharedPreferences getSharedPreferences(Context context){
        return context.getSharedPreferences("previous_missions_info", MODE_PRIVATE);
    }

    public Boolean isUpToDate(Context context) {
        return getSharedPreferences(context).getBoolean(IS_UP_TO_DATE, false);
    }

    public Boolean isUpdating(Context context) {
        return getSharedPreferences(context).getBoolean(IS_UPDATING, false);
    }

    public void setUpToDate(Context context, Boolean bool){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(IS_UP_TO_DATE, bool);
        editor.apply();
    }

    public void setIsUpdating(Context context, Boolean bool){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(IS_UPDATING, bool);
        editor.apply();
    }

    public void clearAll(Context context){
        setUpToDate(context, false);
        setIsUpdating(context, false);
    }
}
