package org.girodicer.plottwist;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.girodicer.plottwist.Bluetooth.BluetoothException;
import org.girodicer.plottwist.Bluetooth.ConnectionThread;
import org.girodicer.plottwist.Bluetooth.GProtocol;
import org.girodicer.plottwist.Fragments.DroneMapFragment;
import org.girodicer.plottwist.Fragments.DroneStateFragment;
import org.girodicer.plottwist.Models.Images;
import org.girodicer.plottwist.Models.JSON;
import org.girodicer.plottwist.Models.Status;
import org.girodicer.plottwist.services.BluetoothService;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TestActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = TestActivity.class.getName().toUpperCase();
    public static final String DRONE_ACTIVITY_BROADCAST = "DRONE_ACTIVITY_BROADCAST";
    public static final String WHICH_FRAG = "WHICH_FRAG";
    public static final String STATUS_PACKAGE = "STATUS_PACKAGE";
    public static final String LOCATION_PACKAGE = "LOCATION_PACKAGE";

    private Button start_button;
    private Button stop_button;
    private Button rgb_button;
    private Button therm_button;
    private Button analysis_button;
    private TextView json_textbox;
    private ImageView rgb_imageview;

    private final Messenger btMessageHandler = new Messenger(new BTMessageHandler());

    private static Messenger bluetoothMessenger; // only for the handler in this class
    private static boolean bluetoothServiceBound = false;

    private ServiceConnection bluetoothConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Service Bound");
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        start_button = (Button) findViewById(R.id.start);
        start_button.setOnClickListener(this);

        stop_button = (Button) findViewById(R.id.stop);
        stop_button.setOnClickListener(this);

        rgb_button = (Button) findViewById(R.id.rcv_rgb_imgs);
        rgb_button.setOnClickListener(this);

        therm_button = (Button) findViewById(R.id.rcv_therm_imgs);
        therm_button.setOnClickListener(this);

        analysis_button = (Button) findViewById(R.id.img_analysis);
        analysis_button.setOnClickListener(this);

        json_textbox = (TextView) findViewById(R.id.json_textbox);
        json_textbox.setMovementMethod(new ScrollingMovementMethod());

        rgb_imageview = (ImageView) findViewById(R.id.rgb_images);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_drone, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindService(new Intent(this, BluetoothService.class), bluetoothConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(bluetoothConnection);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.start:
                App.BTConnection.write(GProtocol.Pack(GProtocol.COMMAND_START_INSPECTION, 1, new byte[1], false));
                Toast.makeText(TestActivity.this, "Starting the drone...", Toast.LENGTH_SHORT).show();
                break;
            case R.id.stop:
                App.BTConnection.write(GProtocol.Pack(GProtocol.COMMAND_END_INSPECTION, 1, new byte[1], false));
                Toast.makeText(TestActivity.this, "Requesting RGB images...", Toast.LENGTH_SHORT).show();
                break;
            case R.id.rcv_rgb_imgs:
                App.BTConnection.write(GProtocol.Pack(GProtocol.COMMAND_BLUETOOTH_SEND_IMAGES_RGB, 1, new byte[1], false));
                Toast.makeText(TestActivity.this, "Requesting RGB images...", Toast.LENGTH_SHORT).show();
                break;
            case R.id.rcv_therm_imgs:
                App.BTConnection.write(GProtocol.Pack(GProtocol.COMMAND_BLUETOOTH_SEND_IMAGES_THERM, 1, new byte[1], false));
                Toast.makeText(TestActivity.this, "Requesting Thermal images...", Toast.LENGTH_SHORT).show();
                break;
            case R.id.img_analysis:
                App.BTConnection.write(GProtocol.Pack(GProtocol.COMMAND_BLUETOOTH_SEND_IMAGES_THERM, 1, new byte[1], false));
                Toast.makeText(TestActivity.this, "Requesting Thermal images...", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private class BTMessageHandler extends Handler {
        List<GProtocol> gprotocol_list = new ArrayList<GProtocol>();
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
                                Status currentStatus = (Status) received.read();
                                Intent broadcastToFrag = new Intent(DRONE_ACTIVITY_BROADCAST);
                                broadcastToFrag.putExtra(WHICH_FRAG, DroneStateFragment.class.getName());
                                broadcastToFrag.putExtra(STATUS_PACKAGE, currentStatus);
                                LocalBroadcastManager.getInstance(TestActivity.this).sendBroadcast(broadcastToFrag);
                                break;
                            case GProtocol.COMMAND_SEND_POINTS:
                                break;
                            case GProtocol.COMMAND_BLUETOOTH_SEND_JSON_RGB:
                                if(received.isPartialEnd()){
                                    gprotocol_list.add(received);
                                    GProtocol final_gprotocol = GProtocol.glue_gprotocols(gprotocol_list);
                                    JSON rgb_json = (JSON) final_gprotocol.read();
                                    json_textbox.setText(rgb_json.getJson());
                                } else if(received.isPartial()){
                                    gprotocol_list.add(received);
                                } else{
                                    JSON rgb_json = (JSON) received.read();
                                    Log.d("JSON_SHOULDN'T GET HERE", rgb_json.getJson());
                                }
                                break;
                            case GProtocol.COMMAND_BLUETOOTH_SEND_JSON_THERM:
                                if(received.isPartialEnd()){
                                    gprotocol_list.add(received);
                                    GProtocol final_gprotocol = GProtocol.glue_gprotocols(gprotocol_list);
                                    JSON therm_json = (JSON) final_gprotocol.read();
                                    json_textbox.setText(therm_json.getJson());
                                } else if(received.isPartial()){
                                    gprotocol_list.add(received);
                                } else{
                                    JSON therm_json = (JSON) received.read();
                                    Log.d("JSON_SHOULDN'T GET HERE", therm_json.getJson());
                                }
                                break;
                            case GProtocol.COMMAND_BLUETOOTH_SEND_IMAGES_RGB:
                                if(received.isPartialEnd()){
                                    gprotocol_list.add(received);
                                    GProtocol final_gprotocol = GProtocol.glue_gprotocols(gprotocol_list);
                                    Images rgb_image = (Images) final_gprotocol.read();
                                    rgb_imageview.setImageBitmap(rgb_image.getImage());
                                } else if(received.isPartial()){
                                    gprotocol_list.add(received);
                                } else{
                                    Images rgb_image = (Images) received.read();
                                    Log.d("WEIRD ERROR", "");
                                }
                                break;
                            case GProtocol.COMMAND_BLUETOOTH_SEND_IMAGES_THERM:
                                break;
                        }
                        //Log.d(TAG, Integer.toString(received.getCommand()));
                    } catch (BluetoothException e) {
                        e.printStackTrace();
                    }
                    break;
                case BluetoothService.MESSAGE_BT_CONNECTION_LOST:
                    Toast.makeText(TestActivity.this, "Connection lost", Toast.LENGTH_SHORT).show();
                    Toast.makeText(TestActivity.this, "Reconnecting....", Toast.LENGTH_LONG).show();
                    break;
                case BluetoothService.MESSAGE_BT_FAILED_RECONNECT:
                    Toast.makeText(TestActivity.this, "Still reconnecting", Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothService.MESSAGE_BT_SUCCESS_RECONNECT:
                    Toast.makeText(TestActivity.this, "Successfully reconnected", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
