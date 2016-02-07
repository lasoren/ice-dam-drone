package com.example.tberroa.girodicerapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.content.Intent;

import com.example.tberroa.girodicerapp.dialogs.MissionInProgressDialog;
import com.example.tberroa.girodicerapp.services.DroneService;
import com.example.tberroa.girodicerapp.R;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // declare and initialize buttons
        Button startMissionButton = (Button)findViewById(R.id.start_mission_button);
        startMissionButton.setOnClickListener(startMissionButtonListener);
        Button currentMissionButton = (Button)findViewById(R.id.current_mission_button);
        currentMissionButton.setOnClickListener(currentMissionButtonListener);
        Button previousMissionsButton = (Button)findViewById(R.id.previous_missions_button);
        previousMissionsButton.setOnClickListener(previousMissionsButtonListener);
    }

    private final OnClickListener startMissionButtonListener = new OnClickListener() {
        public void onClick(View v) {
            if (activeMissionInfo.missionNotInProgress(MainActivity.this)) {
                startService(new Intent(MainActivity.this, DroneService.class));
                startActivity(new Intent(MainActivity.this, ActiveMissionActivity.class));
                finish();
            }
            else{
                new MissionInProgressDialog(v.getContext()).getDialog().show();
            }
        }
    };

    private final OnClickListener currentMissionButtonListener = new OnClickListener() {
        public void onClick(View v) {
            startActivity(new Intent(MainActivity.this, ActiveMissionActivity.class));
            finish();
        }
    };

    private final OnClickListener previousMissionsButtonListener = new OnClickListener() {
        public void onClick(View v) {
            startActivity(new Intent(MainActivity.this, PreviousMissionsActivity.class));
            finish();
        }
    };

    @Override
    protected void onDestroy(){
        super.onDestroy();
        stopService(new Intent(MainActivity.this, DroneService.class));
    }
}
