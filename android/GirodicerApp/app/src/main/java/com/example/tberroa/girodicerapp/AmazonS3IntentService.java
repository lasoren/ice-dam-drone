package com.example.tberroa.girodicerapp;

import android.app.IntentService;
import android.content.Intent;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class AmazonS3IntentService extends IntentService {

    public AmazonS3IntentService() {
        super("AmazonS3IntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // currently fetching
        BucketInfo bucketInfo = new BucketInfo();
        bucketInfo.setFetching(this.getApplicationContext(), true);

        // grab username and initialize number of missions
        String username = intent.getStringExtra("username");
        int numberOfMissions = 1;

        // initialize the Amazon credentials provider and AmazonS3 Client
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-east-1:d64bdcf1-1d5e-441e-ba35-0a3876e4c82c", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );
        AmazonS3 s3Client = new AmazonS3Client(credentialsProvider);

        // get number of missions
        Boolean keepGoing = true;
        while (keepGoing){
            String key = "Flight " + Integer.toString(numberOfMissions) + "/Aerial/aerial1.jpg";
            if (exists(s3Client, username, key)){
                ++numberOfMissions;
            }
            else{
                --numberOfMissions;    // most recent flight did not exist
                keepGoing = false;
            }
        }

        // create array of missions
        ArrayList<Mission> missions = new ArrayList<>(numberOfMissions);

        // get number of images for all missions
        for (int i=1; i <= numberOfMissions; i++ ){
            int numberOfAerials, numberOfThermals, numberOfIceDams, numberOfSalts;
            numberOfAerials = numberOfThermals = numberOfIceDams = numberOfSalts = 0;
            String prefixAerial = "Flight " + Integer.toString(i) + "/Aerial/";
            String prefixThermal = "Flight " + Integer.toString(i) + "/Thermal/";
            String prefixIceDam = "Flight " + Integer.toString(i) + "/IceDam/";
            String prefixSalt = "Flight " + Integer.toString(i) + "/Salt/";
            ObjectListing listAerial = s3Client.listObjects(username, prefixAerial);
            ObjectListing listThermal = s3Client.listObjects(username, prefixThermal);
            ObjectListing listIceDam = s3Client.listObjects(username, prefixIceDam);
            ObjectListing listSalt = s3Client.listObjects(username, prefixSalt);
            // get number of aerials in flight i
            for (S3ObjectSummary objectSummary : listAerial.getObjectSummaries()){
                ++numberOfAerials;
            }
            // get number of thermals in flight i
            for (S3ObjectSummary objectSummary : listThermal.getObjectSummaries()){
                ++numberOfThermals;
            }
            // get number of ice dams in flight i
            for (S3ObjectSummary objectSummary : listIceDam.getObjectSummaries()){
                ++numberOfIceDams;
            }
            // get number of salts in flight i
            for (S3ObjectSummary objectSummary : listSalt.getObjectSummaries()){
                ++numberOfSalts;
            }
            // create new mission object and store in missions array
            Mission mission = new Mission();
            mission.numberOfAerials = numberOfAerials;
            mission.numberOfThermals = numberOfThermals;
            mission.numberOfIceDams = numberOfIceDams;
            mission.numberOfSalts = numberOfSalts;
            missions.add(mission);
        }

        // save the number of missions
        bucketInfo.setNumOfMissions(this.getApplicationContext(), numberOfMissions);

        // save the missions array
        Gson gson = new Gson();
        Type listOfMissions = new TypeToken<ArrayList<Mission>>(){}.getType();
        String json = gson.toJson(missions, listOfMissions);
        bucketInfo.setMissions(this.getApplicationContext(), json);

        // done fetching
        bucketInfo.setFetching(this.getApplicationContext(), false);

        // broadcast that the service is complete
        Intent broadcastIntent = new Intent("SOME_ACTION");
        sendBroadcast(broadcastIntent);
    }

    public boolean exists(AmazonS3 s3Client, String bucketName, String key) {
        try {
            s3Client.getObject(bucketName, key);
        } catch(AmazonServiceException e) {
            return false;
        }
        return true;
    }
}