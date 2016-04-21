package com.example.tberroa.girodicerapp.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.data.ClientId;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.services.BluetoothService;
import com.example.tberroa.girodicerapp.services.UploadIntentService;

// this activity displays the upload phase
public class CurrentFourActivity extends BaseActivity {

    public static int progressStatus;
    public static int previousUploadType;
    private TextView descriptiveText;
    private ProgressBar progressBar;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_four);

        // no animation if starting due to a reload
        String action = getIntent().getAction();
        if (action != null && action.equals(Params.RELOAD)) {
            overridePendingTransition(0, 0);
        }

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
                    new ClientId().clear(CurrentFourActivity.this);
                    startActivity(new Intent(CurrentFourActivity.this, ClientManagerActivity.class));
                    finish();
                }
            }
        });

        // set navigation menu
        navigationView.inflateMenu(R.menu.nav_client_inspections);

        // initialize ui elements
        descriptiveText = (TextView) findViewById(R.id.descriptive_text);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setMax(BluetoothService.BTDataHandler.imgIndexTherm);

        // initialize recycler view
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.upload_recycler_view);
        recyclerView.setVisibility(View.GONE); // test code. will use later

        // initialize receiver, it's triggered when an image is uploaded
        IntentFilter filter = new IntentFilter();
        filter.addAction(Params.IMAGE_UPLOAD_COMPLETE);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case Params.IMAGE_UPLOAD_COMPLETE:
                        update();
                        progressBar.setProgress(++progressStatus);
                        break;
                }
            }
        };
        registerReceiver(broadcastReceiver, filter);
    }

    @Override
    public void onStart(){
        super.onStart();
        if (progressStatus != progressBar.getMax()) {
            // not done yet, run an update
            update();
        }
    }

    @Override
    public void onDestroy(){
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    public void update(){
        // reset progress if all images of a certain type have completed uploading
        if (UploadIntentService.uploadingType != previousUploadType){
            progressStatus = 0;
        }
        previousUploadType = UploadIntentService.uploadingType;

        switch (UploadIntentService.uploadingType){
            case Params.I_TYPE_AERIAL:
                descriptiveText.setText(R.string.uploading_aerial_images);
                progressBar.setMax(UploadIntentService.numOfAerials);
                break;
            case Params.I_TYPE_THERMAL:
                descriptiveText.setText(R.string.uploading_thermal_images);
                progressBar.setMax(UploadIntentService.numOfThermals);
                break;
            case Params.I_TYPE_ROOF_EDGE:
                descriptiveText.setText(R.string.uploading_rgb_images);
                progressBar.setMax(UploadIntentService.numOfRoofEdges);
                break;
        }
    }
}
