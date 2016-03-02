package com.example.tberroa.girodicerapp.services;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.data.ActiveInspectionInfo;
import com.example.tberroa.girodicerapp.helpers.Utilities;

import java.io.File;

// class to receive and store images received from drone
// for now this class will simply download images from the internet
public class ImageTransferIntentService extends IntentService {

    public ImageTransferIntentService() {
        super("ImageTransferIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // inspection in transfer phase, phase=2
        ActiveInspectionInfo activeInspectionInfo = new ActiveInspectionInfo();
        activeInspectionInfo.setPhase(this, 2);

        // broadcast that transfer phase has begun
        Intent transferStarted = new Intent();
        transferStarted.setAction(Params.TRANSFER_STARTED);
        sendBroadcast(transferStarted);

        // get inspection id
        int inspectionId = activeInspectionInfo.getInspectionId(this);

        // get today's date (TEST CODE, date format will be different in live code)
        //String date = new SimpleDateFormat("MM/dd/yyyy").format(new Date());

        // initialize number of images to zero
        int numberOfAerials = 0, numberOfThermals = 0, numberOfIceDams = 0, numberOfSalts = 0;

        // store the different image types in an array
        String imageType[] = {"aerial", "thermal", "icedam", "salt"};

        // 5 potential images per type in this test, but in the real environment
        // I wouldn't know the number of images per type until they all came in from the drone
        String imageNumber[] = {"1", "2", "3", "4", "5"};

        // download and save one image at a time (TEST CODE!!, always copying inspection 1)
        long lastDownload = 0;
        for (String type: imageType) { // loop 4 times, once per image type
            for (String num: imageNumber) { // loop 5 times (up to 5 images per type for test)
                // name of image file
                String imageName = type+num+".jpg";

                // name of image including folder prefixes up to username
                String key = Utilities.constructImageKey(inspectionId, imageName);

                // users local directory for pictures
                String directory = Environment.DIRECTORY_PICTURES;

                // path to image within users local storage
                String path = Params.HOME_FOLDER+key;

                // check if a file already exists at that location
                File file = Environment.getExternalStoragePublicDirectory(directory+path);
                if (!file.exists()){ // if the file does not already exist
                    // initialize download manager
                    DownloadManager dM = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

                    // construct  uri
                    Uri uri = Uri.parse(Utilities.constructImageURL(1, imageName));

                    // initialize the download request
                    DownloadManager.Request request = new DownloadManager.Request(uri);

                    // set details regarding the download
                    request.setDestinationInExternalPublicDir(directory, path);
                    request.setAllowedOverMetered(false);
                    request.setAllowedOverRoaming(false);
                    request.setTitle(getResources().getString(R.string.download_title));
                    request.setDescription(getResources().getString(R.string.download_description));

                    // enqueue a new download
                    lastDownload = dM.enqueue(request);
                }
                // log the image count (always 5 in this test set up)
                switch (type) {

                    case "aerial":
                        numberOfAerials++;
                        break;
                    case "thermal":
                        numberOfThermals++;
                        break;
                    case "icedam":
                        numberOfIceDams++;
                        break;
                    case "salt":
                        numberOfSalts++;
                        break;
                    default:
                        break;
                }
            }
            // save id of last download
            activeInspectionInfo.setLastDownload(this, lastDownload);
        }
        // save the inspection images count
        activeInspectionInfo.setAerialCount(this, numberOfAerials);
        activeInspectionInfo.setThermalCount(this, numberOfThermals);
        activeInspectionInfo.setIceDamCount(this, numberOfIceDams);
        activeInspectionInfo.setSaltCount(this, numberOfSalts);

        // broadcast that the transfer is complete
        sendBroadcast(new Intent().setAction(Params.TRANSFER_COMPLETE));

    }
}
