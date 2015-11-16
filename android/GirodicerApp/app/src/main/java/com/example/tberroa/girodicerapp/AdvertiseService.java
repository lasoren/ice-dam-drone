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

    private Context applicationContext;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeAdvertiser bluetoothLeAdvertiser;
    private AdvertiseCallback advertiseCallback;

    @Override
    public void onCreate() {
        // initialize Bluetooth Manager
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        // initialize Bluetooth Adapter
        bluetoothAdapter = bluetoothManager.getAdapter();
        // initialize Bluetooth LE Advertiser
        bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        // get activityContext
        applicationContext = this.getApplicationContext();
        // update global variable, service is running
        ServiceStatus service_status = new ServiceStatus(applicationContext);
        service_status.setServiceStatusTrue(this);
    }

    @Override
    public void onDestroy() {
        if (bluetoothAdapter.isMultipleAdvertisementSupported()){ // if advertising
            // stop advertising
            bluetoothLeAdvertiser.stopAdvertising(advertiseCallback);
        }
        // update global variable, service has ended
        ServiceStatus service_status = new ServiceStatus(applicationContext);
        service_status.setServiceStatusFalse(this);
        // print message to user, service ended
        Toast.makeText(this, "service ended", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // start service and print message to user
        if (bluetoothAdapter.isMultipleAdvertisementSupported()){  // if advertising is supported
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
        bluetoothAdapter.setName("GiroApp");   // 8 bytes, up to 8 characters (1 byte per character)
        dataBuilder.setIncludeDeviceName(true);
        dataBuilder.setIncludeTxPowerLevel(true);   // 3 bytes
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

        bluetoothLeAdvertiser.startAdvertising(settingsBuilder.build(), dataBuilder.build(), advertiseCallback);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // no binding, so return null
        return null;
    }
}
