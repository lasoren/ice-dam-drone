package com.example.tberroa.girodicerapp.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.adapters.ClientManagerAdapter;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.database.LocalDB;
import com.example.tberroa.girodicerapp.dialogs.CreateClientDialog;
import com.example.tberroa.girodicerapp.helpers.GridSpacingItemDecoration;
import com.example.tberroa.girodicerapp.helpers.Utilities;
import com.example.tberroa.girodicerapp.models.Client;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
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
        Type type = new TypeToken<List<Client>>(){}.getType();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        Log.d("dbg", "@ClientManagerActivity: clients is: " + gson.toJson(clients, type));

        // set toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.client_manager_title);
        }

        // set navigation menu
        navigationView.inflateMenu(R.menu.nav_client_manager);

        // declare and initialize ui elements
        TextView noClients = (TextView) findViewById(R.id.general_message);
        noClients.setText(R.string.no_clients);
        noClients.setVisibility(View.GONE);
        FloatingActionButton createClientButton = (FloatingActionButton) findViewById(R.id.create_client);
        createClientButton.setOnClickListener(createClientButtonListener);

        // initialize recycler view
        int span = Utilities.getSpanGrid(this);
        int spacing = Utilities.getSpacingGrid(this);
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.client_manager_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, span));
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(span, spacing));

        // populate view
        if (!clients.isEmpty()){
            ClientManagerAdapter clientManagerAdapter;
            clientManagerAdapter = new ClientManagerAdapter(this, clients);
            recyclerView.setAdapter(clientManagerAdapter);
        }else{
            noClients.setVisibility(View.VISIBLE);
        }
    }

    private final View.OnClickListener createClientButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            new CreateClientDialog(ClientManagerActivity.this).show();
        }
    };
}
