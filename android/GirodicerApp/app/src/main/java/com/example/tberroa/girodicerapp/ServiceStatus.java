package com.example.tberroa.girodicerapp;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class ServiceStatus extends Application {
    private Boolean SERVICE_RUNNING;

    public ServiceStatus(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("service_status", MODE_PRIVATE);
        SERVICE_RUNNING = prefs.getBoolean("is_service_running", false);
    }

    public void setServiceStatusTrue(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("service_status", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("is_service_running", true);
        editor.apply();
    }

    public void setServiceStatusFalse(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("service_status", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("is_service_running", false);
        editor.apply();
    }
    public Boolean isServiceRunning() {
        return SERVICE_RUNNING;
    }
}