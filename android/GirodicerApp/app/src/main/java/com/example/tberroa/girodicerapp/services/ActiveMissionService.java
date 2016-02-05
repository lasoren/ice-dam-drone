package com.example.tberroa.girodicerapp.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.example.tberroa.girodicerapp.data.ActiveMissionStatus;

public class ActiveMissionService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // mission is now in progress
        new ActiveMissionStatus().setMissionNotInProgress(this, false);
        // mission is active, phase=1
        new ActiveMissionStatus().setMissionPhase(this, 1);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Intent broadcastIntent = new Intent("MISSION_COMPLETE");
        sendBroadcast(broadcastIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // no binding, so return null
        return null;
    }
}
