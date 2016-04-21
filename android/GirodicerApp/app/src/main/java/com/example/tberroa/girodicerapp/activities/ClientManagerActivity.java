package com.example.tberroa.girodicerapp.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.activeandroid.ActiveAndroid;
import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.adapters.ClientManagerAdapter;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.database.LocalDB;
import com.example.tberroa.girodicerapp.database.ServerDB;
import com.example.tberroa.girodicerapp.dialogs.CreateClientDialog;
import com.example.tberroa.girodicerapp.helpers.GridSpacingItemDecoration;
import com.example.tberroa.girodicerapp.helpers.Utilities;
import com.example.tberroa.girodicerapp.models.Client;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class ClientManagerActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    private List<Client> clients;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ClientManagerAdapter clientManagerAdapter;
    private final View.OnClickListener createClientButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            new CreateClientDialog(ClientManagerActivity.this).show();
        }
    };

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
        clients = localDB.getClients();
        Type type = new TypeToken<List<Client>>() {
        }.getType();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        Log.d(Params.TAG_DBG, "@ClientManagerActivity: clients is: " + gson.toJson(clients, type));

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

        // initialize swipe refresh layout
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        // initialize recycler view
        int span = Utilities.getSpanGrid(this);
        int spacing = Utilities.getSpacingGrid(this);
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.client_manager_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, span));
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(span, spacing));

        // populate view
        if (!clients.isEmpty()) {
            clientManagerAdapter = new ClientManagerAdapter(this, clients);
            recyclerView.setAdapter(clientManagerAdapter);
        } else {
            noClients.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        new UpdateClients().execute();
    }

    private class UpdateClients extends AsyncTask<Void, Void, Void> {

        private List<Client> newClients;
        private boolean foundNewClients;

        @Override
        protected Void doInBackground(Void... params) {
            if (Utilities.isInternetAvailable(ClientManagerActivity.this)) {
                newClients = new ServerDB(ClientManagerActivity.this).getClients();
                if (newClients != null && !newClients.isEmpty()) {
                    // save new clients locally
                    ActiveAndroid.beginTransaction();
                    try {
                        for (Client client : newClients) {
                            if (client.user != null) {
                                client.cascadeSave();
                            }
                        }
                        ActiveAndroid.setTransactionSuccessful();
                    } finally {
                        ActiveAndroid.endTransaction();
                    }
                    foundNewClients = true;
                } else {
                    foundNewClients = false;
                }
            } else {
                foundNewClients = false;
            }
            return null;
        }

        protected void onPostExecute(Void param) {
            if (foundNewClients) {
                // update the recycler view
                clients.addAll(0, newClients);
                clientManagerAdapter.notifyDataSetChanged();
            }
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
