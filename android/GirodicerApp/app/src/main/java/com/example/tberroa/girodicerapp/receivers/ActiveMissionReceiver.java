package com.example.tberroa.girodicerapp.receivers;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.data.ActiveMissionInfo;
import com.example.tberroa.girodicerapp.helpers.Utilities;

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

                // get the id of the last download
                long lastDownload = activeMissionInfo.getLastDownload(context);

                // get the id of the recently completed downloaded
                long recentDownload = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);

                if (lastDownload == recentDownload) {
                    // start uploading
                    Utilities.startImageUploadService(context);

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
