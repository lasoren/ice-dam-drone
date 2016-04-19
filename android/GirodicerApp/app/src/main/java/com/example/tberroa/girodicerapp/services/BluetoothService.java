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
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.activities.CurrentThreeActivity;
import com.example.tberroa.girodicerapp.bluetooth.BluetoothException;
import com.example.tberroa.girodicerapp.bluetooth.ConnectionThread;
import com.example.tberroa.girodicerapp.bluetooth.GProtocol;
import com.example.tberroa.girodicerapp.bluetooth.ImageDetails;
import com.example.tberroa.girodicerapp.bluetooth.Images;
import com.example.tberroa.girodicerapp.bluetooth.JSON;
import com.example.tberroa.girodicerapp.data.BluetoothInfo;
import com.example.tberroa.girodicerapp.data.ClientId;
import com.example.tberroa.girodicerapp.data.CurrentInspectionInfo;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.bluetooth.Status;
import com.example.tberroa.girodicerapp.database.ServerDB;
import com.example.tberroa.girodicerapp.fragments.DroneMapFragment;
import com.example.tberroa.girodicerapp.models.Inspection;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
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
    public static LatLng home;
    public static boolean serviceRunning = true;
    public static List<LatLng> iceDamPoints;
    public static boolean iceDamPointsReady = false;
    private static int clientId;
    private boolean droneNotFound = true;


    // shared preference used to save state
    private final BluetoothInfo bluetoothInfo = new BluetoothInfo();

    // bluetooth objects
    private final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothDevice btDevice;

    // bluetooth handlers
    private static final Handler btDataHandler = new BTDataHandler();
    private final Handler btConnectHandler = new BTConnectHandler();

    // main bluetooth connection thread, this is where data is transferred
    public static ConnectionThread btConnectionThread;

    // receiver to handle all bluetooth state changes
    public static BroadcastReceiver btReceiver = null;

    // initializes bluetooth receiver and begins connection process
    @Override
    public void onCreate() {
        Log.d(Params.TAG_DBG + Params.TAG_BT, "@BluetoothService: beginning of onCreate");

        // save client id
        clientId = new ClientId().get(this);

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
                        Log.d(Params.TAG_DBG + Params.TAG_BT, "@BluetoothService: discovery started");
                        break;
                    case BluetoothDevice.ACTION_FOUND:
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if (device.getName() != null) {
                            Log.d(Params.TAG_DBG + Params.TAG_BT, "@BluetoothService: device found: " + device.getName());

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
                        Log.d(Params.TAG_DBG + Params.TAG_BT, "@BluetoothService: discovery finished");

                        if (droneNotFound) {
                            Log.d(Params.TAG_DBG + Params.TAG_BT, "@BluetoothService: drone not found");

                            // discovery finished without finding the drone, let system know of connection failure
                            bluetoothInfo.setState(BluetoothService.this, Params.BTS_NOT_CONNECTED);
                            bluetoothInfo.setErrorCode(BluetoothService.this, Params.BTE_CONNECT_FAILED);
                            sendBroadcast(new Intent().setAction(Params.DRONE_CONNECT_FAILURE));

                            // end bluetooth service
                            stopSelf();
                        }
                        break;
                    case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                        Log.d(Params.TAG_DBG + Params.TAG_BT, "@BluetoothService: connection lost");

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
            Log.d(Params.TAG_DBG + Params.TAG_BT, "@BluetoothService: beginning connection process");

            // let system know we are trying to connect to the drone
            bluetoothInfo.setState(BluetoothService.this, Params.BTS_CONNECTING);
            sendBroadcast(new Intent().setAction(Params.CONNECTING_TO_DRONE));

            pair();
        } else {
            Log.d(Params.TAG_DBG + Params.TAG_BT, "@BluetoothService: bluetooth adapter not enabled, unable to pair");

            // let system know that connect attempt failed because bluetooth is not enabled
            bluetoothInfo.setState(BluetoothService.this, Params.BTS_NOT_CONNECTED);
            bluetoothInfo.setErrorCode(BluetoothService.this, Params.BTE_NOT_ENABLED);
            sendBroadcast(new Intent().setAction(Params.DRONE_CONNECT_FAILURE));

            // end bluetooth service
            stopSelf();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    // checks if drone is already paired, if not, begins discovery
    private void pair() {
        Log.d(Params.TAG_DBG + Params.TAG_BT, "@BluetoothService: beginning of pair()");

        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals(getResources().getString(R.string.drone_bt_name))) {
                    Log.d(Params.TAG_DBG + Params.TAG_BT, "@BluetoothService: drone already paired");

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
        Log.d(Params.TAG_DBG + Params.TAG_BT, "@BluetoothService: beginning of pairComplete()");

        // begin connecting to drone via bluetooth device in background thread
        attemptToConnect();
    }

    // works in a background thread so it requires a handler to handle completion logic
    private void attemptToConnect() {
        new Thread(new Runnable() {
            public void run() {
                // cancel discovery
                btAdapter.cancelDiscovery();

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
                        Log.d(Params.TAG_DBG + Params.TAG_BT, connectException.toString());
                        Log.d(Params.TAG_DBG + Params.TAG_BT, "socket couldn't connect");

                        // try closing socket
                        try {
                            btSocket.close();
                        } catch (IOException closeException) {
                            Log.d(Params.TAG_DBG + Params.TAG_BT, closeException.toString());
                            Log.d(Params.TAG_DBG + Params.TAG_BT, "socket couldn't close");
                        }

                        btConnectHandler.obtainMessage(CONNECT_ATTEMPT_FAILED).sendToTarget();
                        return;
                    }

                    btConnectHandler.obtainMessage(CONNECT_ATTEMPT_SUCCESS, -1, -1, btSocket).sendToTarget();
                } else {
                    Log.d(Params.TAG_DBG + Params.TAG_BT, "socket null");
                    btConnectHandler.obtainMessage(CONNECT_ATTEMPT_FAILED).sendToTarget();
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        Log.d(Params.TAG_DBG + Params.TAG_BT, "@BluetoothService: service destroyed");

        // shutdown connection thread
        if (btConnectionThread != null) {
            btConnectionThread.shutdown();
        }

        // reset state
        bluetoothInfo.setState(BluetoothService.this, Params.BTS_NOT_CONNECTED);
        CurrentInspectionInfo currentInspectionInfo = new CurrentInspectionInfo();
        currentInspectionInfo.setPhase(this, Params.CI_INACTIVE);
        currentInspectionInfo.setNotInProgress(this, true);

        // update variables
        needInitialStatus = true;
        mapPhaseComplete = false;
        serviceRunning = false;
        currentStatus = null;

        // destroy context reference from bluetooth data handler
        BTDataHandler.destroyContext();

        // unregister receiver
        if (btReceiver != null) {
            unregisterReceiver(btReceiver);
            btReceiver = null;
        }
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

    // handler for connection attempts
    @SuppressLint("HandlerLeak")
    private class BTConnectHandler extends Handler {
        @Override
        public void handleMessage(Message incoming) {
            switch (incoming.what) {
                case CONNECT_ATTEMPT_SUCCESS: // successfully connected to drone via bluetooth device
                    Log.d(Params.TAG_DBG + Params.TAG_BT, "@BluetoothService: connect attempt successful");

                    // let system know that bluetooth was successfully connected
                    bluetoothInfo.setState(BluetoothService.this, Params.BTS_CONNECTED);
                    bluetoothInfo.setErrorCode(BluetoothService.this, Params.BTE_NO_ERROR);
                    sendBroadcast(new Intent().setAction(Params.DRONE_CONNECT_SUCCESS));

                    // establish bluetooth connection via bluetooth socket
                    BluetoothSocket btSocket = (BluetoothSocket) incoming.obj;
                    btConnectionThread = new ConnectionThread(btSocket, new Messenger(btDataHandler));
                    btConnectionThread.start();

                    // timeout after 10 seconds if current status still null (never received status signal)
                    Timer timer = new Timer();
                    TimerTask timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            if (currentStatus == null) {
                                bluetoothInfo.setErrorCode(BluetoothService.this, Params.BTE_TIMEOUT);
                                Log.d(Params.TAG_DBG + Params.TAG_BT, "@BluetoothService: timed out while waiting for initial status signal");

                                // let system know of timeout
                                sendBroadcast(new Intent().setAction(Params.BLUETOOTH_TIMEOUT));

                                // end bluetooth service
                                stopSelf();
                            }
                        }
                    };
                    timer.schedule(timerTask, 10000);
                    break;
                case CONNECT_ATTEMPT_FAILED:
                    Log.d(Params.TAG_DBG + Params.TAG_BT, "@BluetoothService: connect attempt failed");

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

        private static Context context = null;
        private final List<GProtocol> listGProtocol = new ArrayList<>();
        private int imgIndexRGB = 0;
        private int imgIndexTherm = 0;
        private List<ImageDetails> imageDetailsList;
        private boolean saltingPhaseImages = true;
        private final String basePath = Environment.DIRECTORY_PICTURES + Params.HOME_FOLDER + "/images/";
        private final CurrentInspectionInfo currentInspectionInfo = new CurrentInspectionInfo();

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
                                //Log.d(Params.TAG_DBG + Params.TAG_DS, "@BluetoothService/BTDataHandler/COMMAND_STATUS");
                                currentStatus = (Status) received.read();

                                // broadcast the status update
                                if (context != null && !needInitialStatus) {
                                    context.sendBroadcast(new Intent().setAction(Params.STATUS_UPDATE));
                                }

                                // check if CurrentOneActivity is waiting for initial status
                                if (needInitialStatus) {
                                    // if so, check if the context has been sent
                                    if (context != null) {
                                        Log.d(Params.TAG_DBG + Params.TAG_DS, "@BS/DH/STATUS: initial status received, broadcasting");
                                        context.sendBroadcast(new Intent().setAction(Params.INITIAL_STATUS_RECEIVED));
                                        needInitialStatus = false;
                                    }

                                }
                                break;

                            case GProtocol.COMMAND_BLUETOOTH_SEND_PATH:
                                Log.d(Params.TAG_DBG + Params.TAG_DS, "@BS/DH/PATH");
                                // noinspection unchecked (Android Lint Suppression)
                                houseBoundary = (ArrayList<LatLng>) received.read();

                                // broadcast that the house boundary points are ready
                                if (context != null) {
                                    Log.d(Params.TAG_DBG + Params.TAG_DS, "@BS/DH/PATH: broadcasting");
                                    context.sendBroadcast(new Intent().setAction(Params.HOUSE_BOUNDARY_RECEIVED));
                                }
                                break;

                            case GProtocol.COMMAND_START_INSPECTION:
                                Log.d(Params.TAG_DBG + Params.TAG_DS, "@BS/DH/START_INSPECTION");

                                // initial RGB images will be related to the salting phase
                                saltingPhaseImages = true;

                                // attempt to start inspection
                                if (context != null) {
                                    Log.d(Params.TAG_DBG + Params.TAG_DS, "@BS/DH/START_INSPECTION: attempting inspection start");
                                    new StartInspection(context).execute();
                                }
                                break;

                            case GProtocol.COMMAND_BLUETOOTH_SEND_FINISHED_BORDER:
                                Log.d(Params.TAG_DBG + Params.TAG_DS, "@BS/DH/FINISHED_BORDER");
                                break;

                            case GProtocol.COMMAND_BLUETOOTH_SEND_FINISHED_SCAN:
                                Log.d(Params.TAG_DBG + Params.TAG_DS, "@BS/DH/FINISHED_SCAN");

                                // scanning phase over, broadcast that salting phase has started
                                if (context != null) {
                                    Log.d(Params.TAG_DBG + Params.TAG_DS, "@BS/DH/FINISHED_SCAN: broadcasting");
                                    currentInspectionInfo.setPhase(context, Params.CI_SALTING);
                                    context.sendBroadcast(new Intent().setAction(Params.SALTING_STARTED));
                                }
                                break;

                            case GProtocol.COMMAND_BLUETOOTH_SEND_DRONE_LANDED:
                                Log.d(Params.TAG_DBG + Params.TAG_DS, "@BS/DH/DRONE_LANDED");
                                break;

                            case GProtocol.COMMAND_BLUETOOTH_RETURN_HOME:
                                Log.d(Params.TAG_DBG + Params.TAG_DS, "@BS/DH/RETURN_HOME");
                                break;

                            case GProtocol.COMMAND_BLUETOOTH_SEND_FINISHED_ANALYSIS:
                                Log.d(Params.TAG_DBG + Params.TAG_DS, "@BS/DH/FINISHED_ANALYSIS");

                                // let drone know we want the icedam marked images
                                byte[] request = GProtocol.Pack(GProtocol.COMMAND_BLUETOOTH_SEND_IMAGES_RGB, 1, new byte[1], false);
                                btConnectionThread.write(request);
                                break;

                            case GProtocol.COMMAND_BLUETOOTH_SEND_LOW_BATTERY:
                                Log.d(Params.TAG_DBG + Params.TAG_DS, "@BS/DH/LOW_BATTERY");
                                break;

                            case GProtocol.COMMAND_BLUETOOTH_SEND_ROOF_SCAN_INTERRUPTED:
                                Log.d(Params.TAG_DBG + Params.TAG_DS, "@BS/DH/ROOF_SCAN_INTERRUPTED");
                                break;

                            case GProtocol.COMMAND_BLUETOOTH_SEND_BORDER_SCAN_INTERRUPTED:
                                Log.d(Params.TAG_DBG + Params.TAG_DS, "@BS/DH/BORDER_SCAN_INTERRUPTED");
                                break;

                            case GProtocol.COMMAND_BLUETOOTH_SEND_FINISHED_DAM:
                                Log.d(Params.TAG_DBG + Params.TAG_DS, "@BS/DH/FINISHED_DAM");
                                break;

                            case GProtocol.COMMAND_BLUETOOTH_SEND_FINISHED_ALL_DAMS:
                                Log.d(Params.TAG_DBG + Params.TAG_DS, "@BS/DH/FINISHED_ALL_DAMS");
                                break;

                            case GProtocol.COMMAND_SEND_ICEDAM_POINTS:
                                Log.d(Params.TAG_DBG + Params.TAG_DS, "@BS/DH/POINTS");
                                break;

                            case GProtocol.COMMAND_BLUETOOTH_SEND_JSON_RGB:
                                //Log.d(Params.TAG_DBG + Params.TAG_DS, "@BS/DH/JSON_RGB");
                                if (received.isPartialEnd()) {
                                    Log.d(Params.TAG_DBG + Params.TAG_DS, "@BS/DH/JSON_RGB: json received");

                                    // construct complete json string
                                    listGProtocol.add(received);
                                    GProtocol finalGProtocol = GProtocol.glueGProtocols(listGProtocol);
                                    String jsonRGB = ((JSON) finalGProtocol.read()).getJson();
                                    listGProtocol.clear();

                                    // turn json string into list of image detail objects
                                    Type type = new TypeToken<List<ImageDetails>>() {
                                    }.getType();
                                    imageDetailsList = new Gson().fromJson(jsonRGB, type);

                                    if (saltingPhaseImages) {
                                        // get icedam points
                                        for (ImageDetails imageDetails : imageDetailsList) {
                                            iceDamPoints.addAll(imageDetails.getIceDamPoints());
                                        }

                                        // raise flag up letting system know icedam points are ready
                                        iceDamPointsReady = true;

                                        // broadcast that icedam points are ready to map fragment
                                        if (context != null) {
                                            Log.d(Params.TAG_DBG + Params.TAG_DS, "@BS/DH/JSON_RGB: broadcasting to map fragment");

                                            // create intent to broadcast to map fragment
                                            Intent toMapFrag = new Intent(CurrentThreeActivity.DRONE_ACTIVITY_BROADCAST);
                                            toMapFrag.putExtra(CurrentThreeActivity.WHICH_FRAG, DroneMapFragment.class.getName());

                                            // send broadcast to map fragment
                                            LocalBroadcastManager.getInstance(context).sendBroadcast(toMapFrag);
                                        }
                                    } else { // transfer phase has begun
                                        // broadcast that transfer phase has started
                                        if (context != null) {
                                            Log.d(Params.TAG_DBG + Params.TAG_DS, "@BS/DH/JSON_RGB: broadcasting");
                                            currentInspectionInfo.setPhase(context, Params.CI_TRANSFERRING);
                                            context.sendBroadcast(new Intent().setAction(Params.TRANSFER_STARTED));
                                        }
                                    }
                                } else if (received.isPartial()) {
                                    listGProtocol.add(received);
                                } else {
                                    JSON jsonRGB = (JSON) received.read();
                                    Log.d(Params.TAG_DBG + Params.TAG_DS, jsonRGB.getJson());
                                }
                                break;

                            case GProtocol.COMMAND_BLUETOOTH_SEND_JSON_THERM:
                                //Log.d(Params.TAG_DBG + Params.TAG_DS, "@BS/DH/JSON_THERM");
                                if (received.isPartialEnd()) {
                                    Log.d(Params.TAG_DBG + Params.TAG_DS, "@BS/DH/JSON_THERM: json received");

                                    // construct complete json string
                                    listGProtocol.add(received);
                                    GProtocol finalGProtocol = GProtocol.glueGProtocols(listGProtocol);
                                    String jsonTherm = ((JSON) finalGProtocol.read()).getJson();
                                    listGProtocol.clear();

                                    // turn json string into list of image detail objects
                                    Type type = new TypeToken<List<ImageDetails>>() {
                                    }.getType();
                                    imageDetailsList = new Gson().fromJson(jsonTherm, type);
                                } else if (received.isPartial()) {
                                    listGProtocol.add(received);
                                } else {
                                    JSON jsonTherm = (JSON) received.read();
                                    Log.d(Params.TAG_DBG + Params.TAG_DS, jsonTherm.getJson());
                                }
                                break;

                            case GProtocol.COMMAND_BLUETOOTH_SEND_IMAGES_RGB:
                                //Log.d(Params.TAG_DBG + Params.TAG_DS, "@BS/DH/IMAGES_RGB");
                                if (received.isPartialEnd()) {
                                    Log.d(Params.TAG_DBG + Params.TAG_DS, "@BS/DH/IMAGE_RGB: image received");

                                    // construct image from byte packets
                                    listGProtocol.add(received);
                                    GProtocol finalGProtocol = GProtocol.glueGProtocols(listGProtocol);
                                    Images imageRGB = (Images) finalGProtocol.read();
                                    listGProtocol.clear();

                                    // check if this is an icedam the user needs to confirm
                                    if (saltingPhaseImages) {
                                        // do confirmation stuff here
                                        // save it locally for now
                                        saveImageLocally(imageRGB, imgIndexRGB);
                                    } else { // these images are part of the image transfer phase
                                        // save image locally
                                        saveImageLocally(imageRGB, imgIndexRGB);
                                    }
                                    imgIndexRGB++;
                                } else if (received.isPartial()) {
                                    listGProtocol.add(received);
                                } else {
                                    Log.d(Params.TAG_DBG + Params.TAG_ERROR, "@BS/DH/IMAGES_RGB: shouldn't be here");
                                }
                                break;

                            case GProtocol.COMMAND_BLUETOOTH_SEND_IMAGES_THERM:
                                //Log.d(Params.TAG_DBG + Params.TAG_DS, "@BS/DH/IMAGES_THERM");
                                if (received.isPartialEnd()) {
                                    Log.d(Params.TAG_DBG + Params.TAG_DS, "@BS/DH/IMAGES_THERM: image received");

                                    // construct the image from byte packets
                                    listGProtocol.add(received);
                                    GProtocol finalGProtocol = GProtocol.glueGProtocols(listGProtocol);
                                    Images imageTherm = (Images) finalGProtocol.read();
                                    listGProtocol.clear();

                                    // thermal images are always received as part of image transfer phase, save locally
                                    saveImageLocally(imageTherm, imgIndexTherm);

                                    // increment index counter
                                    imgIndexTherm++;
                                } else if (received.isPartial()) {
                                    listGProtocol.add(received);
                                } else {
                                    Log.d(Params.TAG_DBG + Params.TAG_ERROR, "@BS/DH/IMAGES_THERM: shouldn't be here");
                                }
                                break;

                            case GProtocol.COMMAND_BLUETOOTH_FINISHED_RGB:
                                Log.d(Params.TAG_DBG + Params.TAG_DS, "@BS/DH/FINISHED_RGB");
                                // check if these images were sent as part of the confirmation/salting phase
                                if (saltingPhaseImages) {
                                    // all images received from now on should be part of image transfer phase
                                    saltingPhaseImages = false;
                                }
                                break;

                            case GProtocol.COMMAND_BLUETOOTH_FINISHED_THERM:
                                Log.d(Params.TAG_DBG + Params.TAG_DS, "@BS/DH/FINISHED_THERM");
                                // transfer phase complete, broadcast that upload phase has started
                                if (context != null) {
                                    Log.d(Params.TAG_DBG + Params.TAG_DS, "@BS/DH/FINISHED_THERM: broadcasting");
                                    currentInspectionInfo.setPhase(context, Params.CI_UPLOADING);
                                    context.sendBroadcast(new Intent().setAction(Params.UPLOAD_STARTED));
                                }
                                break;
                        }
                    } catch (BluetoothException e) {
                        Log.d(Params.TAG_DBG + Params.TAG_DS, "@BS/DH/BTException: " + e.getMessage());
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

        private void saveImageLocally(Images image, int index) {
            // construct path
            String type = Integer.toString(imageDetailsList.get(index).image_type);
            String location = basePath + type + Integer.toString(index) + ".jpg";

            // create parent directories if needed
            File baseDirectories = Environment.getExternalStoragePublicDirectory(basePath);
            if (baseDirectories.mkdirs()) {
                Log.d(Params.TAG_DBG, "@BS/DH/saveImageLocally: base directories created");
            }

            // create file
            try {
                // convert image bitmap to byte array
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                image.getImage().compress(Bitmap.CompressFormat.JPEG, 100, bos);
                byte[] bitMapData = bos.toByteArray();

                // write bytes into the file
                FileOutputStream fos = new FileOutputStream(Environment.getExternalStoragePublicDirectory(location));
                fos.write(bitMapData);
                fos.flush();
                fos.close();
            } catch (Exception e) {
                Log.e(Params.TAG_EXCEPTION, "@BS/DH/saveImageLocally: FAILED TO CREATE IMAGE FILE", e);
            }
        }
    }

    private static class StartInspection extends AsyncTask<Void, Void, Void> {

        final Context context;
        boolean noError;

        public StartInspection(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... params) {
            // create inspection on backend
            Inspection inspection = new ServerDB(context).createInspection(clientId);

            if (inspection == null) { // error occurred
                noError = false;
            } else {
                noError = true;

                // save inspection locally
                inspection.cascadeSave();

                // save inspection id
                CurrentInspectionInfo currentInspectionInfo = new CurrentInspectionInfo();
                currentInspectionInfo.setInspectionId(context, inspection.id);

                // inspection is now in progress
                currentInspectionInfo.setNotInProgress(context, false);

                // drone is starting scanning phase
                currentInspectionInfo.setPhase(context, Params.CI_SCANNING);

                // reset some flow related variables
                iceDamPoints = new ArrayList<>();
                DroneMapFragment.confirmedIceDamPoints = new ArrayList<>();
                DroneMapFragment.sentIcedamPoints = false;
                DroneMapFragment.plottedIceDamPoints = false;
            }
            return null;
        }

        protected void onPostExecute(Void param) {
            if (noError) {
                mapPhaseComplete = true;
                context.sendBroadcast(new Intent().setAction(Params.INSPECTION_STARTED));
            } else {
                context.sendBroadcast(new Intent().setAction(Params.INSPECTION_TERMINATED));
            }
        }
    }
}
