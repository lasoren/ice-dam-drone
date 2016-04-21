package com.example.tberroa.girodicerapp.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.services.BluetoothService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ConnectionThread extends Thread {

    public static final String BT_DATA = "BTDATA";
    private final BluetoothSocket btSocket;
    private final InputStream btInStream;
    private final OutputStream btOutStream;
    private final Messenger btDataHandler;

    public ConnectionThread(BluetoothSocket btSocket, Messenger btDataHandler) {

        this.btSocket = btSocket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        this.btDataHandler = btDataHandler;

        // Get the input and output streams, using temp objects because member streams are final
        try {
            tmpIn = btSocket.getInputStream();
            tmpOut = btSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        btInStream = tmpIn;
        btOutStream = tmpOut;
    }

    public void run() {
        byte[] buffer = new byte[1024];  // buffer store for the stream
        int bytes; // bytes returned from read()

        // Keep listening to the InputStream until an io exception occurs
        boolean listen = true;
        while (listen) {
            try {
                // Read from the InputStream
                bytes = btInStream.read(buffer);
                // Send the obtained bytes to the bluetooth data handler
                Message msg = Message.obtain(null, BluetoothService.READ, bytes, -1);
                Bundle bundle = new Bundle();
                bundle.putByteArray(BT_DATA, buffer);
                msg.setData(bundle);
                btDataHandler.send(msg);
                Log.d(Params.TAG_DBG + Params.TAG_BT, "@ConnectionThread: message sent to handler");
            } catch (IOException e) {
                Log.d(Params.TAG_DBG + Params.TAG_BT, "@ConnectionThread: IOException occurred");
                e.printStackTrace();
                listen = false;
                shutdown();
            } catch (RemoteException e) {
                Log.d(Params.TAG_DBG + Params.TAG_BT, "@ConnectionThread: RemoteException occurred");
                e.printStackTrace();
            }
        }
    }

    /* Call this to send data to the remote device */
    public void write(byte[] bytes) {
        try {
            btOutStream.write(bytes);
            Log.d(Params.TAG_DBG + Params.TAG_BT, "@ConnectionThread/write: sending drone a signal");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* Call this to shutdown the connection */
    public void shutdown() {

        // close streams
        try {
            btInStream.close();
        } catch (IOException inCloseException) {
            inCloseException.printStackTrace();
        }
        try {
            btOutStream.close();
        } catch (IOException outCloseException) {
            outCloseException.printStackTrace();
        }

        // close socket
        try {
            btSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
