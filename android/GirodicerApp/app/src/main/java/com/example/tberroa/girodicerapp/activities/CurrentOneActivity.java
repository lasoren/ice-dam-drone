package com.example.tberroa.girodicerapp.activities;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.data.ClientId;
import com.example.tberroa.girodicerapp.data.CurrentInspectionInfo;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.services.BluetoothService;

public class CurrentOneActivity extends BaseActivity {

    // constants
    private final int REQUEST_ENABLE_BT = 1;

    // ui elements
    private Button startButton, tryAgainButton;
    private TextView generalMessage, loadingMessage;
    private ProgressBar loadingSpinner;

    // button listener
    private final View.OnClickListener connectButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            start();
        }
    };

    // bluetooth object
    private final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_one);

        // set navigation menu
        navigationView.inflateMenu(R.menu.nav_client_inspections);

        // initialize ui elements
        startButton = (Button) findViewById(R.id.start_button);
        tryAgainButton = (Button) findViewById(R.id.try_again_button);
        generalMessage = (TextView) findViewById(R.id.general_message);
        loadingMessage = (TextView) findViewById(R.id.loading_message);
        loadingSpinner = (ProgressBar) findViewById(R.id.loading_spinner);

        // set toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.current_inspection_title);
        }

        // initialize back button
        toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.back_button));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    new ClientId().clear(CurrentOneActivity.this);
                    startActivity(new Intent(CurrentOneActivity.this, ClientManagerActivity.class));
                    finish();
                }
            }
        });

        // turn off all ui elements to begin
        uiControl(0b00000, 0);

        // check some conditions
        String action = getIntent().getAction();
        if (action != null && action.equals(Params.RELOAD)) {
            // no animation if reloading
            overridePendingTransition(0, 0);
        } else {
            // user just arrived, reset bluetooth error code
            bluetoothInfo.setErrorCode(this, 0);
        }

        // check if user should be in this activity
        if (new CurrentInspectionInfo().getPhase(this) == Params.CI_UPLOADING){
            // done inspection but still uploading, go to fourth activity
            startActivity(new Intent(this, CurrentFourActivity.class));
            finish();
            return;
        }
        if (bluetoothInfo.getState(this) == Params.BTS_CONNECTED && BluetoothService.currentStatus != null) {
            // go to next activity if connection process has already been completed
            startActivity(new Intent(this, CurrentTwoActivity.class));
            finish();
            return;
        }

        // attach onclick listener to buttons
        startButton.setOnClickListener(connectButtonListener);
        tryAgainButton.setOnClickListener(connectButtonListener);

        // display content based off bluetooth state & error code
        switch (bluetoothInfo.getState(this)) {
            case Params.BTS_NOT_CONNECTED: // not currently connected to drone
                switch (bluetoothInfo.getErrorCode(this)) { // check why
                    case Params.BTE_NO_ERROR: // just got here, no error thrown yet
                        // display start button
                        uiControl(0b00001, 0);
                        break;
                    case Params.BTE_NOT_ENABLED: // tried but bluetooth was not enabled
                        // display message and try again button
                        uiControl(0b00110, R.string.bte_not_enabled);
                        break;
                    case Params.BTE_CONNECT_FAILED: // tried but connection attempt failed
                        // display message and try again button
                        uiControl(0b00110, R.string.bte_connect_failed);
                        break;
                    case Params.BTE_TIMEOUT: // was able to connect but never received status signal
                        // display message and try again button
                        uiControl(0b00110, R.string.bte_timeout_status);
                }
                break;
            case Params.BTS_CONNECTING: // attempting to connect to drone
                // display loading message & loading spinner
                uiControl(0b11000, R.string.bts_connecting);
                break;
            case Params.BTS_CONNECTED: // successfully connected to drone
                // make sure initial status has been received from drone before continuing
                if (BluetoothService.currentStatus != null) {
                    // go to next activity
                    startActivity(new Intent(this, CurrentTwoActivity.class));
                    finish();
                } else { // let user know that the system is waiting to receive initial status
                    // display loading message and spinner
                    uiControl(0b11000, R.string.waiting_for_status);
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                switch (resultCode) {
                    case RESULT_OK: // user enabled bluetooth
                        startBluetoothService();
                        break;
                    case RESULT_CANCELED: // user chose not to enable bluetooth
                        // display message and try again button
                        uiControl(0b00110, R.string.bte_not_enabled);
                        break;
                }
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

    private void start() {
        // request to enable bluetooth if necessary
        if (btAdapter == null) {
            // display message and try again button
            uiControl(0b00110, R.string.no_bt_adapter);
        } else {
            if (!btAdapter.isEnabled()) {
                Intent enableBt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBt, REQUEST_ENABLE_BT);
            } else { // bluetooth is already enabled
                startBluetoothService();
            }
        }
    }

    private void startBluetoothService() {
        // get client id
        int clientId = new ClientId().get(this);

        // start service
        Intent intent = new Intent(CurrentOneActivity.this, BluetoothService.class);
        intent.putExtra("client_id", clientId);
        startService(intent);

        // pass context to the data handler
        BluetoothService.BTDataHandler.passContext(CurrentOneActivity.this);
    }

    private void uiControl(int num, int message) {
        // 5 UI elements:
        // start button:        00001 bit
        // try again button:    00010 bit
        // general message:     00100 bit
        // loading message:     01000 bit
        // loading spinner:     10000 bit

        // start button control
        if ((num & 0b00001) != 0) {
            startButton.setVisibility(View.VISIBLE);
        } else {
            startButton.setVisibility(View.GONE);
        }

        // try again button control
        if ((num & 0b00010) != 0) {
            tryAgainButton.setVisibility(View.VISIBLE);
        } else {
            tryAgainButton.setVisibility(View.GONE);
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
