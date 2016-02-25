package com.example.tberroa.girodicerapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.tberroa.girodicerapp.data.ClientId;
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

        // get inspections relating to this client
        List<Inspection> inspections = localDB.getInspections(localDB.getClient(new ClientId().get(this)));

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


        // initialize recycler view
        int span = Utilities.getSpanGrid(this);
        int spacing = Utilities.getSpacingGrid(this);
        final RecyclerView recyclerView = (RecyclerView)findViewById(R.id.past_inspections_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, span));
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(span, spacing));

        // populate view with past inspections
        PastInspectionsViewAdapter pastInspectionsViewAdapter;
        pastInspectionsViewAdapter = new PastInspectionsViewAdapter(this, inspections);
        recyclerView.setAdapter(pastInspectionsViewAdapter);
    }

    @Override
    public void onBackPressed() {
        new ClientId().clear(this);
        startActivity(new Intent(this, ClientManagerActivity.class));
        finish();
    }
}
