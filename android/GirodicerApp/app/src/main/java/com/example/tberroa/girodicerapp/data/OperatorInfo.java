package com.example.tberroa.girodicerapp.data;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class OperatorInfo extends Application {

    // keys
    private final String IS_LOGGED_IN = "is_logged_in";
    private final String USERNAME = "USERNAME";
    private final String ID = "id";

    public OperatorInfo() {
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

    public int getId(Context context){
        return getSharedPreferences(context).getInt(this.ID, 0);
    }

    public void setUserStatus(Context context, Boolean bool) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(IS_LOGGED_IN, bool);
        editor.apply();
    }

    public void setId(Context context, int id) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(this.ID, id);
        editor.apply();
    }

    public void setUsername(Context context, String username) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(this.USERNAME, username);
        editor.apply();
    }

    public void clearAll(Context context){
        setUsername(context, "");
        setUserStatus(context, false);
    }
}