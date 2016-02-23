package com.example.tberroa.girodicerapp.helpers;

import android.util.Log;

public class ExceptionHandler {

    public ExceptionHandler(){

    }

    public void HandleException(Exception e){
        Log.d("appErrors", "" + e.getMessage());
    }

}

