package com.example.tberroa.girodicerapp.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.data.Params;

public class ActiveInspectionActivity extends BaseActivity {

    private BroadcastReceiver broadcastReceiver;
    private TextView noActiveInspectionText;
    private TextView activeInspectionText;
    private TextView transferPhaseText;
    private TextView uploadPhaseText;
    private ProgressBar loadingSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_inspection);

        // set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Current Inspection");
        toolbar.setVisibility(View.VISIBLE);
        setSupportActionBar(toolbar);

        // initialize view elements
        noActiveInspectionText = (TextView) findViewById(R.id.no_active_inspection_text);
        activeInspectionText = (TextView) findViewById(R.id.active_inspection_text);
        transferPhaseText = (TextView) findViewById(R.id.transfer_phase_text);
        uploadPhaseText = (TextView) findViewById(R.id.upload_phase_text);
        loadingSpinner = (ProgressBar) findViewById(R.id.loading_spinner);

        // populate view according to inspection phase
        int inspectionPhase = activeInspectionInfo.getPhase(this);
        PopulateView(inspectionPhase);

        // set up receiver to update activity as necessary
        IntentFilter filter = new IntentFilter();
        filter.addAction(Params.RELOAD_AM_ACTIVITY);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // update view according to inspection phase
                int phase = activeInspectionInfo.getPhase(ActiveInspectionActivity.this);
                PopulateView(phase);
            }
        };
        registerReceiver(broadcastReceiver, filter);

    }

    private void PopulateView(int inspectionPhase){
        noActiveInspectionText.setVisibility(View.GONE);
        activeInspectionText.setVisibility(View.GONE);
        transferPhaseText.setVisibility(View.GONE);
        uploadPhaseText.setVisibility(View.GONE);
        loadingSpinner.setVisibility(View.GONE);
        switch (inspectionPhase) {
            case 0:
                noActiveInspectionText.setVisibility(View.VISIBLE);

                break;
            case 1:
                activeInspectionText.setVisibility(View.VISIBLE);
                break;
            case 2:
                transferPhaseText.setVisibility(View.VISIBLE);
                loadingSpinner.setVisibility(View.VISIBLE);
                break;
            case 5:
                transferPhaseText.setVisibility(View.VISIBLE);
                loadingSpinner.setVisibility(View.VISIBLE);
                break;
            case 3:
                uploadPhaseText.setVisibility(View.VISIBLE);
                loadingSpinner.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
}
