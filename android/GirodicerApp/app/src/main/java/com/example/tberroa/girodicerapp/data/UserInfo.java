package com.example.tberroa.girodicerapp.data;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class UserInfo extends Application {

    private final String isLoggedIn = "is_logged_in";
    private final String username = "username";

    public UserInfo() {
    }

    private SharedPreferences getSharedPreferences(Context context){
        return context.getSharedPreferences("user_info", MODE_PRIVATE);
    }

    public Boolean isLoggedIn(Context context){
        return getSharedPreferences(context).getBoolean(isLoggedIn, false);
    }

    public String getUsername(Context context){
        return getSharedPreferences(context).getString(username, "");
    }

    public void setUserStatus(Context context, Boolean bool) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(isLoggedIn, bool);
        editor.apply();
    }

    public void setUsername(Context context, String username) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(this.username, username);
        editor.apply();
    }
}