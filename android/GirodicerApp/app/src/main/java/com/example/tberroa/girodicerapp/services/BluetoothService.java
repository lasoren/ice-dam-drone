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
import com.example.tberroa.girodicerapp.data.ActiveInspectionInfo;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.helpers.ExceptionHandler;
import com.example.tberroa.girodicerapp.models.Status;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BluetoothService extends Service {

    // constants
    public static final int READ = 1;
    public static final String LOCATION = "LOCATION";
    private final int CONNECT_ATTEMPT_SUCCESS = 100;
    private final int CONNECT_ATTEMPT_FAILED = 50;
    private final UUID DRONE_UUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");

    // random variables
    private boolean droneNotFound = true;
    @SuppressWarnings("unused")
    public static Status currentStatus;
    public static ArrayList<LatLng> houseBoundary;

    // shared preference used to save state
    private final ActiveInspectionInfo activeInspectionInfo = new ActiveInspectionInfo();

    // bluetooth objects
    private final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothDevice btDevice;

    // bluetooth handlers
    public static Handler btDataHandler = new BTDataHandler();
    private final Handler btConnectHandler = new BTConnectHandler();
    private final Handler btReconnectHandler = new BTReconnectHandler();

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

                            if (device.getName().equals(getResources().getString(R.string.server_name))) {
                                btDevice = device;
                                btAdapter.cancelDiscovery();
                                droneNotFound = false;
                                pairComplete();
                            }
                        }
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        Log.d("dbg", "@BluetoothService: discovery started");

                        if (droneNotFound) {
                            // discovery finished without finding the drone, let system know of connection failure
                            activeInspectionInfo.setPhase(BluetoothService.this, -5);
                            sendBroadcast(new Intent().setAction(Params.DRONE_CONNECT_FAILURE));
                        }
                        break;
                    case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                        Log.d("dbg", "@BluetoothService: connection lost");

                        // let system know the connection was lost
                        activeInspectionInfo.setPhase(BluetoothService.this, -4);
                        sendBroadcast(new Intent().setAction(Params.DRONE_CONNECTION_LOST));

                        // attempt to reconnect
                        attemptToConnect(btReconnectHandler);
                }
            }
        };
        registerReceiver(btReceiver, btFilter);

        // begin bluetooth connection process
        if (btAdapter.isEnabled()) {
            // let system know we are trying to connect to the drone
            activeInspectionInfo.setPhase(BluetoothService.this, -7);
            sendBroadcast(new Intent().setAction(Params.CONNECTING_TO_DRONE));

            pair();
        } else {
            Log.d("dbg", "@BluetoothService: bluetooth adapter not enabled, unable to pair");
            sendBroadcast(new Intent().setAction(Params.BLUETOOTH_NOT_ENABLED));
        }
    }

    // checks if drone is already paired, if not, begins discovery
    private void pair() {
        Log.d("dbg", "@BluetoothService: beginning of pair()");

        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals(getResources().getString(R.string.server_name))) {
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
        attemptToConnect(btConnectHandler);
    }

    // works in a background thread so it requires a handler to handle completion logic
    private void attemptToConnect(final Handler handler) {
        new Thread(new Runnable() {
            public void run() {
                // cancel discovery
                btAdapter.cancelDiscovery();

                // initialize bluetooth socket
                BluetoothSocket btSocket = null;
                try {
                    btSocket = btDevice.createRfcommSocketToServiceRecord(DRONE_UUID);
                } catch (IOException e) {
                    new ExceptionHandler().HandleException(e);
                }

                // try connecting
                if (btSocket != null) {
                    try {
                        btSocket.connect();
                    } catch (IOException connectException) {
                        try {
                            btSocket.close();
                            handler.obtainMessage(CONNECT_ATTEMPT_FAILED).sendToTarget();
                        } catch (IOException closeException) {
                            handler.obtainMessage(CONNECT_ATTEMPT_FAILED).sendToTarget();
                        }
                        return;
                    }
                }
                handler.obtainMessage(CONNECT_ATTEMPT_SUCCESS, -1, -1, btSocket).sendToTarget();
            }
        }).start();
    }

    // unregisters broadcast receiver which was initialized in onCreate()
    @Override
    public void onDestroy() {
        Log.d("dbg", "@BluetoothService: service destroyed");

        activeInspectionInfo.setPhase(BluetoothService.this, 0);
        currentStatus = null;
        btConnectionThread.shutdown();
        unregisterReceiver(btReceiver);
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

    // not used so returns null
    @Override
    public IBinder onBind(Intent intent) {
        // no binding, so return null
        return null;
    }

    // handler for initial connection attempt
    @SuppressLint("HandlerLeak")
    private class BTConnectHandler extends Handler {
        @Override
        public void handleMessage(Message incoming) {
            switch (incoming.what) {
                case CONNECT_ATTEMPT_SUCCESS: // successfully connected to drone via bluetooth device
                    Log.d("dbg", "@BluetoothService: connect attempt successful");

                    // broadcast success
                    activeInspectionInfo.setPhase(BluetoothService.this, -6);
                    sendBroadcast(new Intent().setAction(Params.DRONE_CONNECT_SUCCESS));

                    // establish bluetooth connection via bluetooth socket
                    BluetoothSocket btSocket = (BluetoothSocket) incoming.obj;
                    btConnectionThread = new ConnectionThread(btSocket, new Messenger(btDataHandler));
                    btConnectionThread.start();
                    break;
                case CONNECT_ATTEMPT_FAILED:
                    Log.d("dbg", "@BluetoothService: connect attempt failed");

                    // broadcast failure
                    activeInspectionInfo.setPhase(BluetoothService.this, -5);
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
                                currentStatus = (Status) received.read();
                                break;
                            case GProtocol.COMMAND_SEND_PATH:
                                // noinspection unchecked
                                houseBoundary = (ArrayList<LatLng>) received.read();

                                // broadcast so MapActivity knows that the points are ready
                                if (context != null){
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

        public static void passContext(Context c){
            context = c;
        }
    }

    // handler for any reconnection attempts
    @SuppressLint("HandlerLeak")
    private class BTReconnectHandler extends Handler {
        @Override
        public void handleMessage(Message incoming) {
            switch (incoming.what) {
                case CONNECT_ATTEMPT_SUCCESS:
                    Log.d("dbg", "@BluetoothService: reconnect attempt successful");

                    // broadcast success
                    activeInspectionInfo.setPhase(BluetoothService.this, -3);
                    sendBroadcast(new Intent().setAction(Params.DRONE_RECONNECT_SUCCESS));

                    // establish bluetooth connection via bluetooth socket
                    BluetoothSocket btSocket = (BluetoothSocket) incoming.obj;
                    btConnectionThread = new ConnectionThread(btSocket, new Messenger(btDataHandler));
                    btConnectionThread.start();
                    break;
                case CONNECT_ATTEMPT_FAILED:
                    Log.d("dbg", "@BluetoothService: reconnect attempt failed");

                    // broadcast failure
                    activeInspectionInfo.setPhase(BluetoothService.this, -2);
                    sendBroadcast(new Intent().setAction(Params.DRONE_RECONNECT_FAILURE));

                    // end bluetooth service
                    stopSelf();
            }
        }
    }
}
