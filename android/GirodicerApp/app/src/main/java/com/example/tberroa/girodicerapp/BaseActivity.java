package com.example.tberroa.girodicerapp;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        // get user login status
        UserInfo userInfo = new UserInfo();
        Boolean userLoggedOn = userInfo.getUserStatus(this.getApplicationContext());

        if (!userLoggedOn){ // if user has not logged in, boot them
            // make sure username is reset
            userInfo.setUsername(this.getApplicationContext(), "");
            // go back to login page
            Intent logout = new Intent(this,LoginActivity.class);
            startActivity(logout);
            finish();
        }

    }

    // implement navigation functions
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ServiceStatus serviceStatus = new ServiceStatus(this.getApplicationContext());
        switch (item.getItemId()) {
            case R.id.end_mission:
                if (!serviceStatus.isServiceRunning()) { // if there is no mission in progress
                    noActiveMissionDialog(this).show();
                }
                else{ // otherwise, there is a mission in progress
                    confirmEndMissionDialog(this).show();
                }
                return true;
            case R.id.start_mission:
                if (!serviceStatus.isServiceRunning()) { // if there is no mission in progress
                    // start the active mission service
                    startService(new Intent(this, ActiveMissionService.class));
                    // go to active mission activity
                    Intent activeMission = new Intent(this,ActiveMissionActivity.class);
                    startActivity(activeMission);
                    finish();
                }
                else{ // otherwise, there is a mission in progress
                    // tell user a mission is already in progress
                    missionInProgressDialog(this).show();
                }
                return true;
            case R.id.current_mission:
                Intent activeMission = new Intent(this,ActiveMissionActivity.class);
                startActivity(activeMission);
                finish();
                return true;
            case R.id.previous_missions:
                Intent previousMissions = new Intent(this,PreviousMissionsActivity.class);
                startActivity(previousMissions);
                finish();
                return true;
            case R.id.delete_previous_missions:
                // run delete metadata service
                return true;
            case R.id.logout:
                // update user info
                UserInfo userInfo = new UserInfo();
                userInfo.setUsername(getApplicationContext(), "");
                userInfo.setUserStatus(getApplicationContext(), false);
                // go back to login page
                Intent logout = new Intent(this,LoginActivity.class);
                startActivity(logout);
                finish();
            default:
                // the users action was not recognized, invoke the superclass to handle it
                return super.onOptionsItemSelected(item);
        }
    }

    // alert dialogs -------------------------------------------------------------------------------
    protected AlertDialog noActiveMissionDialog(Context context) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder
                .setMessage(R.string.no_active_mission)
                .setCancelable(true)
                .setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        return alertDialogBuilder.create();
    }

    protected AlertDialog confirmEndMissionDialog(final Context context) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder
                .setMessage(R.string.confirm_end_mission)
                .setCancelable(true)
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        stopService(new Intent(context, ActiveMissionService.class));
                    }
                });
        return alertDialogBuilder.create();
    }

    protected AlertDialog missionInProgressDialog(final Context context) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setMessage(R.string.mission_in_progress)
                .setCancelable(false)
                .setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent activeMission = new Intent(context, ActiveMissionActivity.class);
                        startActivity(activeMission);
                        finish();
                    }
                });
        return alertDialogBuilder.create();
    }
    // ---------------------------------------------------------------------------------------------

    protected void enableBluetooth(){
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        // ensure Bluetooth is enabled. if not, request user permission to enable Bluetooth.
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            int REQUEST_ENABLE_BT = 1;
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }
}