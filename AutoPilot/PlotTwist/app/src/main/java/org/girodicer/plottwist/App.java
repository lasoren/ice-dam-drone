package org.girodicer.plottwist;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import org.girodicer.plottwist.Bluetooth.ConnectThread;
import org.girodicer.plottwist.Bluetooth.ConnectionThread;
import org.girodicer.plottwist.services.BluetoothService;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Created by Carlos on 2/16/2016.
 */
public class App extends android.app.Application {
    public static final String LOCATION = "location";
    public static final String BT_CONNECTION_STATE = "bt connection state";
    public static BluetoothAdapter bAdapter;
    public static BluetoothDevice bDevice;
    public static final UUID uuid = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");

    private static Messenger bluetoothMessenger; // only for the bluetooth connection thread
    private static boolean bluetoothServiceBound = false;

    private static boolean manualBluetoothDisconnect = true;

    public static ConnectionThread BTConnection;
    public static boolean BTConnected = false;

    private ServiceConnection bluetoothConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bluetoothMessenger = new Messenger(service);
            bluetoothServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bluetoothMessenger = null;
            bluetoothServiceBound = false;
        }
    };

    private BroadcastReceiver BluetoothEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)){
                if(BTConnection != null)
                    BTConnection.cancel(); // we're no longer connected
                    BTConnected = false;
                if(!manualBluetoothDisconnect){ // we weren't the ones disconnecting
                    try {
                        bluetoothMessenger.send(Message.obtain(null, BluetoothService.MESSAGE_BT_CONNECTION_LOST));
                        unregisterReceiver(this);
                        ConnectThread btConnect = new ConnectThread(bDevice, new BTReconnectHandler());
                        btConnect.start();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    public void onCreate(){
        super.onCreate();
        Log.d("dbg", "Application creation");
        Intent serviceIntent = new Intent(this, BluetoothService.class);
        startService(serviceIntent);

        IntentFilter disconnectFilter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(BluetoothEventReceiver, disconnectFilter);

        bindService(serviceIntent, bluetoothConnection, Context.BIND_AUTO_CREATE);
    }

    public static boolean startBluetoothConnection(BluetoothSocket btSocket){
        if(!bluetoothServiceBound)
            return false;

        BTConnection = new ConnectionThread(btSocket, bluetoothMessenger);
        BTConnection.start();

        manualBluetoothDisconnect = false;
        BTConnected = true;
        return true;
    }

    private class BTReconnectHandler extends Handler {
        @Override
        public void handleMessage(Message incoming){
            switch(incoming.what){
                case ConnectThread.CONNECT_SUCCESS:
                    try {
                        bluetoothMessenger.send(Message.obtain(null, BluetoothService.MESSAGE_BT_SUCCESS_RECONNECT));
                        IntentFilter disconnectFilter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
                        registerReceiver(BluetoothEventReceiver, disconnectFilter);
                        BluetoothSocket btSocket = (BluetoothSocket) incoming.obj;
                        startBluetoothConnection(btSocket);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case ConnectThread.CONNECT_FAILURE:
                    try {
                        bluetoothMessenger.send(Message.obtain(null, BluetoothService.MESSAGE_BT_FAILED_RECONNECT));
                        //ConnectThread btConnect = new ConnectThread(bDevice, new BTReconnectHandler());
                        //btConnect.start();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }
}
