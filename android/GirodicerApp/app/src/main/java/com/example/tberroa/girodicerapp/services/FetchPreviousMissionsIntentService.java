package com.example.tberroa.girodicerapp.services;

import android.app.IntentService;
import android.content.Intent;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.data.PreviousMissionsInfo;
import com.example.tberroa.girodicerapp.data.Mission;
import com.example.tberroa.girodicerapp.helpers.Utilities;
import com.example.tberroa.girodicerapp.network.CloudTools;
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

        // grab username and initialize number of missions to 1
        String username = intent.getStringExtra("username");
        int numberOfMissions = 1;

        // initialize the client
        AmazonS3 s3Client = new CloudTools(this).getAmazonS3Client();

        // get number of missions
        Boolean keepGoing = true;
        while (keepGoing){
            String key = Utilities.ConstructImageKey(username, numberOfMissions, "aerial1.jpg");
            if (exists(s3Client, key)){
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
            int numberOfAerials, numberOfThermals, numberOfIceDams, numberOfSalts;
            String x = Integer.toString(i);
            String prefixAerial = username+"/mission"+x+"/images/aerial";
            String prefixThermal = username+"/mission"+x+"/images/thermal";
            String prefixIceDam = username+"/mission"+x+"/images/icedam";
            String prefixSalt = username+"/mission"+x+"/images/salt";

            // get number of aerials in mission i
            ObjectListing aerials = s3Client.listObjects(Params.CLOUD_BUCKET_NAME, prefixAerial);
            numberOfAerials = aerials.getObjectSummaries().size();

            // get number of thermals in mission i
            ObjectListing thermals = s3Client.listObjects(Params.CLOUD_BUCKET_NAME, prefixThermal);
            numberOfThermals = thermals.getObjectSummaries().size();

            // get number of ice dams in mission i
            ObjectListing iceDams = s3Client.listObjects(Params.CLOUD_BUCKET_NAME, prefixIceDam);
            numberOfIceDams = iceDams.getObjectSummaries().size();

            // get number of salts in mission i
            ObjectListing salts = s3Client.listObjects(Params.CLOUD_BUCKET_NAME, prefixSalt);
            numberOfSalts = salts.getObjectSummaries().size();

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
        Intent broadcastIntent = new Intent(Params.FETCHING_COMPLETE);
        sendBroadcast(broadcastIntent);
    }

    private boolean exists(AmazonS3 s3Client, String key) {
        try {
            s3Client.getObject(Params.CLOUD_BUCKET_NAME, key);
        } catch(AmazonServiceException e) {
            return false;
        }
        return true;
    }
}