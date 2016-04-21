package com.example.tberroa.girodicerapp.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.tberroa.girodicerapp.data.BluetoothInfo;
import com.example.tberroa.girodicerapp.data.OperatorInfo;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.data.UserInfo;
import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.data.CurrentInspectionInfo;
import com.example.tberroa.girodicerapp.dialogs.ConfirmEndDialog;
import com.example.tberroa.girodicerapp.dialogs.CreateClientDialog;
import com.example.tberroa.girodicerapp.dialogs.MessageDialog;
import com.example.tberroa.girodicerapp.helpers.Utilities;
import com.example.tberroa.girodicerapp.services.BluetoothService;
import com.example.tberroa.girodicerapp.services.UploadIntentService;

public class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // shared preferences
    final BluetoothInfo bluetoothInfo = new BluetoothInfo();
    private final CurrentInspectionInfo currentInspectionInfo = new CurrentInspectionInfo();
    private final UserInfo userInfo = new UserInfo();

    // control variables for handling reloads upon state changes
    private boolean inView;
    private boolean stateChange = false;

    // ui elements
    Toolbar toolbar;
    DrawerLayout drawer;
    NavigationView navigationView;
    private SmoothActionBarDrawerToggle toggle;

    // receiver used for listening to system updates. upon update it reloads the activity
    private BroadcastReceiver broadcastReceiver;

    // system management
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // if user is not signed in, boot them
        if (!userInfo.isLoggedIn(this)) {
            Utilities.signOut(this);
        }

        // check if bluetooth service was destroyed by the system (in this case onDestroy is not called)
        if (BluetoothService.notRunning(this) && BluetoothService.serviceRunning) {
            Log.d(Params.TAG_DBG, "@BaseActivity: bluetooth service was destroyed by system. cleaning up");

            // shutdown connection thread
            if (BluetoothService.btConnectionThread != null) {
                BluetoothService.btConnectionThread.shutdown();
                BluetoothService.btConnectionThread = null;
            }

            // reset bluetooth state
            bluetoothInfo.setState(this, Params.BTS_NOT_CONNECTED);

            // handle current inspection based on how far the user got
            CurrentInspectionInfo currentInspectionInfo = new CurrentInspectionInfo();
            int phase = currentInspectionInfo.getPhase(this);
            if (phase == Params.CI_UPLOADING) { // no longer dependent on bluetooth, don't touch current inspection info
                Log.d(Params.TAG_DBG + Params.TAG_BT, "@BluetoothService/onDestroy: inspection kept in upload phase");
            } else if (phase == Params.CI_TRANSFERRING) {// transferring gets killed but upload what we can
                Log.d(Params.TAG_DBG + Params.TAG_BT, "@BluetoothService/onDestroy: inspection pushed into upload phase");
                currentInspectionInfo.setRoofEdgeCount(this, BluetoothService.BTDataHandler.imgIndexRGB);
                currentInspectionInfo.setThermalCount(this, BluetoothService.BTDataHandler.imgIndexTherm);
                currentInspectionInfo.setPhase(this, Params.CI_UPLOADING);
                startService(new Intent(this, UploadIntentService.class));
                sendBroadcast(new Intent().setAction(Params.UPLOAD_STARTED));
            } else { // user was servicing icedams or drone was still scanning, full clean up
                Log.d(Params.TAG_DBG + Params.TAG_BT, "@BluetoothService/onDestroy: full inspection clean up");
                currentInspectionInfo.setPhase(this, Params.CI_INACTIVE);
                currentInspectionInfo.setInProgress(this, false);
            }

            // update variables
            BluetoothService.needInitialStatus = true;
            BluetoothService.mapPhaseComplete = false;
            BluetoothService.serviceRunning = false;
            BluetoothService.currentStatus = null;

            // destroy context reference from bluetooth data handler
            BluetoothService.BTDataHandler.destroyContext();

            // unregister receiver
            if (BluetoothService.btReceiver != null) {
                unregisterReceiver(BluetoothService.btReceiver);
                BluetoothService.btReceiver = null;
            }
        }

        // set up receiver to reload activity upon system updates
        IntentFilter filter = new IntentFilter();
        filter.addAction(Params.RELOAD);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                stateChange = true;

                // always do a simply reload
                if (inView) {
                    startActivity(getIntent().setAction(Params.RELOAD).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                    stateChange = false; // state change handled
                    finish();
                }
            }
        };
        registerReceiver(broadcastReceiver, filter);
    }

    // handle ui responsibilities here
    @SuppressLint("InflateParams")
    @Override
    public void setContentView(final int layoutResID) {
        // base layout
        drawer = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_base, null);
        FrameLayout content = (FrameLayout) drawer.findViewById(R.id.activity_content);

        // fill content layout with the provided layout
        getLayoutInflater().inflate(layoutResID, content, true);
        super.setContentView(drawer);

        // initialize toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // initialize drawer
        toggle = new SmoothActionBarDrawerToggle(this, drawer, toolbar);
        toggle.setDrawerIndicatorEnabled(false);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // initialize navigation view
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // initialize drawer header
        View headerLayout = navigationView.getHeaderView(0);
        TextView operatorName = (TextView) headerLayout.findViewById(R.id.operator_name);
        operatorName.setText(new OperatorInfo().getFirstName(this));

        // the rest of the code here is for notification bar management
        // declare the notification text views which may or may not populate the notification bar
        final TextView btStateText = (TextView) findViewById(R.id.bt_state_text);
        final TextView inspectionPhaseText = (TextView) findViewById(R.id.inspection_phase_text);

        // by default all notifications are off
        btStateText.setVisibility(View.GONE);
        inspectionPhaseText.setVisibility(View.GONE);

        // get the status of each system
        int btState = bluetoothInfo.getState(this);
        int inspectionPhase = currentInspectionInfo.getPhase(this);

        // set notification bar accordingly
        switch (btState) {
            case Params.BTS_CONNECTING:
                btStateText.setText(R.string.bt_state_connecting);
                btStateText.setVisibility(View.VISIBLE);
                break;
            case Params.BTS_CONNECTED:
                btStateText.setText(R.string.bt_state_connected);
                btStateText.setVisibility(View.VISIBLE);
                break;
            case Params.BTS_CONNECTION_LOST:
                btStateText.setText(R.string.bt_state_connection_lost);
                btStateText.setVisibility(View.VISIBLE);
                break;
        }
        switch (inspectionPhase) {
            case Params.CI_INACTIVE:
                inspectionPhaseText.setVisibility(View.GONE);
                break;
            case Params.CI_SCANNING:
                inspectionPhaseText.setText(R.string.ci_scanning);
                inspectionPhaseText.setVisibility(View.VISIBLE);
                break;
            case Params.CI_SALTING:
                inspectionPhaseText.setText(R.string.ci_salting);
                inspectionPhaseText.setVisibility(View.VISIBLE);
                break;
            case Params.CI_TRANSFERRING:
                inspectionPhaseText.setText(R.string.ci_data_transfer);
                inspectionPhaseText.setVisibility(View.VISIBLE);
                break;
            case Params.CI_UPLOADING:
                inspectionPhaseText.setText(R.string.ci_uploading);
                inspectionPhaseText.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        inView = true;
        if (stateChange) {
            // reload
            startActivity(getIntent().setAction(Params.RELOAD).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
            stateChange = false; // state change handled
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        inView = false;
    }

    // unregister receiver
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
        }
    }

    // handle all drawer menu clicks here
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.sign_out:
                toggle.runWhenIdle(new Runnable() {
                    @Override
                    public void run() {
                        // check if there is an ongoing active inspection
                        if (currentInspectionInfo.isInProgress(BaseActivity.this)) {
                            String message = getResources().getString(R.string.cannot_sign_out);
                            new MessageDialog(BaseActivity.this, message).show();
                        } else {
                            Utilities.signOut(BaseActivity.this);
                        }
                    }
                });
                drawer.closeDrawers();
                break;
            case R.id.add_client:
                toggle.runWhenIdle(new Runnable() {
                    @Override
                    public void run() {
                        new CreateClientDialog(BaseActivity.this).show();
                    }
                });
                drawer.closeDrawers();
                break;
            case R.id.end_inspection:
                toggle.runWhenIdle(new Runnable() {
                    @Override
                    public void run() {
                        if (BluetoothService.currentStatus != null) {
                            new ConfirmEndDialog(BaseActivity.this).show();
                        } else {
                            String message = getString(R.string.no_active_inspection);
                            new MessageDialog(BaseActivity.this, message).show();
                        }
                    }
                });
                drawer.closeDrawers();
                break;
            case R.id.current_inspection:
                toggle.runWhenIdle(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(BaseActivity.this, CurrentOneActivity.class));
                        finish();
                    }
                });
                drawer.closeDrawers();
                break;
            case R.id.past_inspections:
                toggle.runWhenIdle(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(BaseActivity.this, PastInspectionsActivity.class));
                        finish();
                    }
                });
                drawer.closeDrawers();
                break;
        }
        return true;
    }

    // inflate toolbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.base_menu, menu);

        // check if terminate button needs to be added
        if (BluetoothService.currentStatus != null) {
            menu.add(0, Params.TERMINATE_INSPECTION, Menu.NONE, R.string.terminate)
                    .setIcon(R.drawable.terminate_button)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
        return true;
    }

    // handle toolbar menu clicks here
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.open_menu:
                drawer.openDrawer(GravityCompat.START);
                return true;
            case Params.TERMINATE_INSPECTION:
                new ConfirmEndDialog(this).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // class used to smooth out drawer open/close animations
    private class SmoothActionBarDrawerToggle extends ActionBarDrawerToggle {

        private Runnable runnable;

        public SmoothActionBarDrawerToggle(Activity activity, DrawerLayout drawer, Toolbar toolbar) {
            super(activity, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        }

        @Override
        public void onDrawerOpened(View drawerView) {
            super.onDrawerOpened(drawerView);
            invalidateOptionsMenu();
        }

        @Override
        public void onDrawerClosed(View view) {
            super.onDrawerClosed(view);
            invalidateOptionsMenu();
        }

        @Override
        public void onDrawerStateChanged(int newState) {
            super.onDrawerStateChanged(newState);
            if (runnable != null && newState == DrawerLayout.STATE_IDLE) {
                runnable.run();
                runnable = null;
            }
        }

        public void runWhenIdle(Runnable runnable) {
            this.runnable = runnable;
        }
    }
}