package com.example.tberroa.girodicerapp.services;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.example.tberroa.girodicerapp.activities.CurrentFourActivity;
import com.example.tberroa.girodicerapp.data.CurrentInspectionInfo;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.database.ServerDB;
import com.example.tberroa.girodicerapp.models.InspectionImage;
import com.example.tberroa.girodicerapp.network.CloudTools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class UploadIntentService extends IntentService {

    public static int uploadingType;
    public static int numOfAerials;
    public static int numOfThermals;
    public static int numOfRoofEdges;

    public UploadIntentService() {
        super("UploadIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // bluetooth no longer necessary
        stopService(new Intent(this, BluetoothService.class));

        // initialize constants
        CurrentInspectionInfo currentInspectionInfo = new CurrentInspectionInfo();
        String aerialType = Integer.toString(Params.I_TYPE_AERIAL);
        String thermalType = Integer.toString(Params.I_TYPE_THERMAL);
        String roofEdgeType = Integer.toString(Params.I_TYPE_ROOF_EDGE);
        int imageType[] = {Params.I_TYPE_AERIAL, Params.I_TYPE_THERMAL, Params.I_TYPE_ROOF_EDGE};
        String basePath = Environment.DIRECTORY_PICTURES + Params.HOME_FOLDER;

        // initialize static ui variables
        CurrentFourActivity.progressStatus = 0;
        CurrentFourActivity.previousUploadType = Params.I_TYPE_NOT_SPECIFIED;

        // grab data
        int inspectionId = currentInspectionInfo.getInspectionId(this);
        numOfAerials = currentInspectionInfo.getAerialCount(this);
        numOfThermals = currentInspectionInfo.getThermalCount(this);
        numOfRoofEdges = currentInspectionInfo.getRoofEdgeCount(this);

        // pack data into bundle to make iterative access simpler
        Bundle numberOfImages = new Bundle();
        numberOfImages.putInt(aerialType, numOfAerials);
        numberOfImages.putInt(thermalType, numOfThermals);
        numberOfImages.putInt(roofEdgeType, numOfRoofEdges);

        // initialize transfer utility
        TransferUtility transfer = CloudTools.getTransferUtility(UploadIntentService.this);

        // initialize array of integers, only type is needed for image creation on the backend
        List<Integer> inspectionImages = new ArrayList<>();
        for (int type : imageType) { // loop per image type
            for (int i = 0; i < numberOfImages.getInt(Integer.toString(type)); i++) { // loop per image of that type
                inspectionImages.add(type);
            }
        }

        // create the inspection images on the backend and retrieve the inspection image objects from the response
        ServerDB serverDB = new ServerDB(UploadIntentService.this);
        String taken = "2016-02-24T22:09:05Z"; // TEST CODE, placeholder
        List<InspectionImage> images = serverDB.createInspectionImages(inspectionId, inspectionImages, taken);

        // using the backend-generated path provided in the inspection image objects, upload to aws
        if (images != null) {
            int num = 0;
            for (int type : imageType) { // loop per image type
                uploadingType = type;
                for (int i = 0; i < numberOfImages.getInt(Integer.toString(type)); i++) { // loop per image of that type
                    // initialize some variables
                    InspectionImage image = images.get(num);
                    String typeString = Integer.toString(image.image_type);
                    String iString = Integer.toString(i);
                    String locationGeneric = basePath + "/images/" + typeString + iString;
                    String location = locationGeneric + ".jpg";
                    String thumbLocation = locationGeneric + "_s.jpg";  // S for small :)

                    // get image file from local directory (images are saved during image transfer phase)
                    final File file = Environment.getExternalStoragePublicDirectory(location);

                    if (file.exists()) { // check if file exists before trying to upload
                        // create thumbnail image from full size image to be uploaded to AWS
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                        Bitmap thumbImage = ThumbnailUtils.extractThumbnail(bitmap, Params.THUMB_SIZE, Params.THUMB_SIZE);

                        try {
                            File thumbFile = Environment.getExternalStoragePublicDirectory(thumbLocation);

                            if (!thumbFile.createNewFile()) {
                                Log.d(Params.TAG_DBG, "@UploadIntentService: thumbFile not created");
                            }

                            // convert bitmap to byte array
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            thumbImage.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                            byte[] bitMapData = bos.toByteArray();

                            FileOutputStream fos = new FileOutputStream(thumbFile);
                            fos.write(bitMapData);
                            fos.flush();
                            fos.close();

                            // upload thumbnail image
                            TransferObserver observer; // used to monitor upload status
                            observer = transfer.upload(Params.CLOUD_BUCKET_NAME,
                                    image.path + "_s.jpg", thumbFile);
                            boolean notDone = true;
                            while (notDone) {
                                try {
                                    Thread.sleep(250);
                                    observer.refresh();
                                    if (observer.getState() == TransferState.COMPLETED) {
                                        Log.d(Params.TAG_DBG, "@UploadIntentService: image upload complete");
                                        notDone = false;
                                        if (!thumbFile.delete()) {
                                            try {
                                                throw new Exception("Cannot delete file");
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (Exception e) {
                            Log.e(Params.TAG_EXCEPTION, "@UploadIntentService: FAILED TO CREATE THUMBNAIL IMAGE", e);
                        }

                        // upload image
                        TransferObserver observer; // used to monitor upload status
                        observer = transfer.upload(Params.CLOUD_BUCKET_NAME, image.path + ".jpg", file);
                        boolean notDone = true;
                        while (notDone) {
                            try {
                                Thread.sleep(250);
                                observer.refresh();
                                if (observer.getState() == TransferState.COMPLETED) {
                                    Log.d(Params.TAG_DBG, "@UploadIntentService: image upload complete");
                                    notDone = false;
                                    if (!file.delete()) {
                                        try {
                                            throw new Exception("Cannot delete file");
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    // save image locally
                                    image.save();

                                    // broadcast the successful upload
                                    sendBroadcast(new Intent().setAction(Params.IMAGE_UPLOAD_COMPLETE));
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    num++;
                }
            }
        }
        Log.d(Params.TAG_DBG, "@UploadIntentService: all images are done being uploaded");

        // uploading is complete, delete local file directory
        File directory = Environment.getExternalStoragePublicDirectory(basePath);
        deleteDirectory(directory);

        // inspection is no longer in progress
        currentInspectionInfo.setInProgress(UploadIntentService.this, false);

        // post inspection processing just concluded, inspection is over
        currentInspectionInfo.setPhase(UploadIntentService.this, Params.CI_INACTIVE);

        // broadcast that the upload is complete
        sendBroadcast(new Intent().setAction(Params.INSPECTION_COMPLETE));

    }

    private void deleteDirectory(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteDirectory(child);
            }
        }
        boolean success = false;
        while (!success) {
            success = fileOrDirectory.delete();
        }
    }
}