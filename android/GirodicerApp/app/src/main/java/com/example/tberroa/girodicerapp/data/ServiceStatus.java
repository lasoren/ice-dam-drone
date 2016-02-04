package com.example.tberroa.girodicerapp.data;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class ServiceStatus extends Application {

    private final String isServiceRunning = "is_service_running";

    public ServiceStatus() {
    }

    private SharedPreferences getSharedPreferences(Context context){
        return context.getSharedPreferences("service_status", MODE_PRIVATE);
    }

    public boolean isServiceRunning(Context context){
        return getSharedPreferences(context).getBoolean(isServiceRunning, false);
    }

    public void setServiceStatus(Context context, boolean bool) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(isServiceRunning, bool);
        editor.apply();
    }
}