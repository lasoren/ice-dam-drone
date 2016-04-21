package com.example.tberroa.girodicerapp.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.bluetooth.GProtocol;
import com.example.tberroa.girodicerapp.data.ClientId;
import com.example.tberroa.girodicerapp.data.CurrentInspectionInfo;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.bluetooth.Points;
import com.example.tberroa.girodicerapp.dialogs.GotItDialog;
import com.example.tberroa.girodicerapp.dialogs.MessageDialog;
import com.example.tberroa.girodicerapp.services.BluetoothService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class CurrentTwoActivity extends BaseActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener, View.OnClickListener {

    private GoogleMap mMap;
    private Button next;
    private ArrayList<LatLng> houseBoundary;
    private LatLng home;
    private boolean pathFound = false;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_two);

        // no animation if starting due to a reload
        String action = getIntent().getAction();
        if (action != null && action.equals(Params.RELOAD)) {
            overridePendingTransition(0, 0);
            Log.d(Params.TAG_DBG, "@CurrentTwoActivity: being recreated from a reload");
        }

        // check if user should be in this activity
        if (new CurrentInspectionInfo().getPhase(this) == Params.CI_UPLOADING){
            // done inspection but still uploading, go to fourth activity
            startActivity(new Intent(this, CurrentFourActivity.class));
            finish();
            return;
        }
        if (BluetoothService.notRunning(this)) {
            // bluetooth needs to be setup, go back to first activity
            startActivity(new Intent(this, CurrentOneActivity.class));
            finish();
            return;
        }
        if (BluetoothService.mapPhaseComplete) {
            // done setting up flight path, go to next activity
            startActivity(new Intent(this, CurrentThreeActivity.class));
            finish();
            return;
        }

        // get location, this should be set by the time this activity is launched
        home = BluetoothService.currentStatus.location;
        BluetoothService.home = home;
        Log.d(Params.TAG_DBG, "@CurrentTwoActivity: home is " + home);

        // set toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.current_inspection_title);
        }

        // initialize back button
        toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.back_button));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    new ClientId().clear(CurrentTwoActivity.this);
                    startActivity(new Intent(CurrentTwoActivity.this, ClientManagerActivity.class));
                    finish();
                }
            }
        });

        // set navigation menu
        navigationView.inflateMenu(R.menu.nav_client_inspections);

        // initialize next button
        next = (Button) findViewById(R.id.maps_next);
        next.setText(R.string.get_path);
        next.setOnClickListener(this);
        next.setEnabled(true);

        // Obtain the MapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        houseBoundary = new ArrayList<>();

        // initialize receiver, it's triggered when the house boundary points have been received from the drone
        IntentFilter filter = new IntentFilter();
        filter.addAction(Params.HOUSE_BOUNDARY_RECEIVED);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case Params.HOUSE_BOUNDARY_RECEIVED:
                        houseBoundary = BluetoothService.houseBoundary;
                        plotPoints(houseBoundary);
                        break;
                }
            }
        };
        registerReceiver(broadcastReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
        }
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
    public void onClick(View v) {
        next.setEnabled(false);
        switch (v.getId()) {
            case R.id.maps_next:
                if (pathFound) {
                    GotItDialog gotItDialog = new GotItDialog(this, getString(R.string.drone_active_stay_alert));
                    gotItDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            // send start inspection command to drone
                            BluetoothService.BTDataHandler.passContext(CurrentTwoActivity.this);
                            byte[] command = GProtocol.Pack(GProtocol.COMMAND_START_INSPECTION, 1, new byte[1], false);
                            BluetoothService.btConnectionThread.write(command);
                            Log.d(Params.TAG_DBG + Params.TAG_DS, "@CurrentTwoActivity: sent start inspection command");

                            // open dialog to let user know the app is waiting for a response
                            String message = getString(R.string.waiting_for_confirmation);
                            MessageDialog messageDialog =  new MessageDialog(CurrentTwoActivity.this, message);
                            messageDialog.setCancelable(false);
                            messageDialog.show();
                        }
                    });
                    gotItDialog.show();

                } else {
                    findPath();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            new ClientId().clear(this);
            startActivity(new Intent(this, ClientManagerActivity.class));
            finish();
        }
    }

    private void findPath() {
        if (houseBoundary.size() < 4) {
            Toast.makeText(CurrentTwoActivity.this, "Need more points to complete house boundary.", Toast.LENGTH_SHORT).show();
            next.setEnabled(true);
            return;
        }

        for (LatLng point : houseBoundary) {
            String latitude = Double.toString(point.latitude);
            String longitude = Double.toString(point.longitude);
            Log.d(Params.TAG_DBG, "@CurrentTwoActivity: houseBoundary point: (" + latitude + "," + longitude + ")");
        }

        // send new house point command to drone
        byte[] points = Points.Pack(houseBoundary);
        BluetoothService.BTDataHandler.passContext(this);
        BluetoothService.btConnectionThread.write(GProtocol.Pack(GProtocol.COMMAND_NEW_HOUSE, points.length, points, false));
        Log.d(Params.TAG_DBG + Params.TAG_DS, "@CurrentTwoActivity: sent house points");
        Toast.makeText(CurrentTwoActivity.this, "Sent house points", Toast.LENGTH_SHORT).show();
    }

    private void plotPoints(ArrayList<LatLng> points) {
        for (LatLng point : points) {
            mMap.addMarker(new MarkerOptions().position(point));
        }
        pathFound = true;
        next.setEnabled(true);
        next.setText(R.string.start_inspection);
    }
}
