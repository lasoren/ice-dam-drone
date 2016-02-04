package com.example.tberroa.girodicerapp.activities;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.tberroa.girodicerapp.data.PreviousMissionsInfo;
import com.example.tberroa.girodicerapp.helpers.Utilities;
import com.example.tberroa.girodicerapp.services.ActiveMissionService;
import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.data.MissionStatus;
import com.example.tberroa.girodicerapp.data.UserInfo;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        // get user login status
        Boolean isLoggedIn = new UserInfo().isLoggedIn(this);

        if (!isLoggedIn){ // if user is not logged in, boot them
            // clear username
            new UserInfo().setUsername(this, "");
            // go back to login page
            startActivity(new Intent(BaseActivity.this, LoginActivity.class));
            finish();
        }
    }

    // implement navigation functions
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MissionStatus missionStatus = new MissionStatus();
        switch (item.getItemId()) {
            case R.id.end_mission: // user wants to end current mission
                if (!missionStatus.isMissionInProgress(this)) { // if there is no mission in progress
                    noActiveMissionDialog(this).show();  // notify user
                }
                else{ // otherwise
                    confirmEndMissionDialog(this).show(); // request confirmation for termination
                }
                return true;
            case R.id.start_mission: // user wants to start a new mission
                if (!missionStatus.isMissionInProgress(this)) { // if there is no mission in progress
                    Utilities.startActiveMission(this);
                    startActivity(new Intent(this,ActiveMissionActivity.class));
                    finish();
                }
                else{ // otherwise, there is a mission in progress
                    // notify user that a mission is already in progress
                    missionInProgressDialog(this).show();
                }
                return true;
            case R.id.current_mission: // user wants to see the current mission
                startActivity(new Intent(this,ActiveMissionActivity.class));
                finish();
                return true;
            case R.id.previous_missions: // user wants to see previous missions
                startActivity(new Intent(this,PreviousMissionsActivity.class));
                finish();
                return true;
            case R.id.delete_previous_missions: // user wants to delete previous missions
                // run delete metadata service
                return true;
            case R.id.logout: // user wants to logout
                UserInfo userInfo = new UserInfo();
                userInfo.setUsername(getApplicationContext(), ""); // clear username
                userInfo.setUserStatus(getApplicationContext(), false); // update status
                // go back to login page
                startActivity(new Intent(this,LoginActivity.class));
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
        BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        // ensure Bluetooth is enabled. if not, request user permission to enable Bluetooth
        if (!bluetoothAdapter.isEnabled()) { // if bluetooth is not enabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            int REQUEST_ENABLE_BT = 1;
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }
}