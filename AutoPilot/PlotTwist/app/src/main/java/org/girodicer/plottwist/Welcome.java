package org.girodicer.plottwist;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.girodicer.plottwist.Bluetooth.BluetoothException;
import org.girodicer.plottwist.Bluetooth.ConnectThread;
import org.girodicer.plottwist.Bluetooth.ConnectionThread;
import org.girodicer.plottwist.Bluetooth.GProtocol;
import org.girodicer.plottwist.Models.Status;
import org.girodicer.plottwist.services.BluetoothService;

import java.util.Set;

public class Welcome extends Activity implements View.OnClickListener {
    private static final int REQUEST_ENABLE_BT = 1;

    Button pair, next;
    TextView pairNote;

    private Status currentStatus;

    private final Messenger btMessageHandler = new Messenger(new BTMessageHandler());

    private static Messenger bluetoothMessenger; // only for the handler in this class
    private static boolean bluetoothServiceBound = false;

    private ServiceConnection bluetoothConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bluetoothMessenger = new Messenger(service);
            bluetoothServiceBound = true;

            Message msg = Message.obtain(null, BluetoothService.MESSAGE_NEW_CLIENT);
            msg.replyTo = btMessageHandler;
            try {
                bluetoothMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Message msg = Message.obtain(null, BluetoothService.MESSAGE_DETACH_CLIENT);
            try{
                bluetoothMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            bluetoothMessenger = null;
            bluetoothServiceBound = false;
        }
    };

    private final BroadcastReceiver bScan = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getName() != null){
                    Log.d("dbg",device.getName());
                    if(device.getName().equals(getResources().getString(R.string.server_name))){
                        App.bDevice = device;
                        App.bAdapter.cancelDiscovery();
                        Toast.makeText(Welcome.this, "Found the device", Toast.LENGTH_SHORT).show();
                        pairFinished();
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        pair = (Button) findViewById(R.id.pairButton);
        next = (Button) findViewById(R.id.nextButton);
        pairNote = (TextView) findViewById(R.id.pair_notification);

        next.setVisibility(View.VISIBLE);

        pair.setOnClickListener(this);
        next.setOnClickListener(this);

        App.bAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!App.bAdapter.isEnabled()){
            Intent enableBt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBt, REQUEST_ENABLE_BT);
        }

        if(savedInstanceState != null){
            boolean btconnected = savedInstanceState.getBoolean(App.BT_CONNECTION_STATE);

            if(btconnected){
                bindService(new Intent(Welcome.this, BluetoothService.class), bluetoothConnection, Context.BIND_AUTO_CREATE);
                pair.setVisibility(View.GONE);
                next.setVisibility(View.VISIBLE);
                pairNote.setVisibility(View.VISIBLE);
                pairNote.setText("Paired to: " + getResources().getString(R.string.server_name));
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        switch(requestCode){
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_CANCELED){

                }
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.pairButton:
                pairDevices();
                break;
            case R.id.nextButton:
                skip(null);
                break;
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bScan, filter);
    }

    @Override
    public void onPause(){
        super.onPause();
        unregisterReceiver(bScan);
        if(bluetoothServiceBound)
            unbindService(bluetoothConnection);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(App.BT_CONNECTION_STATE, App.BTConnected);
        super.onSaveInstanceState(outState);
    }

    private void pairDevices(){
        Set<BluetoothDevice> pairedDevices = App.bAdapter.getBondedDevices();
        if(pairedDevices.size() > 0){
            for(BluetoothDevice device : pairedDevices){
                if(device.getName().equals(getResources().getString(R.string.server_name))){
                    App.bDevice = device;
                    pairFinished();
                    return;
                }
            }
        }
        Toast.makeText(Welcome.this, "Starting Discovery", Toast.LENGTH_SHORT).show();
        App.bAdapter.startDiscovery();
    }

    private void pairFinished(){
        pair.setVisibility(View.GONE);
        pairNote.setVisibility(View.VISIBLE);
        pairNote.setText("Paired to: " + getResources().getString(R.string.server_name));
        ConnectThread btConnect = new ConnectThread(App.bDevice, new BTConnectHandler());
        btConnect.start();
    }

    public void skip(View view){
        Intent next = new Intent(this, DroneActivity.class);
        //next.putExtra(App.LOCATION, currentStatus.location);
        startActivity(next);
    }

    public class BTConnectHandler extends Handler{
        @Override
        public void handleMessage(Message incoming){
            switch(incoming.what){
                case ConnectThread.CONNECT_SUCCESS:
                    BluetoothSocket btSocket = (BluetoothSocket) incoming.obj;
                    if(App.startBluetoothConnection(btSocket)){
                        bindService(new Intent(Welcome.this, BluetoothService.class), bluetoothConnection, Context.BIND_AUTO_CREATE);
                        next.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(Welcome.this, "System failure", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case ConnectThread.CONNECT_FAILURE:
                    Toast.makeText(Welcome.this, "Failure to connect", Toast.LENGTH_SHORT).show();
                    next.setVisibility(View.GONE);
                    pair.setVisibility(View.VISIBLE);
                    pairNote.setVisibility(View.GONE);
            }
        }
    }

    private class BTMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
                case BluetoothService.MESSAGE_READ:
                    Bundle bundle = msg.getData();
                    byte[] data = bundle.getByteArray(ConnectionThread.BT_DATA);
                    try {
                        GProtocol received = GProtocol.Unpack(data);
                        switch(received.getCommand()){
                            case GProtocol.COMMAND_STATUS:
                                currentStatus = (Status) received.read();
                                break;
                        }
                    } catch (BluetoothException e) {
                        e.printStackTrace();
                    }
                    break;
                case BluetoothService.MESSAGE_BT_CONNECTION_LOST:
                    Toast.makeText(Welcome.this, "Connection lost", Toast.LENGTH_SHORT).show();
                    Toast.makeText(Welcome.this, "Reconnecting....", Toast.LENGTH_LONG).show();
                    break;
                case BluetoothService.MESSAGE_BT_FAILED_RECONNECT:
                    Toast.makeText(Welcome.this, "Still reconnecting", Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothService.MESSAGE_BT_SUCCESS_RECONNECT:
                    Toast.makeText(Welcome.this, "Successfully reconnected", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
