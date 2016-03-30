package com.example.tberroa.girodicerapp.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.example.tberroa.girodicerapp.data.ClientId;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.data.CurrentInspectionInfo;
import com.example.tberroa.girodicerapp.data.PastInspectionsInfo;

public class DroneService extends Service {

    @Override
    public void onCreate(){
        // inspection is now in progress
        CurrentInspectionInfo currentInspectionInfo = new CurrentInspectionInfo();
        currentInspectionInfo.setNotInProgress(this, false);

        // drone is active, phase=1
        currentInspectionInfo.setPhase(this, 1);

        // get id of most recent inspection
        int mostRecentId = new PastInspectionsInfo().getMostRecentId(this);

        // save client id
        currentInspectionInfo.setClientId(this, new ClientId().get(this));

        // save inspection id
        currentInspectionInfo.setInspectionId(this, mostRecentId + 1);

        // broadcast that a new inspection has started
        Intent inspectionStarted = new Intent();
        inspectionStarted.setAction(Params.INSPECTION_STARTED);
        sendBroadcast(inspectionStarted);

    }

    @Override
    public void onDestroy() {
        // drone is done and ready for transfer
        sendBroadcast(new Intent().setAction(Params.DRONE_DONE));
    }

    @Override
    public IBinder onBind(Intent intent) {
        // no binding, so return null
        return null;
    }
}
