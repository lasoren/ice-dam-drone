package com.example.tberroa.girodicerapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.tberroa.girodicerapp.data.ClientId;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.data.PastInspectionsInfo;
import com.example.tberroa.girodicerapp.database.LocalDB;
import com.example.tberroa.girodicerapp.helpers.GridSpacingItemDecoration;
import com.example.tberroa.girodicerapp.adapters.PastInspectionsViewAdapter;
import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.helpers.Utilities;
import com.example.tberroa.girodicerapp.models.Inspection;

import java.util.List;

public class PastInspectionsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_inspections);
        LocalDB localDB = new LocalDB();

        // no animation if starting due to a reload
        String action = getIntent().getAction();
        if (action != null && action.equals(Params.RELOAD)) {
            overridePendingTransition(0, 0);
        }

        // get inspections relating to this client
        List<Inspection> inspections = localDB.getInspections(localDB.getClient(new ClientId().get(this)));

        // set toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.past_inspections_title);
        }

        // set navigation menu
        navigationView.inflateMenu(R.menu.nav_client_inspections);

        // initialize ui elements, these only appear if there are no past inspections to show
        TextView noInspectionsText = (TextView) findViewById(R.id.no_past_inspections_text);
        noInspectionsText.setVisibility(View.GONE);
        Button startInspectionButton = (Button) findViewById(R.id.start_inspection);
        startInspectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utilities.attemptInspectionStart(PastInspectionsActivity.this);
            }
        });
        startInspectionButton.setVisibility(View.GONE);

        // initialize recycler view, this only appears if there are past inspections to show
        int span = Utilities.getSpanGrid(this);
        int spacing = Utilities.getSpacingGrid(this);
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.past_inspections_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, span));
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(span, spacing));
        recyclerView.setVisibility(View.GONE);

        // check if this client has past inspections
        if (inspections != null && !inspections.isEmpty()) {
            // set most recent inspection id (used for inspection creation later)
            int id = inspections.get(0).id;
            new PastInspectionsInfo().setMostRecentId(this, id);

            // populate view with past inspections
            PastInspectionsViewAdapter pastInspectionsViewAdapter;
            pastInspectionsViewAdapter = new PastInspectionsViewAdapter(this, inspections);
            recyclerView.setAdapter(pastInspectionsViewAdapter);
            recyclerView.setVisibility(View.VISIBLE);
        } else { // no past inspections
            // display text view and button
            noInspectionsText.setVisibility(View.VISIBLE);
            startInspectionButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            new ClientId().clear(this);
            startActivity(new Intent(this, ClientManagerActivity.class));
            finish();
        }
    }
}
