package com.example.tberroa.girodicerapp.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.data.ClientId;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.dialogs.ConfirmEndInspectionDialog;
import com.example.tberroa.girodicerapp.dialogs.MessageDialog;
import com.example.tberroa.girodicerapp.helpers.Utilities;

public class ActiveInspectionActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

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

        // initialize drawer layout
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // initialize navigation view
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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
    public void onBackPressed() {
        new ClientId().clear(this);
        startActivity(new Intent(this, ClientManagerActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
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
                Utilities.attemptInspectionStart(this);
                return true;
            case R.id.current_inspection:
                startActivity(new Intent(this,ActiveInspectionActivity.class));
                finish();
                return true;
            case R.id.past_inspections:
                startActivity(new Intent(this,PastInspectionsActivity.class));
                finish();
                return true;
            case R.id.sign_out:
                // check if there is an ongoing active inspection
                if (!activeInspectionInfo.isNotInProgress(this)){
                    String message = getResources().getString(R.string.cannot_sign_out);
                    new MessageDialog(this, message).getDialog().show();
                }
                else{
                    Utilities.signOut(this);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
