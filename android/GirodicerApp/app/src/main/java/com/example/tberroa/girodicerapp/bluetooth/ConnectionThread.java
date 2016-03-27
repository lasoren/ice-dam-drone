package com.example.tberroa.girodicerapp.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

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

        // Get the input and output streams, using temp objects because
        // member streams are final
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

        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                // Read from the InputStream
                bytes = btInStream.read(buffer);
                // Send the obtained bytes to the bluetooth data handler
                Message msg = Message.obtain(null, BluetoothService.READ, bytes, -1);
                Bundle bundle = new Bundle();
                bundle.putByteArray(BT_DATA, buffer);
                msg.setData(bundle);
                btDataHandler.send(msg);

                //Log.d("dbg", "@ConnectionThread: received message from drone & forwarded to handler");
            } catch (IOException e) {
                break;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        try {
            btOutStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* Call this from the main activity to shutdown the connection */
    public void shutdown() {

        try {
            btSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
