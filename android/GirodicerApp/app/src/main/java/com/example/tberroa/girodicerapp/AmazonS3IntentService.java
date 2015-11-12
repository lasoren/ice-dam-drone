package com.example.tberroa.girodicerapp;

import android.app.IntentService;
import android.content.BroadcastReceiver;
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

    private int number_of_missions;
    private int number_of_aerials;
    private int number_of_thermals;
    private int number_of_iceDams;
    private int number_of_salts;
    private ArrayList<Mission> missions;

    // constructor
    public AmazonS3IntentService() {
        super("AmazonS3IntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // currently fetching
        BucketInfo bucketInfo = new BucketInfo(this.getApplicationContext());
        bucketInfo.setFetching(this.getApplicationContext(), true);

        // grab username and initialize number of missions
        String username = intent.getStringExtra("username");
        number_of_missions = 1;

        // Initialize the Amazon credentials provider and AmazonS3 Client
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-east-1:d64bdcf1-1d5e-441e-ba35-0a3876e4c82c", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );
        AmazonS3 s3Client = new AmazonS3Client(credentialsProvider);

        // get number of missions
        Boolean keepGoing = true;
        while (keepGoing){
            String key = "Flight " + Integer.toString(number_of_missions) + "/Aerial/aerial1.jpg";
            if (exists(s3Client, username, key)){
                ++number_of_missions;
            }
            else{
                --number_of_missions;    // most recent flight did not exist
                keepGoing = false;
            }
        }

        // create array of missions
        missions = new ArrayList<>(number_of_missions);

        // get number of aerials for all missions
        for (int i=1; i <= number_of_missions; i++ ){
            number_of_aerials = 0;
            String prefix = "Flight " + Integer.toString(i) + "/Aerial/";
            ObjectListing list = s3Client.listObjects(username, prefix);
            // get number of aerials in flight i
            for (S3ObjectSummary objectSummary : list.getObjectSummaries()){
                ++number_of_aerials;
            }
            // create Mission object and store in missions array
            Mission mission = new Mission();
            mission.num_of_aerials = number_of_aerials;
            missions.add(mission);
        }

        // save the number of missions
        bucketInfo.setNumOfMissions(this.getApplicationContext(), number_of_missions);

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