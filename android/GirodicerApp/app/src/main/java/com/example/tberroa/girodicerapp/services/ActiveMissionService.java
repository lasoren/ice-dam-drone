package com.example.tberroa.girodicerapp.services;

import android.app.DownloadManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.example.tberroa.girodicerapp.data.PreviousMissionsInfo;
import com.example.tberroa.girodicerapp.data.ServiceStatus;
import com.example.tberroa.girodicerapp.helpers.Utilities;

import java.io.File;
import java.util.UUID;

public class ActiveMissionService extends Service {

    private DownloadManager downloadManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeAdvertiser bluetoothLeAdvertiser;
    private AdvertiseCallback advertiseCallback;
    private String username;
    private int missionNumber;
    private int numberOfAerials;
    private int numberOfThermals;
    private int numberOfIceDams;
    private int numberOfSalts;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // update service status, service is running
        new ServiceStatus().setServiceStatus(getApplicationContext(), true);

        // grab username and mission number
        username = intent.getExtras().getString("username");
        missionNumber = intent.getExtras().getInt("mission_number");

        // initialize number of images to zero
        numberOfAerials = 0;
        numberOfThermals = 0;
        numberOfIceDams = 0;
        numberOfSalts = 0;

        // check if BLE advertising is supported
        if (bluetoothAdapter.isMultipleAdvertisementSupported()){  // if advertising is supported
            startAdvertise(); // start BLE advertising
            Toast.makeText(this,
                    "mission service started, BLE advertising supported",
                    Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "mission service started, BLE advertising not supported",
                    Toast.LENGTH_SHORT).show();
        }

        // Begin downloading
        new Thread(new Runnable() {
            @Override
            public void run() {
                String uriStart = "https://s3.amazonaws.com/missionphotos/Flight+1/";
                for(int i=1; i<=4; i++){
                    String uriNext, directoryEnd, fileStart;
                    switch(i){
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
                    for (int j=1; j<=5; j++){
                        String uriEnd = Integer.toString(j)+".jpg";
                        String fileName = fileStart+Integer.toString(j)+".jpg";

                        downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
                        Uri Download_Uri = Uri.parse(uriStart+uriNext+uriEnd);
                        DownloadManager.Request request = new DownloadManager.Request(Download_Uri);

                        //Restrict the types of networks over which this download may proceed.
                        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                        //Set whether this download may proceed over a roaming connection.
                        request.setAllowedOverRoaming(false);
                        //Set the title of this download, to be displayed in notifications (if enabled).
                        request.setTitle("Girodicer Image Transfer");
                        //Set a description of this download, to be displayed in notifications (if enabled)
                        request.setDescription("In process of receiving images from drone");
                        //Set the local destination for the downloaded file to a path within the application's external files directory
                        String directory = "/Girodicer/"+username+"/Mission"+missionNumber+"/"+directoryEnd;
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES+directory,fileName);

                        //Enqueue a new download
                        downloadManager.enqueue(request);

                        // log the image count
                        switch(i){
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
                }
            }
        }).start();
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        // initialize Bluetooth Manager
        BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        // initialize Bluetooth Adapter
        bluetoothAdapter = bluetoothManager.getAdapter();
        // initialize Bluetooth LE Advertiser
        bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
    }

    @Override
    public void onDestroy() {
        if (bluetoothAdapter.isMultipleAdvertisementSupported()){ // if advertising
            // stop advertising
            bluetoothLeAdvertiser.stopAdvertising(advertiseCallback);
        }

        // update previous missions info, data is no longer up to date
        new PreviousMissionsInfo().setUpToDate(getApplicationContext(), false);

        // update service status, service is no longer running
        new ServiceStatus().setServiceStatus(getApplicationContext(), false);

        // print message to user, service ended
        Toast.makeText(this, "mission service has ended", Toast.LENGTH_SHORT).show();

        // upload the images that were transferred
        Utilities.uploadCurrentMissionData(this, username, missionNumber, numberOfAerials,
                numberOfThermals, numberOfIceDams, numberOfSalts);
    }

    public void startAdvertise() {
        // build settings
        AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
        settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
        settingsBuilder.setConnectable(false);
        settingsBuilder.setTimeout(0);
        settingsBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);

        // build data
        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        bluetoothAdapter.setName("GiroApp"); // 8 bytes, up to 8 characters (1 byte per character)
        dataBuilder.setIncludeDeviceName(true);
        dataBuilder.setIncludeTxPowerLevel(true); // 3 bytes
        ParcelUuid uuid = new ParcelUuid(UUID.fromString("2949c320-870e-11e5-a837-0800200c9a66"));
        dataBuilder.addServiceUuid(uuid);

        // set callback
        advertiseCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
            }

            @Override
            public void onStartFailure(int errorCode) {
                super.onStartFailure(errorCode);
            }
        };

        // start advertising
        bluetoothLeAdvertiser
                .startAdvertising(settingsBuilder.build(), dataBuilder.build(), advertiseCallback);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // no binding, so return null
        return null;
    }
}
