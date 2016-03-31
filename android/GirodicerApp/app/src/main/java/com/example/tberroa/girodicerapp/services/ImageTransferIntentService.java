package com.example.tberroa.girodicerapp.services;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.data.CurrentInspectionInfo;

import java.io.File;

// class to receive and store images received from drone
// for now this class will simply download images from the internet
public class ImageTransferIntentService extends IntentService {

    public ImageTransferIntentService() {
        super("ImageTransferIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // update phase
        CurrentInspectionInfo currentInspectionInfo = new CurrentInspectionInfo();
        currentInspectionInfo.setPhase(this, Params.CI_DATA_TRANSFER);

        // broadcast that transfer phase has begun
        sendBroadcast(new Intent().setAction(Params.TRANSFER_STARTED));

        // initialize number of images to zero
        int numberOfAerials = 0, numberOfThermals = 0, numberOfRoofEdges = 0;

        // store the 3 different image types in an array
        int imageType[] = {Params.I_TYPE_ROOF_EDGE, Params.I_TYPE_THERMAL, Params.I_TYPE_AERIAL};

        // download and save one image at a time (TEST CODE!!, this whole service is test code)
        long lastDownload = 0;
        for (int type : imageType) { // loop per image type
            for (int i = 0; i < 5; i++) { // loop 5 times (5 test images per type)
                String iString = Integer.toString(i);
                String typeString = Integer.toString(type);

                // users local directory for pictures
                String directory = Environment.DIRECTORY_PICTURES;

                // path to image within users local storage
                String path = Params.HOME_FOLDER + "/images/" + typeString + iString + ".jpg";

                // check if a file already exists at that location
                File file = Environment.getExternalStoragePublicDirectory(directory + path);
                if (!file.exists()) { // if the file does not already exist
                    // initialize download manager
                    DownloadManager dM = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

                    // construct  uri
                    String uriString = Params.CLOUD_URL + "0" + "/images/" + typeString + iString + ".jpg";
                    Log.d("dbg", "@ImageTransferIntentService: uri is: " + uriString);
                    Uri uri = Uri.parse(uriString);

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
                    case Params.I_TYPE_AERIAL:
                        numberOfAerials++;
                        break;
                    case Params.I_TYPE_THERMAL:
                        numberOfThermals++;
                        break;
                    case Params.I_TYPE_ROOF_EDGE:
                        numberOfRoofEdges++;
                        break;
                    default:
                        break;
                }
            }
            // save id of last download
            currentInspectionInfo.setLastDownload(this, lastDownload);
        }
        // save the inspection images count
        currentInspectionInfo.setAerialCount(this, numberOfAerials);
        currentInspectionInfo.setThermalCount(this, numberOfThermals);
        currentInspectionInfo.setRoofEdgeCount(this, numberOfRoofEdges);

        // broadcast that the transfer is complete
        sendBroadcast(new Intent().setAction(Params.TRANSFER_COMPLETE));
    }
}
