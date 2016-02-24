package com.example.tberroa.girodicerapp.activities;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.adapters.ClientManagerViewAdapter;
import com.example.tberroa.girodicerapp.database.LocalTestDB;
import com.example.tberroa.girodicerapp.helpers.GridSpacingItemDecoration;
import com.example.tberroa.girodicerapp.helpers.Utilities;
import com.example.tberroa.girodicerapp.models.Client;

import java.util.List;

public class ClientManagerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_manager);

        // get clients (TEST)
        List<Client> clients = new LocalTestDB().getClients();

        // set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Client Manager");
        toolbar.setVisibility(View.VISIBLE);
        setSupportActionBar(toolbar);

        // declare and initialize button
        FloatingActionButton createClientButton = (FloatingActionButton)findViewById(R.id.create_client);
        createClientButton.setOnClickListener(createClientButtonListener);

        // initialize recycler view
        int span = Utilities.getSpanGrid(this);
        int spacing = Utilities.getSpacingGrid(this);
        final RecyclerView recyclerView = (RecyclerView)findViewById(R.id.client_manager_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, span));
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(span, spacing));

        // populate view with clients
        ClientManagerViewAdapter clientManagerViewAdapter;
        clientManagerViewAdapter = new ClientManagerViewAdapter(this, clients);
        recyclerView.setAdapter(clientManagerViewAdapter);
    }

    private final View.OnClickListener createClientButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            // client logic here
        }
    };

}
