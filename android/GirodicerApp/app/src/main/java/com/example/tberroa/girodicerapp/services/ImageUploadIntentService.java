package com.example.tberroa.girodicerapp.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.example.tberroa.girodicerapp.activities.ActiveMissionActivity;
import com.example.tberroa.girodicerapp.data.Mission;
import com.example.tberroa.girodicerapp.data.ActiveMissionStatus;
import com.example.tberroa.girodicerapp.data.PreviousMissionsInfo;
import com.example.tberroa.girodicerapp.data.UserInfo;
import com.example.tberroa.girodicerapp.network.CloudServiceTools;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;

public class ImageUploadIntentService extends IntentService {

    public ImageUploadIntentService() {
        super("ImageUploadIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // mission in upload phase, phase=3
        ActiveMissionStatus activeMissionStatus = new ActiveMissionStatus();
        activeMissionStatus.setMissionPhase(this, 3);

        // grab data
        String username = new UserInfo().getUsername(this);
        int missionNumber = activeMissionStatus.getMissionNumber(this);
        String json = activeMissionStatus.getMissionData(this);
        Mission missionData = new Gson().fromJson(json, new TypeToken<Mission>() {
        }.getType());
        int numberOfAerials = missionData.getNumberOfAerials();
        int numberOfThermals = missionData.getNumberOfThermals();
        int numberOfIceDams = missionData.getNumberOfIceDams();
        int numberOfSalts = missionData.getNumberOfSalts();

        // generate strings required to upload images, then upload images
        String type;
        for(int i=1; i<=4; i++) {
            int maxImages;
            switch (i) {
                case 1:
                    maxImages = numberOfAerials;
                    type = "aerial";
                    break;
                case 2:
                    maxImages = numberOfThermals;
                    type = "thermal";
                    break;
                case 3:
                    maxImages = numberOfIceDams;
                    type = "icedam";
                    break;
                case 4:
                    maxImages = numberOfSalts;
                    type = "salt";
                    break;
                default:
                    maxImages = numberOfAerials;
                    type = "aerial";
                    break;
            }
            for (int j = 1; j <= maxImages; j++) {
                String x = Integer.toString(j);
                String keyName = username+"/mission"+missionNumber+"/"+type+"/"+type+x+".jpg";
                String path = "/Girodicer/"+keyName;
                File file = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES+path);
                if (file.exists()){ // check if file exists before trying to upload

                    TransferObserver transfer = new CloudServiceTools(this).getObserver(keyName, file);
                    transfer.setTransferListener(new TransferListener() {
                        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                            // update progress bar
                            int progress = (int)(bytesCurrent/bytesTotal);
                            ActiveMissionActivity.progressBar.setProgress(progress);
                        }

                        public void onStateChanged(int id, TransferState state) {
                        }

                        public void onError(int id, Exception ex) {
                        }
                    });
                }
            }
        }

        // mission program is now completely over
        activeMissionStatus.setMissionNotInProgress(this, true);
        // mission was just completed, phase=0, inactive
        activeMissionStatus.setMissionPhase(this, 0);
        // previous missions info is out of date
        new PreviousMissionsInfo().setUpToDate(this, false);

        // broadcast that the service is complete
        Intent broadcastIntent = new Intent("UPLOAD_COMPLETE");
        sendBroadcast(broadcastIntent);
    }
}