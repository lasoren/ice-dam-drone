package com.example.tberroa.girodicerapp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.services.BluetoothService;

public class CurrentInspectionReceiver extends BroadcastReceiver {

    public CurrentInspectionReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // action specific actions
        switch (intent.getAction()) {
            case Params.INSPECTION_TERMINATED:
                // stop bluetooth service
                context.stopService(new Intent(context, BluetoothService.class));
                break;
        }

        // always broadcast a reload
        context.sendBroadcast(new Intent().setAction(Params.RELOAD));
    }
}