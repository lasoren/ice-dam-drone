package com.example.tberroa.girodicerapp.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.bluetooth.Status;
import com.example.tberroa.girodicerapp.activities.CurrentThreeActivity;

public class DroneStateFragment extends Fragment {

    private TextView location, velocity, state, armable;

    private final BroadcastReceiver receiveActivityEvents = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String frag = intent.getStringExtra(CurrentThreeActivity.WHICH_FRAG);

            if (frag.equals(DroneStateFragment.class.getName())) {
                Status currentStatus = intent.getParcelableExtra(CurrentThreeActivity.STATUS_PACKAGE);

                if (currentStatus != null){
                    String latitude = String.format("%f", currentStatus.location.latitude);
                    String longitude = String.format("%f", currentStatus.location.longitude);
                    String locationFormatted = "(" + latitude + "," + longitude + ")";
                    location.setText(locationFormatted);
                    velocity.setText(String.format("%f", currentStatus.velocity));
                    state.setText(String.format("%d", currentStatus.state));
                    armable.setText(String.format("%d", currentStatus.armable));
                }
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_drone_status, container, false);

        location = (TextView) root.findViewById(R.id.status_location);
        velocity = (TextView) root.findViewById(R.id.status_velocity);
        state = (TextView) root.findViewById(R.id.status_state);
        armable = (TextView) root.findViewById(R.id.status_armable);

        return root;
    }

    @Override
    public void onResume() {
        IntentFilter filter = new IntentFilter(CurrentThreeActivity.DRONE_ACTIVITY_BROADCAST);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiveActivityEvents, filter);
        super.onResume();
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiveActivityEvents);
        super.onPause();
    }
}
