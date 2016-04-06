package com.example.tberroa.girodicerapp.data;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

@SuppressWarnings("unused")
public class OperatorInfo extends Application {

    // keys
    private final String OPERATOR_ID = "operator_id";
    private final String USER_ID = "user_id";
    private final String SESSION_ID = "session_id";
    private final String FIRST_NAME = "first_name";
    private final String LAST_NAME = "last_name";
    private final String EMAIL = "email";

    public OperatorInfo() {
    }

    private SharedPreferences getSharedPreferences(Context context){
        return context.getSharedPreferences("operator_id", MODE_PRIVATE);
    }

    public int getOperatorId(Context context){
        return getSharedPreferences(context).getInt(OPERATOR_ID, 0);
    }

    public int getUserId(Context context){
        return getSharedPreferences(context).getInt(USER_ID, 0);
    }

    public String getSessionId(Context context){
        return getSharedPreferences(context).getString(SESSION_ID, "");
    }

    public String getFirstName(Context context){
        return getSharedPreferences(context).getString(FIRST_NAME, "");
    }

    public String getLastName(Context context){
        return getSharedPreferences(context).getString(LAST_NAME, "");
    }

    public String getEmail(Context context){
        return getSharedPreferences(context).getString(EMAIL, "");
    }

    public void setOperatorId(Context context, int id) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(OPERATOR_ID, id);
        editor.apply();
    }

    public void setUserId(Context context, int id) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(USER_ID, id);
        editor.apply();
    }

    public void setSessionId(Context context, String id) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(SESSION_ID, id);
        editor.apply();
    }

    public void setFirstName(Context context, String firstName) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(FIRST_NAME, firstName);
        editor.apply();
    }

    public void setLastName(Context context, String lastName) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(LAST_NAME, lastName);
        editor.apply();
    }

    public void setEmail(Context context, String email) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(EMAIL, email);
        editor.apply();
    }

    public void clear(Context context){
        setOperatorId(context, 0);
        setUserId(context, 0);
        setSessionId(context, "");
        setFirstName(context, "");
        setLastName(context, "");
        setEmail(context, "");
    }
}