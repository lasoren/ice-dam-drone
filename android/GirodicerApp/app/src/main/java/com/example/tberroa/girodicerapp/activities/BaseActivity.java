package com.example.tberroa.girodicerapp.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.tberroa.girodicerapp.data.PreviousMissionsInfo;
import com.example.tberroa.girodicerapp.dialogs.CannotLogOutDialog;
import com.example.tberroa.girodicerapp.dialogs.ConfirmEndMissionDialog;
import com.example.tberroa.girodicerapp.dialogs.CurrentlyTransferringDialog;
import com.example.tberroa.girodicerapp.dialogs.CurrentlyUploadingDialog;
import com.example.tberroa.girodicerapp.dialogs.MissionInProgressDialog;
import com.example.tberroa.girodicerapp.dialogs.NoActiveMissionDialog;
import com.example.tberroa.girodicerapp.helpers.Utilities;
import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.data.ActiveMissionInfo;
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
        ActiveMissionInfo activeMissionInfo = new ActiveMissionInfo();
        switch (item.getItemId()) {
            case R.id.end_mission: // user wants to end current mission
                if (activeMissionInfo.missionNotInProgress(this)){
                    new NoActiveMissionDialog(this).getDialog().show();  // notify user
                }
                else { // otherwise
                    int missionPhase = new ActiveMissionInfo().getMissionPhase(this);
                    switch(missionPhase){
                        case 1:
                            new ConfirmEndMissionDialog(this).getDialog().show();
                            break;
                        case 2:
                            new CurrentlyTransferringDialog(this).getDialog().show();
                            break;
                        case 5:
                            new CurrentlyTransferringDialog(this).getDialog().show();
                            break;
                        case 3:
                            new CurrentlyUploadingDialog(this).getDialog().show();
                            break;
                    }
                }
                return true;
            case R.id.start_mission: // user wants to start a new mission
                if (activeMissionInfo.missionNotInProgress(this)){
                    Utilities.startDroneService(this);
                    startActivity(new Intent(this,ActiveMissionActivity.class));
                    finish();
                }
                else{ // otherwise, there is a mission in progress
                    // notify user that a mission is already in progress
                    new MissionInProgressDialog(this).getDialog().show();
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
                // check if there is an active mission
                if (!new ActiveMissionInfo().missionNotInProgress(this)){ // mission in progress
                    new CannotLogOutDialog(this).getDialog().show();
                }
                else{
                    // clear user info
                    new UserInfo().clearUserInfo(this);
                    // clear active mission info
                    new ActiveMissionInfo().clearAll(this);
                    // clear previous missions info
                    new PreviousMissionsInfo().clearAll(this);
                    // go back to login page
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                }
            default:
                // the users action was not recognized, invoke the superclass to handle it
                return super.onOptionsItemSelected(item);
        }
    }
}