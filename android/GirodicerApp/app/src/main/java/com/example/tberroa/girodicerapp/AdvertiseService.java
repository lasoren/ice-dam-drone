package com.example.tberroa.girodicerapp;

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

import java.util.UUID;

public class AdvertiseService extends Service {

    private Context app_context;
    private BluetoothManager btManager;
    private BluetoothAdapter btAdapter;
    private BluetoothLeAdvertiser btAdvertiser;
    private AdvertiseCallback adCallback;

    @Override
    public void onCreate() {
        // initialize Bluetooth Manager
        btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        // initialize Bluetooth Adapter
        btAdapter = btManager.getAdapter();
        // initialize Bluetooth LE Advertiser
        btAdvertiser = btAdapter.getBluetoothLeAdvertiser();
        // get context
        app_context = this.getApplicationContext();
        // update global variable, service is running
        ServiceStatus service_status = new ServiceStatus(app_context);
        service_status.setServiceStatus_true(this);
    }

    @Override
    public void onDestroy() {
        if (btAdapter.isMultipleAdvertisementSupported()){ // if advertising
            // stop advertising
            btAdvertiser.stopAdvertising(adCallback);
        }
        // update global variable, service has ended
        ServiceStatus service_status = new ServiceStatus(app_context);
        service_status.setServiceStatus_false(this);
        // print message to user, service ended
        Toast.makeText(this, "service ended", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // start service and print message to user
        if (btAdapter.isMultipleAdvertisementSupported()){  // if advertising is supported
            startAdvertise();
            Toast.makeText(this,
                    "service started, BLE advertising supported", Toast.LENGTH_SHORT).show();
        }
        else{   // if advertising is not supported
            Toast.makeText(this,
                    "service started, BLE advertising not supported", Toast.LENGTH_SHORT).show();
        }
        return START_NOT_STICKY;
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
        btAdapter.setName("GiroApp");   // 8 bytes, up to 8 characters (1 byte per character)
        dataBuilder.setIncludeDeviceName(true);
        dataBuilder.setIncludeTxPowerLevel(true);   // 3 bytes
        ParcelUuid uuid = new ParcelUuid(UUID.fromString("2949c320-870e-11e5-a837-0800200c9a66"));
        dataBuilder.addServiceUuid(uuid);

        // set callback
        adCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
            }

            @Override
            public void onStartFailure(int errorCode) {
                super.onStartFailure(errorCode);
            }
        };

        btAdvertiser.startAdvertising(settingsBuilder.build(), dataBuilder.build(), adCallback);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // no binding, so return null
        return null;
    }
}
