package com.example.tberroa.girodicerapp.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.data.PastInspectionsInfo;
import com.example.tberroa.girodicerapp.data.UserInfo;
import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.data.ActiveInspectionInfo;
import com.example.tberroa.girodicerapp.helpers.Utilities;

public class BaseActivity extends AppCompatActivity {

    private final UserInfo userInfo = new UserInfo();
    final ActiveInspectionInfo activeInspectionInfo = new ActiveInspectionInfo();
    private final PastInspectionsInfo pastInspectionsInfo = new PastInspectionsInfo();
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onStart(){
        super.onStart();

        // if user is not signed in, boot them
        if (!userInfo.isLoggedIn(this)){
            Utilities.signOut(this);
        }

        // declare notifications
        final TextView inspectionPhase = (TextView) findViewById(R.id.inspection_phase_text);
        final RelativeLayout fetchingData = (RelativeLayout) findViewById(R.id.fetching_text);

        // check if either needs to be set
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
        }
        if (pastInspectionsInfo.isUpdating(this)){
            fetchingData.setVisibility(View.VISIBLE);
        }

        // set up receiver to handle updating notifications
        IntentFilter filter = new IntentFilter();
        filter.addAction(Params.TRANSFER_STARTED);
        filter.addAction(Params.UPLOAD_STARTED);
        filter.addAction(Params.UPLOAD_COMPLETE);
        filter.addAction(Params.UPDATING_STARTED);
        filter.addAction(Params.UPDATING_COMPLETE);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch(action) {
                    case Params.TRANSFER_STARTED:
                        startActivity(getIntent().setAction(Params.RELOAD).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        finish();
                        break;
                    case Params.UPLOAD_STARTED:
                        startActivity(getIntent().setAction(Params.RELOAD).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        finish();
                        break;
                    case Params.UPLOAD_COMPLETE:
                        startActivity(getIntent().setAction(Params.RELOAD).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        finish();
                        break;
                    case Params.UPDATING_STARTED:
                        startActivity(getIntent());
                        finish();
                        break;
                    case Params.UPDATING_COMPLETE:
                        startActivity(getIntent().setAction(Params.RELOAD).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        finish();
                        break;
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
}