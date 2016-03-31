package com.example.tberroa.girodicerapp.data;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class CurrentInspectionInfo extends Application {

    // keys
    private final String IS_NOT_IN_PROGRESS = "is_not_in_progress";
    private final String PHASE = "phase";
    private final String INSPECTION_ID = "inspection_id";   // id for the newly started inspection
    private final String CLIENT_ID = "client_id";           // id for the client this inspection belongs to
    private final String AERIAL_COUNT = "aerial_count";
    private final String THERMAL_COUNT = "thermal_count";
    private final String ROOF_EDGE_COUNT = "roof_edge_count";
    private final String LAST_DOWNLOAD = "last_download";

    public CurrentInspectionInfo() {
    }

    private SharedPreferences getSharedPreferences(Context context){
        return context.getSharedPreferences("active_inspection_info", MODE_PRIVATE);
    }

    public boolean isNotInProgress(Context context){
        return getSharedPreferences(context).getBoolean(IS_NOT_IN_PROGRESS, true);
    }

    public int getPhase(Context context){
        return getSharedPreferences(context).getInt(PHASE, 0);
    }

    public int getInspectionId(Context context){
        return getSharedPreferences(context).getInt(INSPECTION_ID, 0);
    }

    public int getClientId(Context context){
        return getSharedPreferences(context).getInt(CLIENT_ID, 0);
    }

    public int getAerialCount(Context context){
        return getSharedPreferences(context).getInt(AERIAL_COUNT, 0);
    }

    public int getThermalCount(Context context){
        return getSharedPreferences(context).getInt(THERMAL_COUNT, 0);
    }

    public int getRoofEdgeCount(Context context){
        return getSharedPreferences(context).getInt(ROOF_EDGE_COUNT, 0);
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
        editor.putInt(PHASE, phase);
        editor.apply();
    }

    public void setInspectionId(Context context, int id){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(INSPECTION_ID, id);
        editor.apply();
    }

    public void setClientId(Context context, int id){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(CLIENT_ID, id);
        editor.apply();
    }

    public void setAerialCount(Context context, int num){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(AERIAL_COUNT, num);
        editor.apply();
    }

    public void setThermalCount(Context context, int num){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(THERMAL_COUNT, num);
        editor.apply();
    }

    public void setRoofEdgeCount(Context context, int num){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(ROOF_EDGE_COUNT, num);
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
        setInspectionId(context, 0);
        setLastDownload(context, 0);
    }
}