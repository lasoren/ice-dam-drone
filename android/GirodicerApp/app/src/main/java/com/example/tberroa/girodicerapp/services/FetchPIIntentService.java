package com.example.tberroa.girodicerapp.services;

import android.app.IntentService;
import android.content.Intent;

import com.example.tberroa.girodicerapp.data.ClientId;
import com.example.tberroa.girodicerapp.data.OperatorId;

public class FetchPIIntentService extends IntentService {

    public FetchPIIntentService() {
        super("FetchPIIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        /* old code

        // currently fetching
        PastInspectionsInfo pastInspectionsInfo = new PastInspectionsInfo();
        pastInspectionsInfo.setFetching(this, true);

        // broadcast that fetching has begun
        Intent fetchingStarted = new Intent();
        fetchingStarted.setAction(Params.UPDATING_STARTED);
        sendBroadcast(fetchingStarted);

        // grab username
        String username = new OperatorId().getUsername(this);

        // initialize the client
        AmazonS3 s3Client = CloudTools.getAmazonS3Client(this);

        // get number of missions
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
        listObjectsRequest.withBucketName(Params.CLOUD_BUCKET_NAME);
        listObjectsRequest.withPrefix(username + "/");
        listObjectsRequest.withDelimiter("/");
        int numberOfMissions= s3Client.listObjects(listObjectsRequest).getCommonPrefixes().size();

        // create array of missions
        ArrayList<Client> missions = new ArrayList<>(numberOfMissions);

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
            Client mission = new Client();
            mission.setNumberOfAerials(numberOfAerials);
            mission.setNumberOfThermals(numberOfThermals);
            mission.setNumberOfIceDams(numberOfIceDams);
            mission.setNumberOfSalts(numberOfSalts);
            missions.add(mission);
        }

        // save the number of missions
        pastInspectionsInfo.setNumOfMissions(this, numberOfMissions);

        // save the missions array
        Gson gson = new Gson();
        Type listOfMissions = new TypeToken<ArrayList<Client>>(){}.getType();
        String json = gson.toJson(missions, listOfMissions);
        pastInspectionsInfo.setMissions(this, json);

        // done fetching and previous missions info is up to date
        pastInspectionsInfo.setFetching(this, false);
        pastInspectionsInfo.setUpToDate(this, true);

        // broadcast that the service is complete
        Intent broadcastIntent = new Intent(Params.UPDATING_COMPLETE);
        sendBroadcast(broadcastIntent);
        */
    }

}