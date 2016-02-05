package com.example.tberroa.girodicerapp.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.example.tberroa.girodicerapp.data.MissionStatus;
import com.example.tberroa.girodicerapp.data.PreviousMissionsInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;

public class ImageUploadIntentService extends IntentService {

    public ImageUploadIntentService() {
        super("ImageUploadIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // mission in upload phase, phase=3
        MissionStatus missionStatus = new MissionStatus();
        missionStatus.setMissionPhase(this, 3);

        // grab data
        String username = missionStatus.getUsername(this);
        int missionNumber = missionStatus.getMissionNumber(this);
        int numberOfAerials = missionStatus.getNumberOfAerials(this);
        int numberOfThermals = missionStatus.getNumberOfThermals(this);
        int numberOfIceDams = missionStatus.getNumberOfIceDams(this);
        int numberOfSalts = missionStatus.getNumberOfSalts(this);

        // initialize the Amazon credentials provider, AmazonS3 client, and transfer utility
        CognitoCachingCredentialsProvider credentialsProvider =
                new CognitoCachingCredentialsProvider(getApplicationContext(),
                        "us-east-1:d64bdcf1-1d5e-441e-ba35-0a3876e4c82c", Regions.US_EAST_1);
        AmazonS3 s3Client = new AmazonS3Client(credentialsProvider);
        TransferUtility transferUtility = new TransferUtility(s3Client, getApplicationContext());

        // generate string required to upload image, then upload image
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
                if (file.exists()){
                    transferUtility.upload("girodicer", keyName, file);
                }
            }
        }

        // mission program is now completely over
        missionStatus.setMissionNotInProgress(this, true);
        // mission was just completed, phase=0, inactive
        missionStatus.setMissionPhase(this, 0);
        // previous missions info is out of date
        new PreviousMissionsInfo().setUpToDate(this, false);

        // broadcast that the service is complete
        Intent broadcastIntent = new Intent("UPLOAD_COMPLETE");
        sendBroadcast(broadcastIntent);
    }
}