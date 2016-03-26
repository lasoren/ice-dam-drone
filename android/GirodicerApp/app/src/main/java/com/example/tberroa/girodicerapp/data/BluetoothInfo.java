package com.example.tberroa.girodicerapp.data;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class BluetoothInfo extends Application {

    // keys
    private final String STATE = "state";
    private final String ERROR_CODE = "error_code";

    public BluetoothInfo() {
    }

    private SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("bluetooth_info", MODE_PRIVATE);
    }

    public int getState(Context context) {
        return getSharedPreferences(context).getInt(STATE, 0);
    }

    public int getErrorCode(Context context) {
        return getSharedPreferences(context).getInt(ERROR_CODE, 0);
    }

    public void setState(Context context, int phase) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(STATE, phase);
        editor.apply();
    }

    public void setErrorCode(Context context, int phase) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(ERROR_CODE, phase);
        editor.apply();
    }
}



