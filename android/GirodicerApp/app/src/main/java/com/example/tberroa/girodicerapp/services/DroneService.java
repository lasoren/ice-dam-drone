package com.example.tberroa.girodicerapp.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.data.ActiveInspectionInfo;
import com.example.tberroa.girodicerapp.data.PastInspectionsInfo;

public class DroneService extends Service {

    @Override
    public void onCreate(){/*
        // inspection is now in progress
        ActiveInspectionInfo activeInspectionInfo = new ActiveInspectionInfo();
        activeInspectionInfo.setNotInProgress(this, false);

        // drone is active, phase=1
        activeInspectionInfo.setPhase(this, 1);

        // set inspection number
        int missionNumber = new PastInspectionsInfo().getNumOfMissions(this)+1;
        activeInspectionInfo.setId(this, missionNumber);

        // broadcast that a new mission has started
        Intent missionStarted = new Intent();
        missionStarted.setAction(Params.INSPECTION_STARTED);
        sendBroadcast(missionStarted);
        */
    }

    @Override
    public void onDestroy() {/*
        // drone is done and ready for transfer
        sendBroadcast(new Intent().setAction(Params.DRONE_DONE));
        */
    }

    @Override
    public IBinder onBind(Intent intent) {
        // no binding, so return null
        return null;
    }
}
