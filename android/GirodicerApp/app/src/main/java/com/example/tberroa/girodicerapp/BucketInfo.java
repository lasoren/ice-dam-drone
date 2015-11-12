package com.example.tberroa.girodicerapp;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;


public class BucketInfo extends Application {

    // constructor
    public BucketInfo(Context context){
        SharedPreferences prefs = context.getSharedPreferences("bucket_info", MODE_PRIVATE);
    }

    public Boolean getFetching(Context context){
        SharedPreferences prefs = context.getSharedPreferences("bucket_info", MODE_PRIVATE);
        Boolean fetching = prefs.getBoolean("fetching", false);
        return fetching;
    }

    public String getMissions(Context context){
        SharedPreferences prefs = context.getSharedPreferences("bucket_info", MODE_PRIVATE);
        String missions = prefs.getString("missions", "");
        return missions;
    }

    public int getNumOfMissions(Context context){
        SharedPreferences prefs = context.getSharedPreferences("bucket_info", MODE_PRIVATE);
        int num = prefs.getInt("num_of_missions", 0);
        return num;
    }

    public void setFetching(Context context, Boolean bool){
        SharedPreferences prefs = context.getSharedPreferences("bucket_info", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("fetching", bool);
        editor.apply();
    }

    public void setMissions(Context context, String missions){
        SharedPreferences prefs = context.getSharedPreferences("bucket_info", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("missions", missions);
        editor.apply();
    }

    public void setNumOfMissions(Context context, int num){
        SharedPreferences prefs = context.getSharedPreferences("bucket_info", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("num_of_missions", num);
        editor.apply();
    }
}
