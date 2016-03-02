package com.example.tberroa.girodicerapp.services;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.data.ActiveInspectionInfo;
import com.example.tberroa.girodicerapp.helpers.ExceptionHandler;
import com.example.tberroa.girodicerapp.helpers.Utilities;
import com.example.tberroa.girodicerapp.network.CloudTools;

import java.io.File;

public class ImageUploadService extends Service {

    private final ActiveInspectionInfo activeInspectionInfo = new ActiveInspectionInfo();
    private final String imageType[] = {"aerial", "thermal", "icedam", "salt"};
    private Bundle numberOfImages;
    private int inspectionId;
    private TransferUtility transfer;

    @Override
    public void onCreate(){
        // inspection in upload phase, phase=3
        activeInspectionInfo.setPhase(this, 3);

        // broadcast that upload phase has begun
        Intent uploadStarted = new Intent();
        uploadStarted.setAction(Params.UPLOAD_STARTED);
        sendBroadcast(uploadStarted);

        // grab data
        inspectionId = activeInspectionInfo.getInspectionId(this);
        int numberOfAerials = activeInspectionInfo.getAerialCount(this);
        int numberOfThermals = activeInspectionInfo.getThermalCount(this);
        int numberOfIceDams = activeInspectionInfo.getIceDamCount(this);
        int numberOfSalts = activeInspectionInfo.getSaltCount(this);

        // pack data into bundle to make iterative access simpler
        numberOfImages = new Bundle();
        numberOfImages.putInt("aerial", numberOfAerials);
        numberOfImages.putInt("thermal", numberOfThermals);
        numberOfImages.putInt("icedam", numberOfIceDams);
        numberOfImages.putInt("salt", numberOfSalts);

        // initialize transfer utility
        transfer = CloudTools.getTransferUtility(ImageUploadService.this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        final Runnable runnable = new Runnable() {
            public void run() {
                // generate strings required to upload images, then upload images
                for (String type : imageType) {
                    for (int j = 1; j <= numberOfImages.getInt(type); j++) {
                        String imageName = type + Integer.toString(j) + ".jpg";
                        String keyName = Utilities.constructImageKey(inspectionId, imageName);
                        String location = Environment.DIRECTORY_PICTURES + Params.HOME_FOLDER + keyName;
                        final File file = Environment.getExternalStoragePublicDirectory(location);

                        if (file.exists()) { // check if file exists before trying to upload
                            TransferObserver observer; // used to monitor upload status
                            observer = transfer.upload(Params.CLOUD_BUCKET_NAME, keyName, file);
                            boolean notDone = true;
                            while(notDone){
                                try{
                                    Thread.sleep(250);
                                    observer.refresh();
                                    if (observer.getState() == TransferState.COMPLETED){
                                        notDone = false;
                                        if(!file.delete()){
                                            try{
                                                throw new Exception("Cannot delete file");
                                            }catch (Exception e){
                                                new ExceptionHandler().HandleException(e);
                                            }
                                        }
                                    }
                                }catch (InterruptedException e){
                                    new ExceptionHandler().HandleException(e);
                                }
                            }
                        }
                    }
                }
                // uploading is complete

                // save inspection on backend & locally

                // save inspection images on backend & locally



                // delete directory
                String path = Environment.DIRECTORY_PICTURES+Params.HOME_FOLDER;
                File directory = Environment.getExternalStoragePublicDirectory(path);
                Utilities.deleteDirectory(directory);
                ImageUploadService.this.stopSelf();
                stopSelf();
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();

        // recreate service if killed by OS
        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        // inspection is no longer in progress
        activeInspectionInfo.setNotInProgress(this, true);

        // post inspection processing just concluded, phase=0
        activeInspectionInfo.setPhase(this, 0);

        /*
        // past inspection info is out of date
        PastInspectionsInfo pastInspectionsInfo =  new PastInspectionsInfo();
        pastInspectionsInfo.setUpToDate(this, false);

        // update past inspection info
        if (!pastInspectionsInfo.isUpdating(this)){
            startService(new Intent(this, FetchPIIntentService.class));
        }
        */

        // broadcast that the upload is complete
        sendBroadcast(new Intent().setAction(Params.UPLOAD_COMPLETE));
    }

    @Override
    public IBinder onBind(Intent intent) {
        // no binding, so return null
        return null;
    }

}