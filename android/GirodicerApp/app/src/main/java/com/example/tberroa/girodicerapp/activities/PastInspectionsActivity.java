package com.example.tberroa.girodicerapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.tberroa.girodicerapp.data.ClientId;
import com.example.tberroa.girodicerapp.data.PastInspectionsInfo;
import com.example.tberroa.girodicerapp.database.LocalDB;
import com.example.tberroa.girodicerapp.dialogs.ConfirmEndInspectionDialog;
import com.example.tberroa.girodicerapp.dialogs.MessageDialog;
import com.example.tberroa.girodicerapp.helpers.GridSpacingItemDecoration;
import com.example.tberroa.girodicerapp.adapters.PastInspectionsViewAdapter;
import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.helpers.Utilities;
import com.example.tberroa.girodicerapp.models.Inspection;

import java.util.List;

public class PastInspectionsActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_inspections);
        LocalDB localDB = new LocalDB();

        // get inspections relating to this client
        List<Inspection> inspections = localDB.getInspections(localDB.getClient(new ClientId().get(this)));

        // set most recent inspection id (id of most recent inspection. used for inspection creation later)
        if (inspections.size() != 0){
            int id = inspections.get(0).id; // inspections list is ordered by descending id
            new PastInspectionsInfo().setMostRecentId(this, id);
        }

        // set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Past Inspections");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.back_button);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ClientId().clear(PastInspectionsActivity.this);
                startActivity(new Intent(PastInspectionsActivity.this, ClientManagerActivity.class));
                finish();
            }
        });
        toolbar.setVisibility(View.VISIBLE);

        // initialize drawer layout
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // initialize navigation view
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // check to see if their are any inspections
        if (inspections.size() != 0){
            // initialize recycler view
            int span = Utilities.getSpanGrid(this);
            int spacing = Utilities.getSpacingGrid(this);
            final RecyclerView recyclerView = (RecyclerView)findViewById(R.id.past_inspections_recycler_view);
            recyclerView.setLayoutManager(new GridLayoutManager(this, span));
            recyclerView.addItemDecoration(new GridSpacingItemDecoration(span, spacing));
            recyclerView.setVisibility(View.VISIBLE);

            // populate view with past inspections
            PastInspectionsViewAdapter pastInspectionsViewAdapter;
            pastInspectionsViewAdapter = new PastInspectionsViewAdapter(this, inspections);
            recyclerView.setAdapter(pastInspectionsViewAdapter);
        }
        else{ // no inspections
            // initialize text view and button
            TextView noInspectionsText = (TextView) findViewById(R.id.no_past_inspections_text);
            noInspectionsText.setVisibility(View.VISIBLE);
            Button startInspectionButton = (Button) findViewById(R.id.start_inspection);
            startInspectionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utilities.attemptInspectionStart(PastInspectionsActivity.this);
                }
            });
            startInspectionButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        new ClientId().clear(this);
        startActivity(new Intent(this, ClientManagerActivity.class));
        finish();
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
