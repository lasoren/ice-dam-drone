package com.example.tberroa.girodicerapp.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.data.PastInspectionsInfo;
import com.example.tberroa.girodicerapp.data.UserInfo;
import com.example.tberroa.girodicerapp.database.LocalDB;
import com.example.tberroa.girodicerapp.dialogs.ConfirmEndInspectionDialog;
import com.example.tberroa.girodicerapp.dialogs.MessageDialog;
import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.data.ActiveInspectionInfo;
import com.example.tberroa.girodicerapp.helpers.Utilities;

public class BaseActivity extends AppCompatActivity {

    private final UserInfo userInfo = new UserInfo();
    final ActiveInspectionInfo activeInspectionInfo = new ActiveInspectionInfo();
    private final PastInspectionsInfo pastInspectionsInfo = new PastInspectionsInfo();
    private RelativeLayout fetchingData;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onStart(){
        super.onStart();

        // if user is not signed in, boot them
        if (!userInfo.isLoggedIn(this)){
            Utilities.SignOut(this);
        }

        // set notifications if necessary
        TextView inspectionPhase = (TextView) findViewById(R.id.inspection_phase_text);
        fetchingData = (RelativeLayout) findViewById(R.id.fetching_text);
        if (activeInspectionInfo.getPhase(this) != 0){
            int phase = activeInspectionInfo.getPhase(this);
            switch(phase){
                case 1:
                    inspectionPhase.setText(R.string.phase_1);
                    break;
                case 2:
                    inspectionPhase.setText(R.string.phase_2);
                    break;
                case 3:
                    inspectionPhase.setText(R.string.phase_3);
                    break;
            }

            inspectionPhase.setVisibility(View.VISIBLE);
            inspectionPhase.animate().translationY(inspectionPhase.getHeight());
        }
        if (pastInspectionsInfo.isUpdating(this)){
            fetchingData.setVisibility(View.VISIBLE);
            fetchingData.animate().translationY(fetchingData.getHeight());
        }

        // set up receiver to handle updating notifications
        IntentFilter filter = new IntentFilter();
        filter.addAction(Params.TRANSFER_STARTED);
        filter.addAction(Params.UPLOAD_STARTED);
        filter.addAction(Params.UPDATING_STARTED);
        filter.addAction(Params.UPDATING_COMPLETE);
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
                    case Params.UPDATING_STARTED:
                        startActivity(getIntent());
                        finish();
                    case Params.UPDATING_COMPLETE:
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
            case R.id.end_inspection:
                if (activeInspectionInfo.isNotInProgress(this)){
                    String message = getResources().getString(R.string.no_active_inspection);
                    new MessageDialog(this, message).getDialog().show();
                }
                else {
                    int inspectionPhase = activeInspectionInfo.getPhase(this);
                    String message;
                    switch(inspectionPhase){
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
            case R.id.start_inspection:
                Utilities.AttemptInspectionStart(this);
                return true;
            case R.id.current_inspection:
                startActivity(new Intent(this,ActiveInspectionActivity.class));
                finish();
                return true;
            case R.id.past_inspections:
                startActivity(new Intent(this,PastInspectionsActivity.class));
                finish();
                return true;
            case R.id.delete_past_inspections:
                // run delete metadata service
                return true;
            case R.id.sign_out:
                // check if there is an ongoing active inspection
                if (!activeInspectionInfo.isNotInProgress(this)){
                    String message = getResources().getString(R.string.cannot_sign_out);
                    new MessageDialog(this, message).getDialog().show();
                }
                else{
                    Utilities.SignOut(this);
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // populate the navigation
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        String operatorName = new LocalDB().getOperator().user.first_name;
        getMenuInflater().inflate(R.menu.navigation, menu);
        MenuItem item = menu.findItem(R.id.title);
        item.setTitle("Logged in as: " + operatorName);
        return true;
    }

}