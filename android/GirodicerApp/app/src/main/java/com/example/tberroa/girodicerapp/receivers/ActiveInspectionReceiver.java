package com.example.tberroa.girodicerapp.receivers;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.data.CurrentInspectionInfo;
import com.example.tberroa.girodicerapp.services.BluetoothService;
import com.example.tberroa.girodicerapp.services.ImageTransferIntentService;
import com.example.tberroa.girodicerapp.services.ImageUploadService;

// this broadcast receiver is used to help chain post-inspection processing services
public class ActiveInspectionReceiver extends BroadcastReceiver {
    public ActiveInspectionReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String reload = Params.RELOAD;
        String action = intent.getAction();
        CurrentInspectionInfo currentInspectionInfo = new CurrentInspectionInfo();
        switch (action) {
            case Params.INSPECTION_TERMINATED:
                context.stopService(new Intent(context, BluetoothService.class));
                currentInspectionInfo.clearAll(context);

                // reload activity
                context.sendBroadcast(new Intent().setAction(reload));
                break;

            case Params.DRONE_DONE:
                // begin image transfer
                context.startService(new Intent(context, ImageTransferIntentService.class));

                // reload activity
                context.sendBroadcast(new Intent().setAction(reload));
                break;

            case Params.TRANSFER_COMPLETE:
                // reload activity
                context.sendBroadcast(new Intent().setAction(reload));

                // check if anything is being downloaded
                long lastDownloadId = currentInspectionInfo.getLastDownload(context);
                if (lastDownloadId == 0){ // nothing is being downloaded, go to upload phase
                    // start uploading
                    context.startService(new Intent(context, ImageUploadService.class));

                    // reload activity
                    context.sendBroadcast(new Intent().setAction(reload));
                }
                break;

            case DownloadManager.ACTION_DOWNLOAD_COMPLETE:
                // get the id of the last download
                long lastDownload = currentInspectionInfo.getLastDownload(context);

                // get the id of the recently completed downloaded
                long recentDownload = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                int phase = currentInspectionInfo.getPhase(context);
                if (lastDownload == recentDownload && phase == Params.CI_DATA_TRANSFER) {
                    // start uploading
                    context.startService(new Intent(context, ImageUploadService.class));

                    // reload activity
                    context.sendBroadcast(new Intent().setAction(reload));
                }
                break;

            case Params.UPLOAD_COMPLETE:
                // mission was just completed, make sure we are in proper phase
                if (currentInspectionInfo.getPhase(context) != Params.CI_INACTIVE){
                    currentInspectionInfo.setPhase(context, Params.CI_INACTIVE);
                }

                // reload activity
                context.sendBroadcast(new Intent().setAction(reload));
                break;
        }
    }
}
