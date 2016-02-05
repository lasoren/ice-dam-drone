package com.example.tberroa.girodicerapp.services;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import com.example.tberroa.girodicerapp.activities.ActiveMissionActivity;
import com.example.tberroa.girodicerapp.data.Mission;
import com.example.tberroa.girodicerapp.data.MissionStatus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

// class to store images received from drone
// for now this class will simply download images from the internet
public class ImageTransferIntentService extends IntentService {

    public ImageTransferIntentService() {
        super("ImageTransferIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // mission in transfer phase, phase=2
        MissionStatus missionStatus = new MissionStatus();
        missionStatus.setMissionPhase(this, 2);
        ActiveMissionActivity.numberOfDownloads = 0;

        // grab username and mission number
        String username = intent.getExtras().getString("username");
        int missionNumber = intent.getExtras().getInt("mission_number");

        // initialize number of images to zero
        int numberOfAerials = 0, numberOfThermals = 0, numberOfIceDams = 0, numberOfSalts = 0;

        // begin downloading and storing
        String uriBase = "https://s3.amazonaws.com/girodicer/";
        for (int i = 1; i <= 4; i++) {
            String type;
            switch (i) {
                case 1:
                    type = "aerial";
                    break;
                case 2:
                    type = "thermal";
                    break;
                case 3:
                    type = "icedam";
                    break;
                case 4:
                    type = "salt";
                    break;
                default:
                    type = "aerial";
                    break;
            }
            for (int j = 1; j <= 5; j++) {
                // construct key name
                String xDown = Integer.toString(1); // repeatedly re-download mission #1 for now
                String xUp = Integer.toString(missionNumber); // upload as if it were new mission
                String y = Integer.toString(j); // image number
                String keyNameDown = username+"/mission"+xDown+"/"+type+"/"+type+y+".jpg";
                String keyNameUp = username+"/mission"+xUp+"/"+type+"/"+type+y+".jpg";

                DownloadManager downloadManager =
                        (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                Uri downloadUri = Uri.parse(uriBase+keyNameDown);
                DownloadManager.Request request = new DownloadManager.Request(downloadUri);

                // restrict the types of networks over which this download may proceed
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                        DownloadManager.Request.NETWORK_MOBILE);
                // set whether this download may proceed over a roaming connection
                request.setAllowedOverRoaming(false);
                // set the title of this download, to be displayed in notifications (if enabled)
                request.setTitle("Girodicer Image Transfer");
                // set a description of this download, to be displayed in notifications (if enabled)
                request.setDescription("in process of receiving images from drone");
                // set the local destination for the downloaded file
                String path = "/Girodicer/"+keyNameUp;
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, path);

                //Enqueue a new download
                downloadManager.enqueue(request);

                // log the image count
                switch (i) {
                    case 1:
                        numberOfAerials++;
                        break;
                    case 2:
                        numberOfThermals++;
                        break;
                    case 3:
                        numberOfIceDams++;
                        break;
                    case 4:
                        numberOfSalts++;
                        break;
                    default:
                        break;
                }
            }

            // save data
            missionStatus.setUsername(this, username);
            missionStatus.setMissionNumber(this, missionNumber);
            missionStatus.setNumberOfAerials(this, numberOfAerials);
            missionStatus.setNumberOfThermals(this, numberOfThermals);
            missionStatus.setNumberOfIceDams(this, numberOfIceDams);
            missionStatus.setNumberOfSalts(this, numberOfSalts);

            // broadcast that the service is complete
            Intent broadcastIntent = new Intent("TRANSFER_COMPLETE");
            sendBroadcast(broadcastIntent);
        }
    }
}
