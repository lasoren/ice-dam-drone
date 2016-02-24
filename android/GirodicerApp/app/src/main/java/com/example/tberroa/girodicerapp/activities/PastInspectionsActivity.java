package com.example.tberroa.girodicerapp.activities;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.tberroa.girodicerapp.database.LocalTestDB;
import com.example.tberroa.girodicerapp.helpers.GridSpacingItemDecoration;
import com.example.tberroa.girodicerapp.adapters.PastInspectionsViewAdapter;
import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.helpers.Utilities;
import com.example.tberroa.girodicerapp.models.Client;
import com.example.tberroa.girodicerapp.models.Inspection;

import java.util.List;

public class PastInspectionsActivity extends BaseActivity {

     //private int operatorId;  // TEST code

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_inspections);

        // LIVE
        //LocalDB localDB = new LocalDB();

        // TEST
        LocalTestDB localTestDB = new LocalTestDB();

        // get operator (LIVE)
        //userInfo = new OperatorId().get(this);
        //DroneOperator operator = new LocalDB().getOperator(userInfo);

        // get client (LIVE)
        //int clientId = new ClientId().get(this);
        //Client client = new LocalDB().getClient(clientId);

        // get inspections belonging to that operator&client combination (LIVE)
        //List<Inspection> inspections = localDB.getInspections(operator, client);

        // get inspections belonging to that operator&client combination (TEST)
        Client client = localTestDB.getClient();
        List<Inspection> inspections = localTestDB.getInspections(client);
        //String url = localTestDB.getInspectionImages(inspections.get(0), "aerial").get(0).link;
        String size = Integer.toString(inspections.size());
        Log.d("test1", "number of inspections pulled: "+size);

        // set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Past Inspections");
        toolbar.setVisibility(View.VISIBLE);
        setSupportActionBar(toolbar);

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

    // populate the navigation
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // get operator name (LIVE)
        //String operatorName = new LocalDB().getOperator(userInfo).user.first_name;

        // get operator name (TEST)
        String operatorName = new LocalTestDB().getOperator().user.first_name;

        getMenuInflater().inflate(R.menu.navigation, menu);
        MenuItem item = menu.findItem(R.id.title);
        item.setTitle("Logged in as: " + operatorName);
        return true;
    }

}
