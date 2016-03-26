package com.example.tberroa.girodicerapp.activities;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.data.ClientId;
import com.example.tberroa.girodicerapp.database.LocalDB;
import com.example.tberroa.girodicerapp.helpers.Utilities;
import com.example.tberroa.girodicerapp.services.BluetoothService;

public class CurrentOneActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    // constants
    private final int REQUEST_ENABLE_BT = 1;

    // ui elements
    private Button startButton, nextButton;
    private TextView generalMessage, loadingMessage;
    private ProgressBar loadingSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_one);

        // if starting activity from a reload call
        if (getIntent().getAction() != null) {
            // no animation
            overridePendingTransition(0, 0);
        }

        // check if user has already completed the bluetooth connect phase
        int phase = activeInspectionInfo.getPhase(this);
        if (phase == -50 || (phase != 0 && phase > -2)){
            // go to next activity
            startActivity(new Intent(this, CurrentTwoActivity.class));
            finish();
        }

        // get bluetooth adapter
        final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

        // set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Current Inspection");
        toolbar.setVisibility(View.VISIBLE);
        setSupportActionBar(toolbar);

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

        // initialize ui elements
        startButton = (Button) findViewById(R.id.start_button);
        nextButton = (Button) findViewById(R.id.next_button);
        generalMessage = (TextView) findViewById(R.id.general_message);
        loadingMessage = (TextView) findViewById(R.id.loading_message);
        loadingSpinner = (ProgressBar) findViewById(R.id.loading_spinner);

        // attach onclick listener to buttons
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("dbg", "@CurrentOneActivity: start button clicked");

                // request to enable bluetooth if necessary
                if (!btAdapter.isEnabled()) {
                    Intent enableBt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBt, REQUEST_ENABLE_BT);
                } else { // bluetooth is already enabled
                    startService(new Intent(CurrentOneActivity.this, BluetoothService.class));
                }

            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("dbg", "@CurrentOneActivity: next button clicked");

                activeInspectionInfo.setPhase(CurrentOneActivity.this, -50);
                Intent next = new Intent(CurrentOneActivity.this, CurrentTwoActivity.class);
                next.putExtra(BluetoothService.LOCATION, BluetoothService.currentStatus.location);
                startActivity(next);
                finish();
            }
        });

        // initially display start button
        uiControl(0b00010, 0);

        // display content based off phase
        if (activeInspectionInfo.getPhase(this) != 0){
            switch(activeInspectionInfo.getPhase(this)){
                case -7: // connecting to drone
                    // display loading message & loading spinner
                    uiControl(0b11000, R.string.connecting_to_drone);
                    break;
                case -6: // drone connect success
                    // display next button
                    uiControl(0b00001, 0);
                    break;
                case -5: // drone connect failure
                    // display general message and start button
                    uiControl(0b00110, R.string.drone_connect_failure);
                    break;
            }
        }

    }

    @Override
    public void onBackPressed() {
        new ClientId().clear(this);
        startActivity(new Intent(this, ClientManagerActivity.class));
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                switch (resultCode) {
                    case RESULT_OK:
                        // start service
                        startService(new Intent(CurrentOneActivity.this, BluetoothService.class));
                        break;
                    case RESULT_CANCELED:
                        // user did not enable bluetooth, display message & start button
                        uiControl(0b0110, R.string.bluetooth_not_enabled);
                        break;
                }
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return (Utilities.inspectionMenu(item.getItemId(), this)) || super.onOptionsItemSelected(item);
    }

    private void uiControl(int num, int message) {
        // 5 UI elements:
        // next button:         00001 bit
        // start button:        00010 bit
        // general message:     00100 bit
        // loading message:     01000 bit
        // loading spinner:     10000 bit

        // next button control
        if ((num & 0b00001) != 0) {
            nextButton.setVisibility(View.VISIBLE);
        } else {
            nextButton.setVisibility(View.GONE);
        }

        // start button control
        if ((num & 0b00010) != 0) {
            startButton.setVisibility(View.VISIBLE);
        } else {
            startButton.setVisibility(View.GONE);
        }

        // general message control
        if ((num & 0b00100) != 0) {
            generalMessage.setText(message);
            generalMessage.setVisibility(View.VISIBLE);
        } else {
            generalMessage.setVisibility(View.GONE);
        }

        // loading message control
        if ((num & 0b01000) != 0) {
            loadingMessage.setText(message);
            loadingMessage.setVisibility(View.VISIBLE);
        } else {
            loadingMessage.setVisibility(View.GONE);
        }

        // loading spinner control
        if ((num & 0b10000) != 0) {
            loadingSpinner.setVisibility(View.VISIBLE);
        } else {
            loadingSpinner.setVisibility(View.GONE);
        }
    }
}
