package com.example.tberroa.girodicerapp.data;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class Provisions extends Application {

    // keys
    private final String GET_CLIENTS = "get_clients";
    private final String GET_INSPECTIONS = "get_inspections";
    private final String GET_INSPECTION_IMAGES = "get_inspection_images";

    public Provisions() {
    }

    private SharedPreferences getSharedPreferences(Context context){
        return context.getSharedPreferences("provisions", MODE_PRIVATE);
    }

    public int getClients(Context context){
        return getSharedPreferences(context).getInt(GET_CLIENTS, 0);
    }

    public int getInspections(Context context){
        return getSharedPreferences(context).getInt(GET_INSPECTIONS, 0);
    }

    public int getInspectionImages(Context context){
        return getSharedPreferences(context).getInt(GET_INSPECTION_IMAGES, 0);
    }


    public void setClients(Context context, int provision) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(GET_CLIENTS, provision);
        editor.apply();
    }

    public void setInspections(Context context, int provision) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(GET_INSPECTIONS, provision);
        editor.apply();
    }

    public void setInspectionImages(Context context, int provision) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(GET_INSPECTION_IMAGES, provision);
        editor.apply();
    }

    public void clear(Context context){
        setClients(context, 0);
        setInspections(context, 0);
        setInspectionImages(context, 0);
    }
}
