package com.example.tberroa.girodicerapp.data;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

 // user of the Android device

public class UserInfo extends Application {

    private final String IS_LOGGED_IN = "is_logged_in";

    private SharedPreferences getSharedPreferences(Context context){
        return context.getSharedPreferences("user_info", MODE_PRIVATE);
    }

    public Boolean isLoggedIn(Context context){
        return getSharedPreferences(context).getBoolean(IS_LOGGED_IN, false);
    }

    public void setUserStatus(Context context, Boolean bool) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(IS_LOGGED_IN, bool);
        editor.apply();
    }
}
