package com.example.tberroa.girodicerapp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.tberroa.girodicerapp.bluetooth.GProtocol;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.services.BluetoothService;

public class CurrentInspectionReceiver extends BroadcastReceiver {

    public CurrentInspectionReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // action specific actions
        switch (intent.getAction()) {
            case Params.DRONE_CONNECT_SUCCESS:
                // send ok message (in case its a reconnect, this may help reestablish program flow)
                Log.d(Params.TAG_DBG + Params.TAG_DS, "@CurrentInspectionReceiver: sending ok signal to drone");
                BluetoothService.btConnectionThread.write(GProtocol.Pack(GProtocol.COMMAND_BLUETOOTH_OK_TO_SEND, 1, new byte[1], false));
                break;
            case Params.INSPECTION_TERMINATED:
                // stop bluetooth service
                context.stopService(new Intent(context, BluetoothService.class));
                break;
        }

        // always broadcast a reload
        context.sendBroadcast(new Intent().setAction(Params.RELOAD));
    }
}