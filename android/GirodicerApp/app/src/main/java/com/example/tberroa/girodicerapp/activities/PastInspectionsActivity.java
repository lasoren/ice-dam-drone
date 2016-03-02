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

        if (getIntent().getAction() != null){
            overridePendingTransition(0, 0);
        }

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

        // initialize text view within drawer navigation
        View headerLayout = navigationView.getHeaderView(0);
        TextView operatorName = (TextView) headerLayout.findViewById(R.id.operator_name);
        operatorName.setText(new LocalDB().getOperator().user.first_name);

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
        return (Utilities.inspectionMenu(item.getItemId(), this))|| super.onOptionsItemSelected(item);
    }
}
