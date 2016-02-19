package org.girodicer.plottwist;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.girodicer.plottwist.Bluetooth.ConnectThread;
import org.girodicer.plottwist.services.GetAddress;

import java.util.Set;

public class Welcome extends Activity implements View.OnClickListener {
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int okay = 0;
    private static final int failure = -1;

    Button pair, next;
    TextView pairNote;
    EditText address;

    AddressResultReceiver resultReceiver;

    private final BroadcastReceiver bScan = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //Log.d("dbg",device.getName());
                if(device.getName() != null){
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
        address = (EditText) findViewById(R.id.user_address);

        pair.setOnClickListener(this);
        next.setOnClickListener(this);

        App.bAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!App.bAdapter.isEnabled()){
            Intent enableBt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBt, REQUEST_ENABLE_BT);
        }

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bScan, filter);

        resultReceiver = new AddressResultReceiver(new Handler());
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

    private void pairDevices(){
        Set<BluetoothDevice> pairedDevices = App.bAdapter.getBondedDevices();
        if(pairedDevices.size() > 0){
            for(BluetoothDevice device : pairedDevices){
                if(device.getName() == getResources().getString(R.string.server_name)){
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
        pairNote.setText("Paired to: " + getResources().getString(R.string.server_name));
        ConnectThread btConnect = new ConnectThread(App.bDevice, new BTConnectHandler());
        btConnect.start();
    }

    public void skip(View view){
        Intent getAddress = new Intent(this, GetAddress.class);
        String location = address.getText().toString();
        getAddress.putExtra(GetAddress.RECEIVER, resultReceiver);
        getAddress.putExtra(GetAddress.LOCATION_DATA, location);
        startService(getAddress);
    }

    private class AddressResultReceiver extends ResultReceiver {

        /**
         * Create a new ResultReceive to receive results.  Your
         * {@link #onReceiveResult} method will be called from the thread running
         * <var>handler</var> if given, or from an arbitrary thread if null.
         *
         * @param handler
         */
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData){
            if(resultCode == failure){
                String message = resultData.getString(GetAddress.RECEIVER_DATA);
                Toast.makeText(Welcome.this, message, Toast.LENGTH_SHORT).show();
            } else if (resultCode == okay){
                LatLng coordinates = resultData.getParcelable(GetAddress.RECEIVER_DATA);
                Toast.makeText(Welcome.this, coordinates.toString(), Toast.LENGTH_SHORT).show();

                Intent skip = new Intent(Welcome.this, MapsActivity.class);
                skip.putExtra(GetAddress.RECEIVER_DATA, coordinates);
                startActivity(skip);
            }

        }
    }

    public class BTConnectHandler extends Handler{
        @Override
        public void handleMessage(Message incoming){
            switch(incoming.what){
                case ConnectThread.CONNECT_SUCCESS:
                    BluetoothSocket btSocket = (BluetoothSocket) incoming.obj;
                    if(App.startBluetoothConnection(btSocket)){
                        next.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(Welcome.this, "System failure", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case ConnectThread.CONNECT_FAILURE:
                    Toast.makeText(Welcome.this, "Failure to connect", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
