package org.girodicer.plottwist.Bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import org.girodicer.plottwist.App;
import org.girodicer.plottwist.Welcome;

import java.io.IOException;

/**
 * Created by Carlos on 2/16/2016.
 */
public class ConnectThread extends Thread {
    private BluetoothSocket bSocket;
    private BluetoothDevice bDevice;
    private Welcome.BTConnectHandler handler;

    public static final int CONNECT_SUCCESS = 11;
    public static final int CONNECT_FAILURE = -11;

    public ConnectThread(BluetoothDevice bDevice, Welcome.BTConnectHandler handler){
        this.bDevice = bDevice;
        this.handler = handler;

        BluetoothSocket tmp = null;

        try{
            tmp = bDevice.createRfcommSocketToServiceRecord(App.uuid);
        } catch (IOException e){}

        bSocket = tmp;
    }

    public void run(){
        App.bAdapter.cancelDiscovery();

        try{
            bSocket.connect();
        } catch(IOException connectException){
            try{
                bSocket.close();
                handler.obtainMessage(CONNECT_FAILURE).sendToTarget();
            } catch(IOException closeException){
                handler.obtainMessage(CONNECT_FAILURE).sendToTarget();
            }
            return;
        }

        handler.obtainMessage(CONNECT_SUCCESS, -1, -1, bSocket).sendToTarget();
    }

    public void cancel(){
        try{
            bSocket.close();
        } catch(IOException closeException){}
    }

}
