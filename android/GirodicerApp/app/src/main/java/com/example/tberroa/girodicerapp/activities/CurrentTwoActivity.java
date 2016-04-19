package com.example.tberroa.girodicerapp.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
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
import com.example.tberroa.girodicerapp.database.ServerDB;
import com.example.tberroa.girodicerapp.models.Inspection;
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

    private int clientId;
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
        if (BluetoothService.notRunning(this)) { // bluetooth needs to be setup
            startActivity(new Intent(this, CurrentOneActivity.class));
            finish();
            return;
        }
        if (BluetoothService.mapPhaseComplete) {
            // go to next activity
            startActivity(new Intent(this, CurrentThreeActivity.class));
            finish();
            return;
        }

        // get client id, will be needed later in async task
        clientId = new ClientId().get(CurrentTwoActivity.this);

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
        filter.addAction(Params.START_INSPECTION_CONFIRMED);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case Params.HOUSE_BOUNDARY_RECEIVED:
                        houseBoundary = BluetoothService.houseBoundary;
                        plotPoints(houseBoundary);
                        break;
                    case Params.START_INSPECTION_CONFIRMED:
                        new StartInspection().execute();
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
                    // send start inspection command to drone
                    BluetoothService.BTDataHandler.passContext(this);
                    BluetoothService.btConnectionThread.write(GProtocol.Pack(GProtocol.COMMAND_START_INSPECTION, 1, new byte[1], false));
                    Log.d(Params.TAG_DBG, "@CurrentTwoActivity: sent start inspection command");
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
        Log.d(Params.TAG_DBG, "@CurrentTwoActivity: sent house points");
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

    private class StartInspection extends AsyncTask<Void, Void, Void> {

        boolean noError;

        @Override
        protected Void doInBackground(Void... params) {
            // create inspection on backend
            Inspection inspection = new ServerDB(CurrentTwoActivity.this).createInspection(clientId);

            if (inspection == null) { // error occurred
                noError = false;
            } else {
                noError = true;

                // save inspection locally
                inspection.cascadeSave();

                // inspection is now in progress
                CurrentInspectionInfo currentInspectionInfo = new CurrentInspectionInfo();
                currentInspectionInfo.setNotInProgress(CurrentTwoActivity.this, false);

                // drone is starting scanning phase
                currentInspectionInfo.setPhase(CurrentTwoActivity.this, Params.CI_SCANNING);

                // save inspection id
                currentInspectionInfo.setInspectionId(CurrentTwoActivity.this, inspection.id);
            }
            return null;
        }

        protected void onPostExecute(Void param) {
            if (noError) {
                BluetoothService.mapPhaseComplete = true;
                startActivity(new Intent(CurrentTwoActivity.this, CurrentThreeActivity.class));
                finish();
            } else {
                Toast.makeText(CurrentTwoActivity.this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
