package org.girodicer.plottwist;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;

import org.girodicer.plottwist.Bluetooth.ConnectionThread;
import org.girodicer.plottwist.services.BluetoothService;

import java.util.UUID;

/**
 * Created by Carlos on 2/16/2016.
 */
public class App extends android.app.Application {
    public static BluetoothAdapter bAdapter;
    public static BluetoothDevice bDevice;
    public static final UUID uuid = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");

    private static Messenger bluetoothMessenger; // only for the bluetooth connection thread
    private static boolean bluetoothServiceBound = false;

    public static ConnectionThread BTConnection;

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

    public void onCreate(){
        super.onCreate();
        Log.d("dbg", "Application creation");
        Intent serviceIntent = new Intent(this, BluetoothService.class);
        startService(serviceIntent);

        bindService(serviceIntent, bluetoothConnection, Context.BIND_AUTO_CREATE);
    }

    public static boolean startBluetoothConnection(BluetoothSocket btSocket){
        if(!bluetoothServiceBound)
            return false;

        BTConnection = new ConnectionThread(btSocket, bluetoothMessenger);
        BTConnection.start();
        return true;
    }
}
