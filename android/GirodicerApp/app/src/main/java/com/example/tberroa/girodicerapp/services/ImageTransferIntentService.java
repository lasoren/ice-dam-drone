package com.example.tberroa.girodicerapp.services;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import com.example.tberroa.girodicerapp.activities.ActiveMissionActivity;

// class to store images received from drone
// for now this class will simply download images from the internet
public class ImageTransferIntentService extends IntentService {

    public ImageTransferIntentService() {
        super("ImageTransferIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // grab username and mission number
        String username = intent.getExtras().getString("username");
        int missionNumber = intent.getExtras().getInt("mission_number");

        // initialize number of images to zero
        int numberOfAerials = 0, numberOfThermals = 0, numberOfIceDams = 0, numberOfSalts = 0;

        // beginning of download uri
        String uriStart = "https://s3.amazonaws.com/missionphotos/Flight+1/";
        for (int i = 1; i <= 4; i++) {
            String uriNext, directoryEnd, fileStart;
            // next portion of path based on image type
            switch (i) {
                case 1:
                    uriNext = "Aerial/aerial";
                    directoryEnd = "Aerial/";
                    fileStart = "aerial";
                    break;
                case 2:
                    uriNext = "Thermal/thermal";
                    directoryEnd = "Thermal/";
                    fileStart = "thermal";
                    break;
                case 3:
                    uriNext = "IceDam/icedam";
                    directoryEnd = "IceDam/";
                    fileStart = "icedam";
                    break;
                case 4:
                    uriNext = "Salt/salt";
                    directoryEnd = "Salt/";
                    fileStart = "salt";
                    break;
                default:
                    uriNext = "Aerial/aerial";
                    directoryEnd = "Aerial/";
                    fileStart = "aerial";
                    break;
            }
            for (int j = 1; j <= 5; j++) {
                String uriEnd = Integer.toString(j) + ".jpg";
                String fileName = fileStart + Integer.toString(j) + ".jpg";

                DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                Uri downloadUri = Uri.parse(uriStart + uriNext + uriEnd);
                DownloadManager.Request request = new DownloadManager.Request(downloadUri);

                //Restrict the types of networks over which this download may proceed.
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                //Set whether this download may proceed over a roaming connection.
                request.setAllowedOverRoaming(false);
                //Set the title of this download, to be displayed in notifications (if enabled).
                request.setTitle("Girodicer Image Transfer");
                //Set a description of this download, to be displayed in notifications (if enabled)
                request.setDescription("In process of receiving images from drone");
                //Set the local destination for the downloaded file to a path within the application's external files directory
                String directory = "/Girodicer/" + username + "/Mission" + missionNumber + "/" + directoryEnd;
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES + directory, fileName);

                //Enqueue a new download
                downloadManager.enqueue(request);

                // log the image count
                switch (i) {
                    case 1:
                        numberOfAerials++;
                        break;
                    case 2:
                        numberOfThermals++;
                        break;
                    case 3:
                        numberOfIceDams++;
                        break;
                    case 4:
                        numberOfSalts++;
                        break;
                    default:
                        break;
                }
            }

            // create bundle
            Bundle bundle = new Bundle();
            bundle.putString("username", username);
            bundle.putInt("mission_number", missionNumber);
            bundle.putInt("number_of_aerials", numberOfAerials);
            bundle.putInt("number_of_thermals", numberOfThermals);
            bundle.putInt("number_of_icedams", numberOfIceDams);
            bundle.putInt("number_of_salts", numberOfSalts);

            // broadcast that the service is complete and pass bundle
            Intent broadcastIntent = new Intent("TRANSFER_COMPLETE");
            broadcastIntent.putExtras(bundle);
            sendBroadcast(broadcastIntent);
        }
    }
}
