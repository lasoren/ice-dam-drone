package com.example.tberroa.girodicerapp.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.tberroa.girodicerapp.data.BluetoothInfo;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.data.PastInspectionsInfo;
import com.example.tberroa.girodicerapp.data.UserInfo;
import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.data.CurrentInspectionInfo;
import com.example.tberroa.girodicerapp.database.LocalDB;
import com.example.tberroa.girodicerapp.dialogs.ConfirmEndInspectionDialog;
import com.example.tberroa.girodicerapp.dialogs.MessageDialog;
import com.example.tberroa.girodicerapp.helpers.Utilities;
import com.example.tberroa.girodicerapp.services.BluetoothService;

public class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // shared preferences
    final BluetoothInfo bluetoothInfo = new BluetoothInfo();
    final CurrentInspectionInfo currentInspectionInfo = new CurrentInspectionInfo();
    private final UserInfo userInfo = new UserInfo();
    private final PastInspectionsInfo pastInspectionsInfo = new PastInspectionsInfo();

    // control variables for handling reloads upon state changes
    private boolean inView;
    private boolean stateChange = false;

    // ui elements
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
            Log.d("dbg", "@BaseActivity: bluetooth service was destroyed by system. cleaning up");

            // update variables
            BluetoothService.needInitialStatus = true;
            BluetoothService.serviceRunning = false;

            // reset state
            bluetoothInfo.setState(this, Params.BTS_NOT_CONNECTED);
            BluetoothService.currentStatus = null;
            if (BluetoothService.btConnectionThread != null) {
                BluetoothService.btConnectionThread.shutdown();
            }

            // also reset current inspection info, most current inspection processing is bluetooth dependent
            currentInspectionInfo.clearAll(this);
        }

        // set up receiver to reload activity upon system updates
        IntentFilter filter = new IntentFilter();
        filter.addAction(Params.BLUETOOTH_TIMEOUT);
        filter.addAction(Params.CONNECTING_TO_DRONE);
        filter.addAction(Params.DRONE_CONNECT_SUCCESS);
        filter.addAction(Params.DRONE_CONNECT_FAILURE);
        filter.addAction(Params.DRONE_CONNECTION_LOST);
        filter.addAction(Params.INITIAL_STATUS_RECEIVED);
        filter.addAction(Params.TRANSFER_STARTED);
        filter.addAction(Params.UPLOAD_STARTED);
        filter.addAction(Params.UPLOAD_COMPLETE);
        filter.addAction(Params.UPDATING_STARTED);
        filter.addAction(Params.UPDATING_COMPLETE);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case Params.BLUETOOTH_TIMEOUT:
                    case Params.CONNECTING_TO_DRONE:
                    case Params.DRONE_CONNECT_SUCCESS:
                    case Params.DRONE_CONNECT_FAILURE:
                    case Params.DRONE_CONNECTION_LOST:
                    case Params.INITIAL_STATUS_RECEIVED:
                    case Params.TRANSFER_STARTED:
                    case Params.UPLOAD_STARTED:
                    case Params.UPLOAD_COMPLETE:
                    case Params.UPDATING_STARTED:
                    case Params.UPDATING_COMPLETE:
                        stateChange = true;
                        if (inView){
                            startActivity(getIntent().setAction(Params.RELOAD).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                            stateChange = false; // state change handled
                            finish();
                        }
                        break;
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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // initialize drawer
        toggle = new SmoothActionBarDrawerToggle(this, drawer, toolbar);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // initialize navigation view
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // initialize drawer header
        View headerLayout = navigationView.getHeaderView(0);
        TextView operatorName = (TextView) headerLayout.findViewById(R.id.operator_name);
        operatorName.setText(new LocalDB().getOperator().user.first_name);

        // the rest of the code here is for notification bar management
        // declare the notification text views which may or may not populate the notification bar
        final TextView btStateText = (TextView) findViewById(R.id.bt_state_text);
        final TextView inspectionPhaseText = (TextView) findViewById(R.id.inspection_phase_text);
        final TextView fetchingDataText = (TextView) findViewById(R.id.fetching_text);

        // by default all notifications are off
        btStateText.setVisibility(View.GONE);
        inspectionPhaseText.setVisibility(View.GONE);
        fetchingDataText.setVisibility(View.GONE);

        // get the status of each system
        int btState = bluetoothInfo.getState(this);
        int inspectionPhase = currentInspectionInfo.getPhase(this);
        boolean isFetchingData = pastInspectionsInfo.isUpdating(this);

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
            case Params.CI_DRONE_ACTIVE:
                inspectionPhaseText.setText(R.string.ci_drone_active);
                inspectionPhaseText.setVisibility(View.VISIBLE);
                break;
            case Params.CI_DATA_TRANSFER:
                inspectionPhaseText.setText(R.string.ci_data_transfer);
                inspectionPhaseText.setVisibility(View.VISIBLE);
                break;
            case Params.CI_UPLOADING:
                inspectionPhaseText.setText(R.string.ci_uploading);
                inspectionPhaseText.setVisibility(View.VISIBLE);
                break;
        }
        if (isFetchingData) {
            fetchingDataText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        inView = true;
        if (stateChange){
            // reload
            startActivity(getIntent().setAction(Params.RELOAD).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
            stateChange = false; // state change handled
            finish();
        }
    }

    @Override
    protected void onStop(){
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
                        if (!new CurrentInspectionInfo().isNotInProgress(BaseActivity.this)) {
                            String message = getResources().getString(R.string.cannot_sign_out);
                            new MessageDialog(BaseActivity.this, message).getDialog().show();
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
                        // add client
                    }
                });
                drawer.closeDrawers();
                break;
            case R.id.end_inspection:
                toggle.runWhenIdle(new Runnable() {
                    @Override
                    public void run() {
                        if (currentInspectionInfo.isNotInProgress(BaseActivity.this)) {
                            String message = getString(R.string.no_active_inspection);
                            new MessageDialog(BaseActivity.this, message).getDialog().show();
                        } else {
                            int inspectionPhase = currentInspectionInfo.getPhase(BaseActivity.this);
                            String message;
                            switch (inspectionPhase) {
                                case 1:
                                    new ConfirmEndInspectionDialog(BaseActivity.this).getDialog().show();
                                    break;
                                case 2:
                                    message = getString(R.string.transfer_phase_text);
                                    new MessageDialog(BaseActivity.this, message).getDialog().show();
                                    break;
                                case 3:
                                    message = getString(R.string.upload_phase_text);
                                    new MessageDialog(BaseActivity.this, message).getDialog().show();
                                    break;
                            }
                        }
                    }
                });
                drawer.closeDrawers();
                break;
            case R.id.start_inspection:
                toggle.runWhenIdle(new Runnable() {
                    @Override
                    public void run() {
                        Utilities.attemptInspectionStart(BaseActivity.this);
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

    // used to smooth out drawer open/close animations
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