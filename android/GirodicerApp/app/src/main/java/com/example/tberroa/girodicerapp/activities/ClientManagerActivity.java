package com.example.tberroa.girodicerapp.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.adapters.ClientManagerViewAdapter;
import com.example.tberroa.girodicerapp.data.ActiveInspectionInfo;
import com.example.tberroa.girodicerapp.database.LocalDB;
import com.example.tberroa.girodicerapp.dialogs.MessageDialog;
import com.example.tberroa.girodicerapp.helpers.GridSpacingItemDecoration;
import com.example.tberroa.girodicerapp.helpers.Utilities;
import com.example.tberroa.girodicerapp.models.Client;

import java.util.List;

public class ClientManagerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_manager);
        LocalDB localDB = new LocalDB();

        if (getIntent().getAction() != null){
            overridePendingTransition(0, 0);
        }

        // get clients
        List<Client> clients = localDB.getClients();

        // set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Client Manager");
        toolbar.setVisibility(View.VISIBLE);
        setSupportActionBar(toolbar);

        // declare and initialize button
        FloatingActionButton createClientButton = (FloatingActionButton)findViewById(R.id.create_client);
        createClientButton.setOnClickListener(createClientButtonListener);

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

        }
    };

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.sign_out) {
            // check if there is an ongoing active inspection
            if (!new ActiveInspectionInfo().isNotInProgress(this)){
                String message = getResources().getString(R.string.cannot_sign_out);
                new MessageDialog(this, message).getDialog().show();
            }
            else{
                Utilities.signOut(this);
            }
        }
        else if(id == R.id.add_client){
            // add client
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
