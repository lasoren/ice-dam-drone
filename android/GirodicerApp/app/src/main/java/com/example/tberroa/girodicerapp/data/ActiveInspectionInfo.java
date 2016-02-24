package com.example.tberroa.girodicerapp.data;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class ActiveInspectionInfo extends Application {

    // keys
    private final String IS_NOT_IN_PROGRESS = "is_not_in_progress";
    private final String PHASE = "phase";
    private final String INSPECTION_NUMBER = "inspection_number";
    private final String DATA = "data";
    private final String LAST_DOWNLOAD = "last_download";

    public ActiveInspectionInfo() {
    }

    private SharedPreferences getSharedPreferences(Context context){
        return context.getSharedPreferences("active_inspection_info", MODE_PRIVATE);
    }

    public boolean isNotInProgress(Context context){
        return getSharedPreferences(context).getBoolean(IS_NOT_IN_PROGRESS, true);
    }

    public int getMissionPhase(Context context){
        return getSharedPreferences(context).getInt(PHASE, 0);
    }

    public int getMissionNumber(Context context){
        return getSharedPreferences(context).getInt(INSPECTION_NUMBER, 0);
    }

    public String getMissionData(Context context){ // as JSON of a Mission object
        return getSharedPreferences(context).getString(DATA, "");
    }

    public long getLastDownload(Context context){
        return getSharedPreferences(context).getLong(LAST_DOWNLOAD, 0);
    }

    public void setNotInProgress(Context context, boolean bool) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(IS_NOT_IN_PROGRESS, bool);
        editor.apply();
    }

    public void setPhase(Context context, int phase){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(this.PHASE, phase);
        editor.apply();
    }

    public void setInspectionNumber(Context context, int inspectionNumber){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(this.INSPECTION_NUMBER, inspectionNumber);
        editor.apply();
    }

    public void setData(Context context, String data){ // as JSON of a Mission object
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(this.DATA, data);
        editor.apply();
    }

    public void setLastDownload(Context context, long lastDownload){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putLong(LAST_DOWNLOAD, lastDownload);
        editor.apply();
    }

    public void clearAll(Context context){
        setNotInProgress(context, true);
        setPhase(context, 0);
        setInspectionNumber(context, 0);
        setData(context, "");
        setLastDownload(context, 0);
    }
}