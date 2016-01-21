package com.example.tberroa.girodicerapp;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class UserInfo extends Application {

    public UserInfo() {
    }

    public void setUserStatus(Context context, Boolean bool) {
        SharedPreferences prefs = context.getSharedPreferences("userInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("userLoggedOn", bool);
        editor.apply();
    }

    public void setUsername(Context context, String username) {
        SharedPreferences prefs = context.getSharedPreferences("userInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("username", username);
        editor.apply();
    }

    public Boolean getUserStatus(Context context){
        SharedPreferences prefs = context.getSharedPreferences("userInfo", MODE_PRIVATE);
        return prefs.getBoolean("userLoggedOn", false);
    }

    public String getUsername(Context context){
        SharedPreferences prefs = context.getSharedPreferences("userInfo", MODE_PRIVATE);
        return prefs.getString("username", "");
    }
}