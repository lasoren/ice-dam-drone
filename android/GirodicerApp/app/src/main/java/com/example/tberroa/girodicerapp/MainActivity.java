package com.example.tberroa.girodicerapp;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {

    private Context app_context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button start_mission = (Button)findViewById(R.id.start_mission);
        start_mission.setOnClickListener(startListener1);
        Button previous_missions = (Button)findViewById(R.id.previous_missions);
        previous_missions.setOnClickListener(startListener2);
        app_context = this.getApplicationContext();
        // initialize Bluetooth Manager
        BluetoothManager btManager;
        btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        // initialize Bluetooth Adapter
        BluetoothAdapter btAdapter = btManager.getAdapter();
        // ensure Bluetooth is enabled. if not, request user permission to enable Bluetooth.
        if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            int REQUEST_ENABLE_BT = 1;
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private OnClickListener startListener1 = new OnClickListener() {
        public void onClick(View v) {

            ServiceStatus service_status = new ServiceStatus(app_context);
            if (!service_status.getServiceStatus()) {   // if there is no mission in progress
                // start new mission
                startService(new Intent(MainActivity.this, AdvertiseService.class));
                Intent start_mission = new Intent(MainActivity.this, ActiveMissionActivity.class);
                startActivity(start_mission);
            }
            else{   // otherwise, there is a mission in progress
                // display alert dialog
                AlertDialog alertDialog = createDialogBox();
                alertDialog.show();
            }
        }
    };

    private OnClickListener startListener2 = new OnClickListener() {
        public void onClick(View v) {
            // go to previous missions
            Intent previous_missions = new Intent(MainActivity.this,PreviousMissionsActivity.class);
            startActivity(previous_missions);
        }
    };

    private AlertDialog createDialogBox(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setMessage(R.string.mission_in_progress)
                .setCancelable(false)
                .setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent current_mission = new Intent(MainActivity.this, ActiveMissionActivity.class);
                        startActivity(current_mission);
                    }
                });
        return alertDialogBuilder.create();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        stopService(new Intent(MainActivity.this, AdvertiseService.class));
    }
}
