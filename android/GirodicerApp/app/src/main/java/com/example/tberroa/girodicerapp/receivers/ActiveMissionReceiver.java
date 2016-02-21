package com.example.tberroa.girodicerapp.receivers;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.data.ActiveMissionInfo;
import com.example.tberroa.girodicerapp.services.ImageTransferIntentService;
import com.example.tberroa.girodicerapp.services.ImageUploadService;

public class ActiveMissionReceiver extends BroadcastReceiver {
    public ActiveMissionReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String reload = Params.RELOAD_AM_ACTIVITY;
        String action = intent.getAction();
        ActiveMissionInfo activeMissionInfo = new ActiveMissionInfo();
        switch(action){
            case Params.DRONE_DONE:
                // begin image transfer
                context.startService(new Intent(context, ImageTransferIntentService.class));
                // reload active mission activity
                context.sendBroadcast(new Intent().setAction(reload));
                break;

            case Params.TRANSFER_COMPLETE:
                // reload active mission activity
                context.sendBroadcast(new Intent().setAction(reload));
                break;

            case DownloadManager.ACTION_DOWNLOAD_COMPLETE:
                // get the id of the last download
                long lastDownload = activeMissionInfo.getLastDownload(context);

                // get the id of the recently completed downloaded
                long recentDownload = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                int missionPhase = activeMissionInfo.getMissionPhase(context);
                if (lastDownload==recentDownload && missionPhase==2) {
                    // start uploading
                    context.startService(new Intent(context, ImageUploadService.class));

                    // reload active mission activity
                    context.sendBroadcast(new Intent().setAction(reload));
                }
                break;

            case Params.UPLOAD_COMPLETE:
                // mission was just completed, phase=0, inactive
                activeMissionInfo.setMissionPhase(context, 0);

                // reload active mission activity
                context.sendBroadcast(new Intent().setAction(reload));
                break;
        }
    }
}
