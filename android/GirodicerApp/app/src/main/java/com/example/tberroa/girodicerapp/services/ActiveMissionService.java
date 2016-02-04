package com.example.tberroa.girodicerapp.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
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

import java.io.File;
import java.util.UUID;

public class ActiveMissionService extends Service {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeAdvertiser bluetoothLeAdvertiser;
    private AdvertiseCallback advertiseCallback;
    private String username;
    private int missionNumber;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // update service status, service is running
        new ServiceStatus().setServiceStatus(getApplicationContext(), true);

        // grab username and mission number
        username = intent.getExtras().getString("username");
        missionNumber = intent.getExtras().getInt("mission_number");

        // start service and print message to user
        if (bluetoothAdapter.isMultipleAdvertisementSupported()){  // if advertising is supported
            startAdvertise(); // also start BLE advertising
            Toast.makeText(this,
                    "mission service started, BLE advertising supported",
                    Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "mission service started, BLE advertising not supported",
                    Toast.LENGTH_SHORT).show();
        }
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

        // mission over, time to upload images
        // initialize the Amazon credentials provider, AmazonS3 client, and transfer utility
        CognitoCachingCredentialsProvider credentialsProvider =
                new CognitoCachingCredentialsProvider(getApplicationContext(),
                        "us-east-1:d64bdcf1-1d5e-441e-ba35-0a3876e4c82c", Regions.US_EAST_1);
        AmazonS3 s3Client = new AmazonS3Client(credentialsProvider);
        TransferUtility transferUtility = new TransferUtility(s3Client, getApplicationContext());

        // upload the images
        String keyName = username+"/Mission "+missionNumber+"/Aerial/aerial1.jpg";
        File fileName = new File("/storage/emulated/0/Pictures/Screenshots/screenshot1.png");
        transferUtility.upload("girodicer", keyName, fileName);

        // update previous missions info, data is no longer up to date
        new PreviousMissionsInfo().setUpToDate(getApplicationContext(), false);

        // update service status, service is no longer running
        new ServiceStatus().setServiceStatus(getApplicationContext(), false);

        // print message to user, service ended
        Toast.makeText(this, "mission service has ended", Toast.LENGTH_SHORT).show();
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
