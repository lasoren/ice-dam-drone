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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.girodicer.plottwist.Bluetooth.BluetoothException;
import org.girodicer.plottwist.Bluetooth.ConnectionThread;
import org.girodicer.plottwist.Bluetooth.GProtocol;
import org.girodicer.plottwist.Models.Images;
import org.girodicer.plottwist.Models.JSON;
import org.girodicer.plottwist.Models.Status;
import org.girodicer.plottwist.services.BluetoothService;

import java.util.ArrayList;
import java.util.List;

public class TestActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = TestActivity.class.getName().toUpperCase();
    public static final String DRONE_ACTIVITY_BROADCAST = "DRONE_ACTIVITY_BROADCAST";
    public static final String WHICH_FRAG = "WHICH_FRAG";
    public static final String STATUS_PACKAGE = "STATUS_PACKAGE";
    public static final String LOCATION_PACKAGE = "LOCATION_PACKAGE";

    private LatLng home;

    private Button start_button, stop_button, rgb_button, therm_button, plot_icedam_button, service_icedam_button;
    private TextView json_textbox, notification_textbox, location, velocity, battery;
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

        if(savedInstanceState == null){
            Intent incomingIntent = getIntent();
            home = incomingIntent.getParcelableExtra(App.LOCATION);
        } else {
            home = savedInstanceState.getParcelable(App.LOCATION);
        }

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

        plot_icedam_button = (Button) findViewById(R.id.plot_icedam_points);
        plot_icedam_button.setOnClickListener(this);

        service_icedam_button = (Button) findViewById(R.id.service_icedam);
        service_icedam_button.setOnClickListener(this);
        service_icedam_button.setVisibility(View.GONE);

        json_textbox = (TextView) findViewById(R.id.json_textbox);
        json_textbox.setMovementMethod(new ScrollingMovementMethod());
        json_textbox.setVisibility(View.GONE);

        notification_textbox = (TextView) findViewById(R.id.notification_textbox);
        notification_textbox.setMovementMethod(new ScrollingMovementMethod());

        location = (TextView) findViewById(R.id.status_location);
        velocity = (TextView) findViewById(R.id.status_velocity);
        battery = (TextView) findViewById(R.id.status_battery);

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
                Toast.makeText(TestActivity.this, "Ending drone inspection", Toast.LENGTH_SHORT).show();
                break;
            case R.id.rcv_rgb_imgs:
                App.BTConnection.write(GProtocol.Pack(GProtocol.COMMAND_BLUETOOTH_SEND_IMAGES_RGB, 1, new byte[1], false));
                Toast.makeText(TestActivity.this, "Requesting RGB images...", Toast.LENGTH_SHORT).show();
                break;
            case R.id.rcv_therm_imgs:
                App.BTConnection.write(GProtocol.Pack(GProtocol.COMMAND_BLUETOOTH_SEND_IMAGES_THERM, 1, new byte[1], false));
                Toast.makeText(TestActivity.this, "Requesting Thermal images...", Toast.LENGTH_SHORT).show();
                break;
            case R.id.plot_icedam_points:
                Intent testing = new Intent(this, IceDamActivity.class);
                testing.putExtra(App.LOCATION, home);
                startActivityForResult(testing, 1);
                Toast.makeText(TestActivity.this, "Setting ice dam points...", Toast.LENGTH_SHORT).show();
                break;
            case R.id.service_icedam:
                App.BTConnection.write(GProtocol.Pack(GProtocol.COMMAND_BLUETOOTH_SERVICE_ICEDAM, 1, new byte[1], false));
                Toast.makeText(TestActivity.this, "Servicing ice dam...", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int request_code, int result_code, Intent data){
        Log.d("In onActivityResult", "");
        if(request_code == 1){
            if(result_code == TestActivity.RESULT_OK){
                Log.d("In onActivityResult", "Setting new button");
                plot_icedam_button.setVisibility(View.GONE);
                service_icedam_button.setVisibility(View.VISIBLE);
            }
        }
    }

    private class BTMessageHandler extends Handler {
        List<GProtocol> gprotocol_list = new ArrayList<GProtocol>();
        int rgb_img_index = 0;
        int therm_img_index = 0;
        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
                case BluetoothService.MESSAGE_READ:
                    Bundle bundle = msg.getData();
                    byte[] data = bundle.getByteArray(ConnectionThread.BT_DATA);
                    try {
                        GProtocol received = GProtocol.Unpack(data);
                        switch(received.getCommand()){
                            case GProtocol.COMMAND_START_INSPECTION:
                                //TODO: Change to new fragment that doesn't show start command anymore
                                Log.d("DRONE STATUS:", "Drone starting inspection...");
                                //Toast.makeText(TestActivity.this, "Starting Inspection!!!", Toast.LENGTH_SHORT).show();
                                notification_textbox.append("\nDrone starting inspection...");
                                break;
                            case GProtocol.COMMAND_BLUETOOTH_SEND_FINISHED_BORDER:
                                //TODO: Show message or something to notify user of border finishing (maybe a Toast statement is enough...)
                                Log.d("DRONE STATUS:", "Drone finished border");
                                //Toast.makeText(TestActivity.this, "Drone finished border!!!", Toast.LENGTH_SHORT).show();
                                notification_textbox.append("\nDrone finished border");
                                break;
                            case GProtocol.COMMAND_BLUETOOTH_SEND_FINISHED_SCAN:
                                //TODO: Show message or something to notify user of border finishing (maybe a Toast statement is enough...)
                                Log.d("DRONE STATUS", "Drone finished scan");
                                //Toast.makeText(TestActivity.this, "Drone finished scan!!!", Toast.LENGTH_SHORT).show();
                                notification_textbox.append("\nDrone finished scan");
                                break;
                            case GProtocol.COMMAND_BLUETOOTH_SEND_DRONE_LANDED:
                                //TODO: Show message or something to notify user of border finishing (maybe a Toast statement is enough...)
                                Log.d("DRONE STATUS", "Drone landed");
                                //Toast.makeText(TestActivity.this, "Drone Landed", Toast.LENGTH_SHORT).show();
                                notification_textbox.append("\nDrone landed");
                                break;
                            case GProtocol.COMMAND_BLUETOOTH_RETURN_HOME:
                                Log.d("DRONE STATUS", "Drone returned home");
                                //Toast.makeText(TestActivity.this, "Drone returning home...", Toast.LENGTH_SHORT).show();
                                notification_textbox.append("\nDrone returning home...");
                                break;
                            case GProtocol.COMMAND_BLUETOOTH_SEND_FINISHED_ANALYSIS:
                                //TODO: Show the button for Receiving RGB or Thermal Images and maybe change to a new fragment w/ a gallery view of images
                                Log.d("DRONE STATUS", "Drone finished analysis");
                                //Toast.makeText(TestActivity.this, "Drone is analyzing images...", Toast.LENGTH_SHORT).show();
                                notification_textbox.append("\nDrone finished analysis");
                                break;
                            case GProtocol.COMMAND_BLUETOOTH_SEND_LOW_BATTERY:
                                //TODO: Notify user that the drone is low battery and is heading back home now no matter what; might need to change to new screen
                                Log.d("DRONE STATUS", "Drone LOW BATTERY");
                                //Toast.makeText(TestActivity.this, "Drone LOW BATTERY!!!", Toast.LENGTH_SHORT).show();
                                notification_textbox.append("\nDrone LOW BATTERY");
                                break;
                            case GProtocol.COMMAND_BLUETOOTH_SEND_ROOF_SCAN_INTERRUPTED:
                                //TODO: Notify user, maybe just a message would be fine
                                Log.d("DRONE STATUS", "Drone roof scan interrupted");
                                //Toast.makeText(TestActivity.this, "Drone had roof scan interrupted", Toast.LENGTH_SHORT).show();
                                notification_textbox.append("\nDrone had roof scan interrupted");
                                break;
                            case GProtocol.COMMAND_BLUETOOTH_SEND_BORDER_SCAN_INTERRUPTED:
                                //TODO: Notify user, maybe just a message would be fine
                                Log.d("DRONE STATUS", "Drone border scan interrupted");
                                //Toast.makeText(TestActivity.this, "Drone had border scan interrupted", Toast.LENGTH_SHORT).show();
                                notification_textbox.append("\nDrone had border scan interrupted");
                                break;
                            case GProtocol.COMMAND_BLUETOOTH_SEND_FINISHED_DAM:
                                //TODO: Notify user, maybe just a message would be fine
                                Log.d("DRONE STATUS", "Drone finished one dam");
                                //Toast.makeText(TestActivity.this, "Drone finished one dam", Toast.LENGTH_SHORT).show();
                                notification_textbox.append("\nDrone finished salting an ice dam");
                                break;
                            case GProtocol.COMMAND_BLUETOOTH_SEND_FINISHED_ALL_DAMS:
                                //TODO: Notify user, maybe just a message would be fine
                                Log.d("DRONE STATUS", "Drone finished all dams");
                                //Toast.makeText(TestActivity.this, "Drone finished all dams", Toast.LENGTH_SHORT).show();
                                notification_textbox.append("\nDrone finished all dams");
                                break;
                            case GProtocol.COMMAND_BLUETOOTH_SERVICE_ICEDAM:
                                Log.d("DRONE STATUS", "Drone servicing dams");
                                //Toast.makeText(TestActivity.this, "Drone servicing dams", Toast.LENGTH_SHORT).show();
                                notification_textbox.append("\nDrone servicing ice dam");
                                break;
                            case GProtocol.COMMAND_STATUS:
                                //TODO: Show the map and other status info in a fragment (maybe a half-the-screen fragment)
                                Status currentStatus = (Status) received.read();
                                location.setText("Lat: " + currentStatus.location.latitude + "\nLong:" + currentStatus.location.longitude);
                                velocity.setText(currentStatus.velocity.toString());
                                battery.setText(Integer.toString(currentStatus.battery));
                                break;
                            case GProtocol.COMMAND_SEND_ICEDAM_POINTS:
                                break;
                            case GProtocol.COMMAND_BLUETOOTH_SEND_JSON_RGB:
                                if(received.isPartialEnd()){
                                    gprotocol_list.add(received);
                                    GProtocol final_gprotocol = GProtocol.glue_gprotocols(gprotocol_list);
                                    JSON rgb_json = (JSON) final_gprotocol.read();
                                    json_textbox.setText(rgb_json.getJson());
                                    gprotocol_list.clear();
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
                                    gprotocol_list.clear();
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
                                    gprotocol_list.clear();
                                    Toast.makeText(TestActivity.this, "Showing Image: " + rgb_img_index, Toast.LENGTH_SHORT).show();
                                    rgb_img_index++;
                                } else if(received.isPartial()){
                                    gprotocol_list.add(received);
                                } else{
                                    //Images rgb_image = (Images) received.read();
                                    Log.d("WEIRD ERROR", "COMMAND_BLUETOOTH_SEND_IMAGES_RGB shouldn't get here...");
                                }
                                break;
                            case GProtocol.COMMAND_BLUETOOTH_SEND_IMAGES_THERM:
                                if(received.isPartialEnd()){
                                    gprotocol_list.add(received);
                                    GProtocol final_gprotocol = GProtocol.glue_gprotocols(gprotocol_list);
                                    Images rgb_image = (Images) final_gprotocol.read();
                                    rgb_imageview.setImageBitmap(rgb_image.getImage());
                                    gprotocol_list.clear();
                                    Toast.makeText(TestActivity.this, "Showing Image: " + therm_img_index, Toast.LENGTH_SHORT).show();
                                    therm_img_index++;
                                } else if(received.isPartial()){
                                    gprotocol_list.add(received);
                                } else{
                                    //Images therm_image = (Images) received.read();
                                    Log.d("WEIRD ERROR", "COMMAND_BLUETOOTH_SEND_IMAGES_THERM shouldn't get here...");
                                }
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
