package org.girodicer.plottwist.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Created by Carlos on 2/16/2016.
 */
public class BluetoothService extends Service {
    public static final int MESSAGE_READ = 1;
    public static final int MESSAGE_NEW_CLIENT = 2;
    public static final int MESSAGE_BT_CONNECTION_LOST = -1;
    public static final int MESSAGE_BT_FAILED_RECONNECT = -2;
    public static final int MESSAGE_BT_SUCCESS_RECONNECT = 3;
    public static final int MESSAGE_DETACH_CLIENT = 4;

    private Messenger currentClient = null;

    final Messenger mMessenger = new Messenger(new BluetoothHandler());
    final Queue<Message> backlog = new PriorityQueue<>();

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
                case MESSAGE_BT_FAILED_RECONNECT:
                case MESSAGE_BT_SUCCESS_RECONNECT:
                    try { // just forwards the message
                        if(currentClient != null) {
                            currentClient.send(msg);
                        } else {
                            backlog.offer(msg);
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case MESSAGE_NEW_CLIENT:
                    while(!backlog.isEmpty()){
                        try {
                            msg.replyTo.send(backlog.poll());
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    currentClient = msg.replyTo;
                    break;
                case MESSAGE_DETACH_CLIENT:
                    currentClient = null;
                    break;
            }
        }
    }
}
