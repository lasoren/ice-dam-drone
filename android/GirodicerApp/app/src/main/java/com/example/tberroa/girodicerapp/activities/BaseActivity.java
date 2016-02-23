package com.example.tberroa.girodicerapp.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;

import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.data.PastInspectionsInfo;
import com.example.tberroa.girodicerapp.dialogs.ConfirmEndInspectionDialog;
import com.example.tberroa.girodicerapp.dialogs.MessageDialog;
import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.data.ActiveInspectionInfo;
import com.example.tberroa.girodicerapp.data.OperatorInfo;
import com.example.tberroa.girodicerapp.helpers.Utilities;

public class BaseActivity extends AppCompatActivity {

    final OperatorInfo operatorInfo = new OperatorInfo();
    final ActiveInspectionInfo activeInspectionInfo = new ActiveInspectionInfo();
    private final PastInspectionsInfo pastInspectionsInfo = new PastInspectionsInfo();
    private RelativeLayout fetchingData;
    private BroadcastReceiver broadcastReceiver;
    String operatorName;

    @Override
    protected void onStart(){
        super.onStart();

        // if user is not logged in, boot them
        if (!operatorInfo.isLoggedIn(this)){
            // clear operatorName
            operatorInfo.setUsername(this, "");
            // go back to login page
            startActivity(new Intent(BaseActivity.this, SignInActivity.class));
            finish();
        }

        // set notifications if necessary
        TextView missionPhase = (TextView) findViewById(R.id.mission_phase_text);
        fetchingData = (RelativeLayout) findViewById(R.id.fetching_text);
        if (activeInspectionInfo.getMissionPhase(this) != 0){
            int phase = activeInspectionInfo.getMissionPhase(this);
            switch(phase){
                case 1:
                    missionPhase.setText(R.string.phase_1);
                    break;
                case 2:
                    missionPhase.setText(R.string.phase_2);
                    break;
                case 3:
                    missionPhase.setText(R.string.phase_3);
                    break;
            }

            missionPhase.setVisibility(View.VISIBLE);
            missionPhase.animate().translationY(missionPhase.getHeight());
        }
        if (pastInspectionsInfo.isFetching(this)){
            fetchingData.setVisibility(View.VISIBLE);
            fetchingData.animate().translationY(fetchingData.getHeight());
        }

        // set up receiver to handle updating notifications
        IntentFilter filter = new IntentFilter();
        filter.addAction(Params.TRANSFER_STARTED);
        filter.addAction(Params.UPLOAD_STARTED);
        filter.addAction(Params.FETCHING_STARTED);
        filter.addAction(Params.FETCHING_COMPLETE);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch(action) {
                    case Params.TRANSFER_STARTED:
                        startActivity(getIntent());
                        finish();
                        break;
                    case Params.UPLOAD_STARTED:
                        startActivity(getIntent());
                        finish();
                        break;
                    case Params.FETCHING_STARTED:
                        startActivity(getIntent());
                        finish();
                    case Params.FETCHING_COMPLETE:
                        // check which activity is currently in view
                        String currentActivity = context.getClass().getSimpleName();
                        String pMActivity = "PastInspectionsActivity";
                        if (currentActivity.equals(pMActivity)){
                            startActivity(getIntent());
                            finish();
                        }
                        else{
                            fetchingData.setVisibility(View.GONE);
                        }

                }
            }
        };
        registerReceiver(broadcastReceiver, filter);



    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(broadcastReceiver);
    }

    // implement navigation functions
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.end_mission: // user wants to end current mission
                if (activeInspectionInfo.missionNotInProgress(this)){
                    String message = getResources().getString(R.string.no_active_inspection);
                    new MessageDialog(this, message).getDialog().show();
                }
                else { // otherwise
                    int missionPhase = activeInspectionInfo.getMissionPhase(this);
                    String message;
                    switch(missionPhase){
                        case 1:
                            new ConfirmEndInspectionDialog(this).getDialog().show();
                            break;
                        case 2:
                            message = getResources().getString(R.string.transfer_phase_text);
                            new MessageDialog(this, message).getDialog().show();
                            break;
                        case 3:
                            message = getResources().getString(R.string.upload_phase_text);
                            new MessageDialog(this, message).getDialog().show();
                            break;
                    }
                }
                return true;
            case R.id.start_mission: // user wants to start a new mission
                Utilities.AttemptMissionStart(this);
                return true;
            case R.id.current_mission: // user wants to see the current mission
                startActivity(new Intent(this,ActiveInspectionActivity.class));
                finish();
                return true;
            case R.id.previous_missions: // user wants to see previous missions
                startActivity(new Intent(this,PastInspectionsActivity.class));
                finish();
                return true;
            case R.id.delete_previous_missions: // user wants to delete previous missions
                // run delete metadata service
                return true;
            case R.id.logout: // user wants to logout
                // check if there is an active mission
                if (!activeInspectionInfo.missionNotInProgress(this)){ // mission in progress
                    String message = getResources().getString(R.string.cannot_logout);
                    new MessageDialog(this, message).getDialog().show();
                }
                else{
                    // clear all local data
                    operatorInfo.clearAll(this);
                    activeInspectionInfo.clearAll(this);
                    pastInspectionsInfo.clearAll(this);

                    // go back to login page
                    startActivity(new Intent(this, SignInActivity.class));
                    finish();
                }
            default:
                // the users action was not recognized, invoke the superclass to handle it
                return super.onOptionsItemSelected(item);
        }
    }
}