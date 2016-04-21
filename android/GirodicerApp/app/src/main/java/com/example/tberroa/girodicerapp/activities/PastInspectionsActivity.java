package com.example.tberroa.girodicerapp.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.activeandroid.ActiveAndroid;
import com.example.tberroa.girodicerapp.data.ClientId;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.database.LocalDB;
import com.example.tberroa.girodicerapp.database.ServerDB;
import com.example.tberroa.girodicerapp.helpers.GridSpacingItemDecoration;
import com.example.tberroa.girodicerapp.adapters.PastInspectionsAdapter;
import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.helpers.Utilities;
import com.example.tberroa.girodicerapp.models.Inspection;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class PastInspectionsActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    private List<Inspection> inspections;
    private SwipeRefreshLayout swipeRefreshLayout;
    private PastInspectionsAdapter pastInspectionsAdapter;

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
        inspections = localDB.getInspections(new ClientId().get(this));

        // create list of ids, paths, and labels to be sent to the view adapter
        List<Integer> ids = new ArrayList<>();
        List<String> paths = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        if (inspections != null && !inspections.isEmpty()) {
            for (Iterator<Inspection> iterator = inspections.listIterator(); iterator.hasNext(); ) {
                Inspection inspection = iterator.next();
                String path = localDB.getInspectionThumbnail(inspection.id);
                if (path != null) {
                    ids.add(inspection.id);
                    paths.add(path);
                    try {
                        Date date = Params.DATE_FORMAT.parse(inspection.created);
                        String dateLabel = Params.OUT_FORMAT.format(date);
                        labels.add(dateLabel);
                    } catch (ParseException e) {
                        labels.add(inspection.created);
                    }
                } else { // delete any inspections which cant produce a path. inspection contains corrupted data
                    iterator.remove();
                    inspection.delete();
                }
            }
        }

        // set toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.past_inspections_title);
        }

        // initialize back button
        toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.back_button));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    new ClientId().clear(PastInspectionsActivity.this);
                    startActivity(new Intent(PastInspectionsActivity.this, ClientManagerActivity.class));
                    finish();
                }
            }
        });

        // set navigation menu
        navigationView.inflateMenu(R.menu.nav_client_inspections);

        // initialize ui elements, these only appear if there are no past inspections to show
        TextView noInspectionsText = (TextView) findViewById(R.id.general_message);
        noInspectionsText.setText(R.string.no_past_inspections);
        noInspectionsText.setVisibility(View.GONE);
        Button startInspectionButton = (Button) findViewById(R.id.start_inspection);
        startInspectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PastInspectionsActivity.this, CurrentOneActivity.class));
                finish();
            }
        });
        startInspectionButton.setVisibility(View.GONE);

        // initialize swipe refresh layout
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        // initialize recycler view, this only appears if there are past inspections to show
        int span = Utilities.getSpanGrid(this);
        int spacing = Utilities.getSpacingGrid(this);
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.past_inspections_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, span));
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(span, spacing));
        recyclerView.setVisibility(View.GONE);

        // check if this client has past inspections
        if (inspections != null && !inspections.isEmpty()) {
            // populate view with past inspections
            pastInspectionsAdapter = new PastInspectionsAdapter(this, ids, paths, labels);
            recyclerView.setAdapter(pastInspectionsAdapter);
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

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        new UpdateInspections().execute();
    }

    private class UpdateInspections extends AsyncTask<Void, Void, Void> {

        private List<Inspection> newInspections;
        private boolean foundNewInspections;

        @Override
        protected Void doInBackground(Void... params) {
            if (Utilities.isInternetAvailable(PastInspectionsActivity.this)) {
                newInspections = new ServerDB(PastInspectionsActivity.this).getInspections();
                if (newInspections != null && !newInspections.isEmpty()) {
                    // save new inspections locally
                    ActiveAndroid.beginTransaction();
                    try {
                        for (Inspection inspection : newInspections) {
                            inspection.cascadeSave();
                        }
                        ActiveAndroid.setTransactionSuccessful();
                    } finally {
                        ActiveAndroid.endTransaction();
                    }
                    foundNewInspections = true;
                } else {
                    foundNewInspections = false;
                }
            } else {
                foundNewInspections = false;
            }
            return null;
        }

        protected void onPostExecute(Void param) {
            if (foundNewInspections) {
                // update the recycler view
                inspections.addAll(0, newInspections);
                pastInspectionsAdapter.notifyDataSetChanged();
            }
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
