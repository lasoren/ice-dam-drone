package com.example.tberroa.girodicerapp.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import com.example.tberroa.girodicerapp.helpers.ExceptionHandler;
import com.example.tberroa.girodicerapp.App;

import java.io.IOException;

public class ConnectThread extends Thread {
    private BluetoothSocket bSocket;
    private BluetoothDevice bDevice;
    private Handler handler;

    public static final int CONNECT_SUCCESS = 11;
    public static final int CONNECT_FAILURE = -11;

    public ConnectThread(BluetoothDevice bDevice, Handler handler){
        this.bDevice = bDevice;
        this.handler = handler;

        BluetoothSocket tmp = null;

        try{
            tmp = bDevice.createRfcommSocketToServiceRecord(App.uuid);
        } catch (IOException e){
            new ExceptionHandler().HandleException(e);
        }

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
        } catch(IOException closeException){
            new ExceptionHandler().HandleException(closeException);
        }
    }

}
