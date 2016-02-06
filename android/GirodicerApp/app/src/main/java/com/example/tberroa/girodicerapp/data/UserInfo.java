package com.example.tberroa.girodicerapp.data;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class UserInfo extends Application {

    // keys
    private final String IS_LOGGED_IN = "is_logged_in";
    private final String USERNAME = "USERNAME";

    public UserInfo() {
    }

    private SharedPreferences getSharedPreferences(Context context){
        return context.getSharedPreferences("user_info", MODE_PRIVATE);
    }

    public Boolean isLoggedIn(Context context){
        return getSharedPreferences(context).getBoolean(IS_LOGGED_IN, false);
    }

    public String getUsername(Context context){
        return getSharedPreferences(context).getString(USERNAME, "");
    }

    public void setUserStatus(Context context, Boolean bool) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(IS_LOGGED_IN, bool);
        editor.apply();
    }

    public void setUsername(Context context, String username) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(this.USERNAME, username);
        editor.apply();
    }

    public void clearUserInfo(Context context){
        setUsername(context, "");
        setUserStatus(context, false);
    }

}