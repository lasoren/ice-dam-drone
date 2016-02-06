package com.example.tberroa.girodicerapp.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.data.ActiveMissionInfo;
import com.example.tberroa.girodicerapp.data.PreviousMissionsInfo;

public class DroneService extends Service {

    @Override
    public void onCreate(){
        // mission is now in progress
        ActiveMissionInfo activeMissionInfo = new ActiveMissionInfo();
        activeMissionInfo.setMissionNotInProgress(this, false);

        // drone is active, phase=1
        activeMissionInfo.setMissionPhase(this, 1);

        // set mission number
        int missionNumber = new PreviousMissionsInfo().getNumOfMissions(this)+1;
        activeMissionInfo.setMissionNumber(this, missionNumber);
    }

    @Override
    public void onDestroy() {
        // drone is done and ready for transfer, phase=2
        new ActiveMissionInfo().setMissionPhase(this, 2);
        sendBroadcast(new Intent().setAction(Params.DRONE_READY));
    }

    @Override
    public IBinder onBind(Intent intent) {
        // no binding, so return null
        return null;
    }
}
