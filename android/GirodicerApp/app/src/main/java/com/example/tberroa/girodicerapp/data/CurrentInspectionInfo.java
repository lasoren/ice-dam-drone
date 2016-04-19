package com.example.tberroa.girodicerapp.data;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class CurrentInspectionInfo extends Application {

    // keys
    private final String IS_IN_PROGRESS = "is_not_in_progress";
    private final String PHASE = "phase";
    private final String INSPECTION_ID = "inspection_id";   // id for the newly started inspection
    private final String AERIAL_COUNT = "aerial_count";
    private final String THERMAL_COUNT = "thermal_count";
    private final String ROOF_EDGE_COUNT = "roof_edge_count";

    public CurrentInspectionInfo() {
    }

    private SharedPreferences getSharedPreferences(Context context){
        return context.getSharedPreferences("active_inspection_info", MODE_PRIVATE);
    }

    public boolean isInProgress(Context context){
        return getSharedPreferences(context).getBoolean(IS_IN_PROGRESS, false);
    }

    public int getPhase(Context context){
        return getSharedPreferences(context).getInt(PHASE, 0);
    }

    public int getInspectionId(Context context){
        return getSharedPreferences(context).getInt(INSPECTION_ID, 0);
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

    public void setInProgress(Context context, boolean bool) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(IS_IN_PROGRESS, bool);
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

    @SuppressWarnings("unused")
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

    public void clearAll(Context context){
        setInProgress(context, false);
        setPhase(context, Params.CI_INACTIVE);
        setInspectionId(context, 0);
    }
}