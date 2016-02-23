package com.example.tberroa.girodicerapp.activities;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.example.tberroa.girodicerapp.R;

public class ClientManagerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Client Manager");
        toolbar.setVisibility(View.VISIBLE);
        setSupportActionBar(toolbar);

        // declare and initialize buttons
        Button startMissionButton = (Button)findViewById(R.id.start_mission_button);
        startMissionButton.setOnClickListener(startMissionButtonListener);
    }

    private final View.OnClickListener startMissionButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            //Utilities.AttemptMissionStart(HomeActivity.this);
        }
    };

}
