package com.example.tberroa.girodicerapp.data;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class OperatorId extends Application {

    // keys
    private final String ID = "id";

    public OperatorId() {
    }

    private SharedPreferences getSharedPreferences(Context context){
        return context.getSharedPreferences("operator_id", MODE_PRIVATE);
    }

    public int get(Context context){
        return getSharedPreferences(context).getInt(ID, 0);
    }

    public void set(Context context, int id) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(this.ID, id);
        editor.apply();
    }

    public void clear(Context context){
        set(context, 0);
    }
}