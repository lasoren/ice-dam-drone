package com.example.tberroa.girodicerapp.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.example.tberroa.girodicerapp.data.PreviousMissionsInfo;
import com.example.tberroa.girodicerapp.data.ServiceStatus;

import java.io.File;

public class UploadCurrentMissionDataIntentService extends IntentService {

    public UploadCurrentMissionDataIntentService() {
        super("UploadCurrentMissionDataIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String username = intent.getExtras().getString("username");
        int missionNumber = intent.getExtras().getInt("mission_number");
        int numberOfAerials = intent.getExtras().getInt("number_of_aerials");
        int numberOfThermals = intent.getExtras().getInt("number_of_thermals");
        int numberOfIceDams = intent.getExtras().getInt("number_of_icedams");
        int numberOfSalts = intent.getExtras().getInt("number_of_salts");

        // initialize the Amazon credentials provider, AmazonS3 client, and transfer utility
        CognitoCachingCredentialsProvider credentialsProvider =
                new CognitoCachingCredentialsProvider(getApplicationContext(),
                        "us-east-1:d64bdcf1-1d5e-441e-ba35-0a3876e4c82c", Regions.US_EAST_1);
        AmazonS3 s3Client = new AmazonS3Client(credentialsProvider);
        TransferUtility transferUtility = new TransferUtility(s3Client, getApplicationContext());

        // upload the images

        String keyNext, directoryEnd, fileStart;
        for(int i=1; i<=4; i++) {
            int maxImages;
            switch (i) {
                case 1:
                    maxImages = numberOfAerials;
                    keyNext = "Aerial/aerial";
                    directoryEnd = "Aerial/";
                    fileStart = "aerial";
                    break;
                case 2:
                    maxImages = numberOfThermals;
                    keyNext = "Thermal/thermal";
                    directoryEnd = "Thermal/";
                    fileStart = "thermal";
                    break;
                case 3:
                    maxImages = numberOfIceDams;
                    keyNext = "IceDam/icedam";
                    directoryEnd = "IceDam/";
                    fileStart = "icedam";
                    break;
                case 4:
                    maxImages = numberOfSalts;
                    keyNext = "Salt/salt";
                    directoryEnd = "Salt/";
                    fileStart = "salt";
                    break;
                default:
                    maxImages = numberOfAerials;
                    keyNext = "Aerial/aerial";
                    directoryEnd = "Aerial/";
                    fileStart = "aerial";
                    break;
            }
            for (int j = 1; j <= maxImages; j++) {
                String fileEnd = Integer.toString(j) + ".jpg";
                String keyName = username+"/Mission "+missionNumber+"/"+keyNext+fileEnd;
                String directory = "/Girodicer/"+username+"/Mission"+missionNumber+"/"+directoryEnd+fileStart+fileEnd;
                String fileLocation = Environment.DIRECTORY_PICTURES+directory;
                File fileName = new File(fileLocation);
                transferUtility.upload("girodicer", keyName, fileName);

            }

        }

        // update previous missions info, data is no longer up to date
        new PreviousMissionsInfo().setUpToDate(getApplicationContext(), false);

        // update service status, service is no longer running
        new ServiceStatus().setServiceStatus(getApplicationContext(), false);

        // print message to user, service ended
        Toast.makeText(this, "mission service has ended", Toast.LENGTH_SHORT).show();

        // broadcast that the service is complete
        Intent broadcastIntent = new Intent("UPLOAD_COMPLETE");
        sendBroadcast(broadcastIntent);
    }
}