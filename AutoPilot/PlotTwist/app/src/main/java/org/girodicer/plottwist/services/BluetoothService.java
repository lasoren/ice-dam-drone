package org.girodicer.plottwist.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

/**
 * Created by Carlos on 2/16/2016.
 */
public class BluetoothService extends Service {
    public static final int MESSAGE_READ = 1;
    public static final int MESSAGE_NEW_CLIENT = 2;
    public static final int MESSAGE_BT_CONNECTION_LOST = -1;
    public static final int MESSAGE_BT_FAILED_RECONNECT = -2;
    public static final int MESSAGE_BT_SUCCESS_RECONNECT = 3;

    private Messenger currentClient;

    final Messenger mMessenger = new Messenger(new BluetoothHandler());

    int count = 0;

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("dbg", "Service has been bound");
        count++;
        return mMessenger.getBinder();
    }

    class BluetoothHandler extends Handler {
        @Override
        public void handleMessage(Message msg){
            Log.d("dbg", "incoming message" + Integer.toString(count));
            switch(msg.what){
                case MESSAGE_READ:
                case MESSAGE_BT_CONNECTION_LOST:
                    try { // just forwards the message
                        currentClient.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case MESSAGE_NEW_CLIENT:
                    currentClient = msg.replyTo;
                    break;
            }
        }
    }
}
