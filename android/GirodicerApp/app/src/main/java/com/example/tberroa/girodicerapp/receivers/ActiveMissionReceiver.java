package com.example.tberroa.girodicerapp.receivers;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.data.ActiveMissionInfo;
import com.example.tberroa.girodicerapp.data.Mission;
import com.example.tberroa.girodicerapp.helpers.Utilities;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ActiveMissionReceiver extends BroadcastReceiver {
    public ActiveMissionReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String reload = Params.RELOAD_ACTIVE_MISSION_ACTIVITY;
        String action = intent.getAction();
        switch(action){
            case Params.DRONE_READY:
                Utilities.startImageTransferService(context);
                // reload active mission activity
                context.sendBroadcast(new Intent().setAction(reload));
                break;

            case Params.TRANSFER_COMPLETE:
                // reload active mission activity
                context.sendBroadcast(new Intent().setAction(reload));
                break;

            case DownloadManager.ACTION_DOWNLOAD_COMPLETE:
                ActiveMissionInfo activeMissionInfo = new ActiveMissionInfo();

                // get current count of completed downloads
                int count = activeMissionInfo.getCompletedDownloads(context);

                // increment count
                count++;

                // store the new value of count
                activeMissionInfo.setCompletedDownloads(context, count);

                // get the mission phase
                int missionPhase = activeMissionInfo.getMissionPhase(context);

                if (missionPhase == 5) { // all downloads queued, waiting for them to complete
                    // get the total number of images
                    String json = activeMissionInfo.getMissionData(context);
                    Gson gson = new Gson();
                    Mission mission = gson.fromJson(json, new TypeToken<Mission>(){}.getType());
                    int numberOfImages = mission.getNumberOfImages();

                    if (count == numberOfImages) {
                        Utilities.startImageUploadService(context); // start uploading
                    }
                    // reload active mission activity
                    context.sendBroadcast(new Intent().setAction(reload));
                }
                break;

            case Params.UPLOAD_COMPLETE:
                // reload active mission activity
                context.sendBroadcast(new Intent().setAction(reload));
                break;
        }
    }
}
