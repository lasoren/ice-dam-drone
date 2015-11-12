package com.example.tberroa.girodicerapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.Menu;

public class ActiveMissionActivity extends AppCompatActivity {

    private Context app_context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_mission);

        // set toolbar
        Toolbar activeMissionToolbar = (Toolbar) findViewById(R.id.active_mission_toolbar);
        activeMissionToolbar.setTitle("Current Mission");
        setSupportActionBar(activeMissionToolbar);
        app_context = this.getApplicationContext();
    }

    // populate the navigation
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }

    // implement navigation functions
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.end_mission:
                // user chose the "End Mission" item
                ServiceStatus service_status = new ServiceStatus(app_context);
                if (!service_status.getServiceStatus()) {   // if there is no mission in progress
                    // tell user there is no mission in progress
                    AlertDialog no_active_mission = no_active_mission_createDialog();
                    no_active_mission.show();
                }
                else{   // otherwise, there is a mission in progress
                    // ask user for confirmation to end mission
                    AlertDialog confirm_end_mission = confirm_end_mission_createDialog();
                    confirm_end_mission.show();
                }
                return true;
            case R.id.previous_missions:
                // user chose the "Previous Missions" item, change activity
                Intent previous_missions = new Intent(ActiveMissionActivity.this,PreviousMissionsActivity.class);
                startActivity(previous_missions);
                return true;
            default:
                // the users action was not recognized, invoke the superclass to handle it
                return super.onOptionsItemSelected(item);
        }
    }

    private AlertDialog no_active_mission_createDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
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

    private AlertDialog confirm_end_mission_createDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
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
                        stopService(new Intent(ActiveMissionActivity.this, AdvertiseService.class));

                    }
                });
        return alertDialogBuilder.create();
    }
}
