package com.example.tberroa.girodicerapp;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.content.Intent;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startMissionButton = (Button)findViewById(R.id.start_mission_button);
        startMissionButton.setOnClickListener(startMissionButtonListener);

        Button currentMissionButton = (Button)findViewById(R.id.current_mission_button);
        currentMissionButton.setOnClickListener(currentMissionButtonListener);

        Button previousMissionsButton = (Button)findViewById(R.id.previous_missions_button);
        previousMissionsButton.setOnClickListener(previousMissionsButtonListener);

        enableBluetooth();
    }

    private OnClickListener startMissionButtonListener = new OnClickListener() {
        public void onClick(View v) {
            ServiceStatus serviceStatus = new ServiceStatus(v.getContext().getApplicationContext());
            if (!serviceStatus.isServiceRunning()) {
                startService(new Intent(MainActivity.this, ActiveMissionService.class));
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
