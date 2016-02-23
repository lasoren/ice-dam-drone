package org.girodicer.plottwist;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.girodicer.plottwist.Bluetooth.BluetoothException;
import org.girodicer.plottwist.Bluetooth.ConnectionThread;
import org.girodicer.plottwist.Bluetooth.GProtocol;
import org.girodicer.plottwist.Models.Points;
import org.girodicer.plottwist.Models.Status;
import org.girodicer.plottwist.services.BluetoothService;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, View.OnClickListener{

    private GoogleMap mMap;
    private Button next;

    private ArrayList<LatLng> houseBoundary;
    private LatLng home;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null){
            Intent incomingIntent = getIntent();
            home = incomingIntent.getParcelableExtra(App.LOCATION);
        } else {
            home = savedInstanceState.getParcelable(App.LOCATION);
        }

        setContentView(R.layout.activity_maps);

        next = (Button) findViewById(R.id.maps_next);
        next.setOnClickListener(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().show();

        // Obtain the MapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        houseBoundary = new ArrayList<>();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(home, 20));

        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener(this);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mMap.addMarker(new MarkerOptions().position(latLng).title(Integer.toString(houseBoundary.size())).draggable(true));
        houseBoundary.add(latLng);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        houseBoundary.remove(marker.getPosition());
        marker.remove();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d("dbg", "Resumed");
        bindService(new Intent(this, BluetoothService.class), bluetoothConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onPause(){
        super.onPause();
        if(bluetoothServiceBound){
            unbindService(bluetoothConnection);
            bluetoothMessenger = null;
            bluetoothServiceBound = false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outstate){
        outstate.putParcelable(App.LOCATION, home);
        super.onSaveInstanceState(outstate);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.maps_next:
                goToStatus();
                break;
        }
    }

    private void goToStatus(){
        if(houseBoundary.size() < 4){
            Toast.makeText(MapsActivity.this, "Need more points to complete house boundary.", Toast.LENGTH_SHORT).show();
        return;
    }

        byte[] points = Points.Pack(houseBoundary);
        App.BTConnection.write(GProtocol.Pack(GProtocol.COMMAND_SEND_POINTS, points.length, points, false));
    }

    private void plotPoints(ArrayList<LatLng> points){
        for (LatLng point : points){
            mMap.addMarker(new MarkerOptions().position(point));
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
                            case GProtocol.COMMAND_SEND_POINTS:
                                plotPoints((ArrayList<LatLng>) received.read());
                                break;
                        }
                    } catch (BluetoothException e) {
                        e.printStackTrace();
                    }
                    break;
                case BluetoothService.MESSAGE_BT_CONNECTION_LOST:
                    Toast.makeText(MapsActivity.this, "Connection lost", Toast.LENGTH_SHORT).show();
                    Toast.makeText(MapsActivity.this, "Reconnecting....", Toast.LENGTH_LONG).show();
                    break;
                case BluetoothService.MESSAGE_BT_FAILED_RECONNECT:
                    Toast.makeText(MapsActivity.this, "Still reconnecting", Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothService.MESSAGE_BT_SUCCESS_RECONNECT:
                    Toast.makeText(MapsActivity.this, "Successfully reconnected", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
