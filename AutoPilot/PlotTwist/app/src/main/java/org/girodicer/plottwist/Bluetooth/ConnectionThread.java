package org.girodicer.plottwist.Bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import org.girodicer.plottwist.services.BluetoothService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Carlos on 2/16/2016.
 */
public class ConnectionThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private final Messenger btService;

    public ConnectionThread(BluetoothSocket socket, Messenger btService) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        this.btService = btService;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        byte[] buffer = new byte[1024];  // buffer store for the stream
        int bytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                // Read from the InputStream
                bytes = mmInStream.read(buffer);
                // Send the obtained bytes to the UI activity

                Message msg = Message.obtain(null, BluetoothService.MESSAGE_READ, bytes, -1, buffer);
                btService.send(msg);
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
            mmOutStream.write(bytes);
        } catch (IOException e) { }
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}
