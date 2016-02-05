package com.example.tberroa.girodicerapp.services;

import android.app.IntentService;
import android.content.Intent;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.example.tberroa.girodicerapp.data.PreviousMissionsInfo;
import com.example.tberroa.girodicerapp.data.Mission;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class FetchPreviousMissionsIntentService extends IntentService {

    public FetchPreviousMissionsIntentService() {
        super("FetchPreviousMissionsIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // currently fetching
        PreviousMissionsInfo previousMissionsInfo = new PreviousMissionsInfo();
        previousMissionsInfo.setFetching(this, true);
        final String bucketName = "girodicer";

        // grab username and initialize number of missions to 1
        String username = intent.getStringExtra("username");
        int numberOfMissions = 1;

        // initialize the Amazon credentials provider and AmazonS3 Client
        CognitoCachingCredentialsProvider credentialsProvider =
                new CognitoCachingCredentialsProvider(this,
                        "us-east-1:d64bdcf1-1d5e-441e-ba35-0a3876e4c82c",
                        Regions.US_EAST_1);
        AmazonS3 s3Client = new AmazonS3Client(credentialsProvider);

        // get number of missions
        Boolean keepGoing = true;
        while (keepGoing){
            String number = Integer.toString(numberOfMissions);
            String key = username+"/mission"+number+"/aerial/aerial1.jpg";
            if (exists(s3Client, bucketName, key)){
                ++numberOfMissions;
            }
            else{
                --numberOfMissions;
                keepGoing = false;
            }
        }

        // create array of missions
        ArrayList<Mission> missions = new ArrayList<>(numberOfMissions);

        // get number of images for all missions
        for (int i=1; i <= numberOfMissions; i++ ){
            int numberOfAerials = 0, numberOfThermals = 0, numberOfIceDams = 0, numberOfSalts = 0;
            String x = Integer.toString(i);
            String prefixAerial = username+"/mission"+x+"/aerial/";
            String prefixThermal = username+"/mission"+x+"/thermal/";
            String prefixIceDam = username+"/mission"+x+"/icedam/";
            String prefixSalt = username+"/mission"+x+"/salt/";
            ObjectListing listAerial = s3Client.listObjects(bucketName, prefixAerial);
            ObjectListing listThermal = s3Client.listObjects(bucketName, prefixThermal);
            ObjectListing listIceDam = s3Client.listObjects(bucketName, prefixIceDam);
            ObjectListing listSalt = s3Client.listObjects(bucketName, prefixSalt);
            // get number of aerials in mission i
            for (S3ObjectSummary objectSummary : listAerial.getObjectSummaries()){
                ++numberOfAerials;
            }
               // get number of thermals in mission i
            for (S3ObjectSummary objectSummary : listThermal.getObjectSummaries()){
                ++numberOfThermals;
            }
            // get number of ice dams in mission i
            for (S3ObjectSummary objectSummary : listIceDam.getObjectSummaries()){
                ++numberOfIceDams;
            }
            // get number of salts in mission i
            for (S3ObjectSummary objectSummary : listSalt.getObjectSummaries()){
                ++numberOfSalts;
            }
                // create new mission object and store in missions array
            Mission mission = new Mission();
            mission.setNumberOfAerials(numberOfAerials);
            mission.setNumberOfThermals(numberOfThermals);
            mission.setNumberOfIceDams(numberOfIceDams);
            mission.setNumberOfSalts(numberOfSalts);
            missions.add(mission);
        }

        // save the number of missions
        previousMissionsInfo.setNumOfMissions(this, numberOfMissions);

        // save the missions array
        Gson gson = new Gson();
        Type listOfMissions = new TypeToken<ArrayList<Mission>>(){}.getType();
        String json = gson.toJson(missions, listOfMissions);
        previousMissionsInfo.setMissions(this, json);

        // done fetching and previous missions info is up to date
        previousMissionsInfo.setFetching(this, false);
        previousMissionsInfo.setUpToDate(this, true);

        // broadcast that the service is complete
        Intent broadcastIntent = new Intent("FETCHING_COMPLETE");
        sendBroadcast(broadcastIntent);
    }

    private boolean exists(AmazonS3 s3Client, String bucketName, String key) {
        try {
            s3Client.getObject(bucketName, key);
        } catch(AmazonServiceException e) {
            return false;
        }
        return true;
    }
}