package org.girodicer.plottwist.Bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import org.girodicer.plottwist.App;

import java.io.IOException;

/**
 * Created by Carlos on 2/16/2016.
 */
public class ConnectThread extends Thread {
    private BluetoothSocket bSocket;
    private BluetoothDevice bDevice;

    public ConnectThread(BluetoothDevice bDevice){
        this.bDevice = bDevice;

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
            } catch(IOException closeException){}
            return;
        }
    }

    public void cancel(){
        try{
            bSocket.close();
        } catch(IOException closeException){}
    }

}
