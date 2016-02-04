package com.example.tberroa.girodicerapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.content.Intent;

import com.example.tberroa.girodicerapp.data.PreviousMissionsInfo;
import com.example.tberroa.girodicerapp.data.UserInfo;
import com.example.tberroa.girodicerapp.services.ActiveMissionService;
import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.data.ServiceStatus;

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

        // make sure bluetooth is enabled
        enableBluetooth();
    }

    private OnClickListener startMissionButtonListener = new OnClickListener() {
        public void onClick(View v) {
            if (!new ServiceStatus().isServiceRunning(MainActivity.this)) {
                // grab username and calculate the mission number for the new mission
                String username = new UserInfo().getUsername(MainActivity.this);
                int missionNumber =
                        (new PreviousMissionsInfo().getNumOfMissions(MainActivity.this))+1;
                // start the active mission service, pass it the username and mission number
                startService(new Intent(MainActivity.this, ActiveMissionService.class)
                        .putExtra("username", username)
                        .putExtra("mission_number", missionNumber));
                // go to active mission activity
                startActivity(new Intent(MainActivity.this, ActiveMissionActivity.class));
                finish();
            }
            else{
                missionInProgressDialog(v.getContext()).show();
            }
        }
    };

    private OnClickListener currentMissionButtonListener = new OnClickListener() {
        public void onClick(View v) {
            startActivity(new Intent(MainActivity.this, ActiveMissionActivity.class));
            finish();
        }
    };

    private OnClickListener previousMissionsButtonListener = new OnClickListener() {
        public void onClick(View v) {
            startActivity(new Intent(MainActivity.this, PreviousMissionsActivity.class));
            finish();
        }
    };

    @Override
    protected void onDestroy(){
        super.onDestroy();
        stopService(new Intent(MainActivity.this, ActiveMissionService.class));
    }
}
