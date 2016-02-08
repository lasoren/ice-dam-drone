package com.example.tberroa.girodicerapp.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.data.Mission;
import com.example.tberroa.girodicerapp.data.ActiveMissionInfo;
import com.example.tberroa.girodicerapp.data.PreviousMissionsInfo;
import com.example.tberroa.girodicerapp.data.UserInfo;
import com.example.tberroa.girodicerapp.helpers.Utilities;
import com.example.tberroa.girodicerapp.network.CloudTools;
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
        ActiveMissionInfo activeMissionInfo = new ActiveMissionInfo();
        activeMissionInfo.setMissionPhase(this, 3);

        // grab data
        String username = new UserInfo().getUsername(this);
        int missionNumber = activeMissionInfo.getMissionNumber(this);
        String json = activeMissionInfo.getMissionData(this);
        Mission missionData = new Gson().fromJson(json, new TypeToken<Mission>() {}.getType());
        int numberOfAerials = missionData.getNumberOfAerials();
        int numberOfThermals = missionData.getNumberOfThermals();
        int numberOfIceDams = missionData.getNumberOfIceDams();
        int numberOfSalts = missionData.getNumberOfSalts();

        // store the different image types in an array
        String imageType[] = {"aerial", "thermal", "icedam", "salt"};
        Bundle numberOfImages = new Bundle();
        numberOfImages.putInt("aerial", numberOfAerials);
        numberOfImages.putInt("thermal", numberOfThermals);
        numberOfImages.putInt("icedam", numberOfIceDams);
        numberOfImages.putInt("salt", numberOfSalts);

        // generate strings required to upload images, then upload images
        for(String type: imageType) {
            for (int j = 1; j <= numberOfImages.getInt(type); j++) {
                String imageName = type+Integer.toString(j)+".jpg";
                String keyName = Utilities.ConstructImageKey(username, missionNumber, imageName);
                String location = Environment.DIRECTORY_PICTURES+Params.HOME_FOLDER+keyName;
                File file = Environment.getExternalStoragePublicDirectory(location);

                if (file.exists()){ // check if file exists before trying to upload
                    TransferUtility transfer = new CloudTools(this).getTransferUtility();
                    transfer.upload(Params.CLOUD_BUCKET_NAME, keyName, file);
                }
            }
        }

        // mission program is now completely over
        activeMissionInfo.setMissionNotInProgress(this, true);
        // mission was just completed, phase=0, inactive
        activeMissionInfo.setMissionPhase(this, 0);

        // previous missions info is out of date
        new PreviousMissionsInfo().setUpToDate(this, false);

        // update previous missions info
        if (!new PreviousMissionsInfo().isFetching(this)){
            startService(new Intent(this, FetchPMIntentService.class));
        }

        // broadcast that the upload is complete
        sendBroadcast(new Intent().setAction(Params.UPLOAD_COMPLETE));
    }
}