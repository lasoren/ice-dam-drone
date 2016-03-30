package com.example.tberroa.girodicerapp.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.adapters.ClientManagerViewAdapter;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.database.LocalDB;
import com.example.tberroa.girodicerapp.dialogs.CreateClientDialog;
import com.example.tberroa.girodicerapp.helpers.GridSpacingItemDecoration;
import com.example.tberroa.girodicerapp.helpers.Utilities;
import com.example.tberroa.girodicerapp.models.Client;

import java.util.List;

public class ClientManagerActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_manager);
        LocalDB localDB = new LocalDB();

        // no animation if starting due to a reload
        String action = getIntent().getAction();
        if (action != null && action.equals(Params.RELOAD)) {
            overridePendingTransition(0, 0);
        }

        // get clients
        List<Client> clients = localDB.getClients();

        // set toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.client_manager_title);
        }

        // set navigation menu
        navigationView.inflateMenu(R.menu.nav_client_manager);

        // declare and initialize button
        FloatingActionButton createClientButton = (FloatingActionButton) findViewById(R.id.create_client);
        createClientButton.setOnClickListener(createClientButtonListener);

        // initialize recycler view
        int span = Utilities.getSpanGrid(this);
        int spacing = Utilities.getSpacingGrid(this);
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.client_manager_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, span));
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(span, spacing));

        // populate view
        ClientManagerViewAdapter clientManagerViewAdapter;
        clientManagerViewAdapter = new ClientManagerViewAdapter(this, clients);
        recyclerView.setAdapter(clientManagerViewAdapter);
    }

    private final View.OnClickListener createClientButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            new CreateClientDialog(ClientManagerActivity.this).show();
        }
    };
}
