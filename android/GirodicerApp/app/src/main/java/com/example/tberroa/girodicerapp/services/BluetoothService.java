package com.example.tberroa.girodicerapp.services;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.bluetooth.BluetoothException;
import com.example.tberroa.girodicerapp.bluetooth.ConnectionThread;
import com.example.tberroa.girodicerapp.bluetooth.GProtocol;
import com.example.tberroa.girodicerapp.data.BluetoothInfo;
import com.example.tberroa.girodicerapp.data.CurrentInspectionInfo;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.bluetooth.Status;
import com.example.tberroa.girodicerapp.database.ServerDB;
import com.example.tberroa.girodicerapp.models.Inspection;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class BluetoothService extends Service {

    // constants
    public static final int READ = 1;
    private final int CONNECT_ATTEMPT_SUCCESS = 100;
    private final int CONNECT_ATTEMPT_FAILED = 50;
    private final UUID DRONE_UUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");

    // variables
    @SuppressWarnings("unused")
    public static boolean needInitialStatus = true;
    public static ArrayList<LatLng> houseBoundary;
    public static boolean mapPhaseComplete = false;
    public static Status currentStatus;
    public static boolean serviceRunning = true;
    private boolean droneNotFound = true;
    private int clientId;

    // shared preference used to save state
    private final BluetoothInfo bluetoothInfo = new BluetoothInfo();

    // bluetooth objects
    private final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothDevice btDevice;

    // bluetooth handlers
    public static final Handler btDataHandler = new BTDataHandler();
    private final Handler btConnectHandler = new BTConnectHandler();

    // main bluetooth connection thread, this is where data is transferred
    public static ConnectionThread btConnectionThread; // so legacy code compiles

    // receiver to handle all bluetooth state changes
    private BroadcastReceiver btReceiver;

    // initializes bluetooth receiver and begins connection process
    @Override
    public void onCreate() {
        Log.d("dbg", "@BluetoothService: beginning of onCreate");

        // initialize bluetooth receiver
        IntentFilter btFilter = new IntentFilter();
        btFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        btFilter.addAction(BluetoothDevice.ACTION_FOUND);
        btFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        btFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        btReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                switch (action) {
                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                        Log.d("dbg", "@BluetoothService: discovery started");
                        break;
                    case BluetoothDevice.ACTION_FOUND:
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if (device.getName() != null) {
                            Log.d("dbg", "@BluetoothService: device found: " + device.getName());

                            if (device.getName().equals(getResources().getString(R.string.drone_bt_name))) {
                                btDevice = device;
                                if (btAdapter.isDiscovering()) {
                                    btAdapter.cancelDiscovery();
                                }
                                droneNotFound = false;
                                pairComplete();
                            }
                        }
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        Log.d("dbg", "@BluetoothService: discovery finished");

                        if (droneNotFound) {
                            Log.d("dbg", "@BluetoothService: drone not found");

                            // discovery finished without finding the drone, let system know of connection failure
                            bluetoothInfo.setState(BluetoothService.this, Params.BTS_NOT_CONNECTED);
                            bluetoothInfo.setErrorCode(BluetoothService.this, Params.BTE_CONNECT_FAILED);
                            sendBroadcast(new Intent().setAction(Params.DRONE_CONNECT_FAILURE));

                            // end bluetooth service
                            stopSelf();
                        }
                        break;
                    case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                        Log.d("dbg", "@BluetoothService: connection lost");

                        // let system know the connection was lost
                        bluetoothInfo.setState(BluetoothService.this, Params.BTS_CONNECTION_LOST);
                        sendBroadcast(new Intent().setAction(Params.DRONE_CONNECTION_LOST));

                        // attempt to reconnect
                        attemptToConnect();
                }
            }
        };
        registerReceiver(btReceiver, btFilter);

        // begin bluetooth connection process
        if (btAdapter.isEnabled()) {
            Log.d("dbg", "@BluetoothService: beginning connection process");

            // let system know we are trying to connect to the drone
            bluetoothInfo.setState(BluetoothService.this, Params.BTS_CONNECTING);
            sendBroadcast(new Intent().setAction(Params.CONNECTING_TO_DRONE));

            pair();
        } else {
            Log.d("dbg", "@BluetoothService: bluetooth adapter not enabled, unable to pair");

            // let system know that connect attempt failed because bluetooth is not enabled
            bluetoothInfo.setState(BluetoothService.this, Params.BTS_NOT_CONNECTED);
            bluetoothInfo.setErrorCode(BluetoothService.this, Params.BTE_NOT_ENABLED);
            sendBroadcast(new Intent().setAction(Params.BLUETOOTH_NOT_ENABLED));

            // end bluetooth service
            stopSelf();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        clientId = intent.getIntExtra("client_id", 0);
        return START_NOT_STICKY;
    }

    // checks if drone is already paired, if not, begins discovery
    private void pair() {
        Log.d("dbg", "@BluetoothService: beginning of pair()");

        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals(getResources().getString(R.string.drone_bt_name))) {
                    Log.d("dbg", "@BluetoothService: drone already paired");

                    btDevice = device;
                    pairComplete();
                    return;
                }
            }
        }
        btAdapter.startDiscovery();
    }

    // once drone is paired, connection attempt is made
    private void pairComplete() {
        Log.d("dbg", "@BluetoothService: beginning of pairComplete()");

        // begin connecting to drone via bluetooth device in background thread
        attemptToConnect();
    }

    // works in a background thread so it requires a handler to handle completion logic
    private void attemptToConnect() {
        new Thread(new Runnable() {
            public void run() {
                // cancel discovery
                if (btAdapter.isDiscovering()) {
                    btAdapter.cancelDiscovery();
                }

                // initialize bluetooth socket
                BluetoothSocket btSocket = null;
                try {
                    btSocket = btDevice.createRfcommSocketToServiceRecord(DRONE_UUID);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // try connecting
                if (btSocket != null) {
                    try {
                        btSocket.connect();
                    } catch (IOException connectException) {
                        try {
                            btSocket.close();
                            btConnectHandler.obtainMessage(CONNECT_ATTEMPT_FAILED).sendToTarget();
                        } catch (IOException closeException) {
                            btConnectHandler.obtainMessage(CONNECT_ATTEMPT_FAILED).sendToTarget();
                        }
                        return;
                    }
                }
                btConnectHandler.obtainMessage(CONNECT_ATTEMPT_SUCCESS, -1, -1, btSocket).sendToTarget();
            }
        }).start();
    }

    // unregisters broadcast receiver which was initialized in onCreate()
    @Override
    public void onDestroy() {
        Log.d("dbg", "@BluetoothService: service destroyed");

        // shutdown connection thread
        if (btConnectionThread != null) {
            btConnectionThread.shutdown();
        }

        // reset state
        bluetoothInfo.setState(BluetoothService.this, Params.BTS_NOT_CONNECTED);

        /*// all inspection phases but uploading to aws require bluetooth
        CurrentInspectionInfo currentInspectionInfo = new CurrentInspectionInfo();
        if (currentInspectionInfo.getPhase(this) != Params.CI_UPLOADING){
            currentInspectionInfo.setPhase(this, Params.CI_INACTIVE);
        }*/

        // update variables
        needInitialStatus = true;
        mapPhaseComplete = false;
        serviceRunning = false;
        currentStatus = null;

        // unregister receiver (this can leak because onDestroy not guaranteed, will fix later)
        unregisterReceiver(btReceiver);

        // TEST CODE
        droneDone();
    }

    // if android system kills service, onDestroy is not called. This method allows us to check if service is running
    static public boolean notRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (BluetoothService.class.getName().equals(service.service.getClassName())) {
                return false;
            }
        }
        return true;
    }

    private void droneStarted() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // create inspection on backend
                Inspection inspection = new ServerDB(BluetoothService.this).createInspection(clientId);

                if (inspection == null) { // error occurred
                    stopSelf();
                } else {
                    // save inspection locally
                    inspection.cascadeSave();

                    // inspection is now in progress
                    CurrentInspectionInfo currentInspectionInfo = new CurrentInspectionInfo();
                    currentInspectionInfo.setNotInProgress(BluetoothService.this, false);

                    // drone is active
                    currentInspectionInfo.setPhase(BluetoothService.this, Params.CI_DRONE_ACTIVE);

                    // save inspection id
                    currentInspectionInfo.setInspectionId(BluetoothService.this, inspection.id);

                    // save client id
                    currentInspectionInfo.setClientId(BluetoothService.this, clientId);
                }
            }
        }).start();
    }

    private void droneDone() {
        sendBroadcast(new Intent().setAction(Params.DRONE_DONE));
    }

    // not used so returns null
    @Override
    public IBinder onBind(Intent intent) {
        // no binding, so return null
        return null;
    }

    // handler for connection attempts
    @SuppressLint("HandlerLeak")
    private class BTConnectHandler extends Handler {
        @Override
        public void handleMessage(Message incoming) {
            switch (incoming.what) {
                case CONNECT_ATTEMPT_SUCCESS: // successfully connected to drone via bluetooth device
                    Log.d("dbg", "@BluetoothService: connect attempt successful");

                    // drone has started inspection, set initializations (TEST CODE, THIS WILL LIKELY BE MOVED/DELETED)
                    droneStarted();

                    // let system know that bluetooth was successfully connected
                    bluetoothInfo.setState(BluetoothService.this, Params.BTS_CONNECTED);
                    bluetoothInfo.setErrorCode(BluetoothService.this, Params.BTE_NO_ERROR);
                    sendBroadcast(new Intent().setAction(Params.DRONE_CONNECT_SUCCESS));

                    // establish bluetooth connection via bluetooth socket
                    BluetoothSocket btSocket = (BluetoothSocket) incoming.obj;
                    btConnectionThread = new ConnectionThread(btSocket, new Messenger(btDataHandler));
                    btConnectionThread.start();

                    // timeout after 3 seconds if current status still null (never received status signal)
                    Timer timer = new Timer();
                    TimerTask timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            if (currentStatus == null) {
                                bluetoothInfo.setErrorCode(BluetoothService.this, Params.BTE_TIMEOUT);
                                Log.d("dbg", "@BluetoothService: timed out while waiting for initial status signal");

                                // let system know of timeout
                                sendBroadcast(new Intent().setAction(Params.BLUETOOTH_TIMEOUT));

                                // end bluetooth service
                                stopSelf();
                            }
                        }
                    };
                    timer.schedule(timerTask, 3000);
                    break;
                case CONNECT_ATTEMPT_FAILED:
                    Log.d("dbg", "@BluetoothService: connect attempt failed");

                    // let system know that bluetooth connect attempt failed
                    bluetoothInfo.setState(BluetoothService.this, Params.BTS_NOT_CONNECTED);
                    bluetoothInfo.setErrorCode(BluetoothService.this, Params.BTE_CONNECT_FAILED);
                    sendBroadcast(new Intent().setAction(Params.DRONE_CONNECT_FAILURE));

                    // end bluetooth service
                    stopSelf();
            }
        }
    }

    // handler for reading data from ConnectionThread (reading data from drone)
    public static class BTDataHandler extends Handler {

        static Context context = null;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case READ:
                    Bundle bundle = msg.getData();
                    byte[] data = bundle.getByteArray(ConnectionThread.BT_DATA);
                    try {
                        GProtocol received = GProtocol.Unpack(data);
                        switch (received.getCommand()) {
                            case GProtocol.COMMAND_STATUS:
                                //Log.d("dbg", "@BluetoothService/BTDataHandler/COMMAND_STATUS");
                                currentStatus = (Status) received.read();

                                // broadcast the status update
                                if (context != null && !needInitialStatus) {
                                    context.sendBroadcast(new Intent().setAction(Params.STATUS_UPDATE));
                                }

                                // check if CurrentOneActivity is waiting for initial status
                                if (needInitialStatus) {
                                    // if so, check if the context has been sent
                                    if (context != null) {
                                        Log.d("dbg", "@BluetoothService/BTDataHandler: initial status received. broadcasting");

                                        context.sendBroadcast(new Intent().setAction(Params.INITIAL_STATUS_RECEIVED));
                                        context = null;
                                        needInitialStatus = false;
                                    }

                                }
                                break;
                            case GProtocol.COMMAND_SEND_PATH:
                                Log.d("dbg", "@BluetoothService/BTDataHandler/COMMAND_SEND_PATH");
                                // noinspection unchecked (Android Lint Suppression)
                                houseBoundary = (ArrayList<LatLng>) received.read();

                                // broadcast that the house boundary points are ready
                                if (context != null) {
                                    Log.d("dbg", "@BluetoothService/BTDataHandler: house boundary received. broadcasting");

                                    context.sendBroadcast(new Intent().setAction(Params.HOUSE_BOUNDARY_RECEIVED));
                                    context = null;
                                }

                                break;
                        }
                    } catch (BluetoothException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }

        public static void passContext(Context c) {
            context = c;
        }

        public static void destroyContext() {
            context = null;
        }
    }
}
