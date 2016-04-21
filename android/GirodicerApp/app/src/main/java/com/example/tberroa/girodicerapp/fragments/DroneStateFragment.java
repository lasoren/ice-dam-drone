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
import com.example.tberroa.girodicerapp.services.BluetoothService;

public class DroneStateFragment extends Fragment {

    private TextView landOrAir, location, velocity, battery;

    private final BroadcastReceiver receiveActivityEvents = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String frag = intent.getStringExtra(CurrentThreeActivity.WHICH_FRAG);

            if (frag.equals(DroneStateFragment.class.getName())) {
                Status currentStatus = intent.getParcelableExtra(CurrentThreeActivity.STATUS_PACKAGE);

                if (BluetoothService.motorsArmed){
                    landOrAir.setText(R.string.drone_is_in_the_air);
                } else{
                    landOrAir.setText(R.string.drone_is_landed);
                }

                if (currentStatus != null){
                    String latitude = String.format("%f", currentStatus.location.latitude);
                    String longitude = String.format("%f", currentStatus.location.longitude);
                    String locationFormatted = "(" + latitude + "," + longitude + ")";
                    location.setText(locationFormatted);
                    velocity.setText(String.format("%f", currentStatus.velocity));
                    battery.setText(String.format("%d", currentStatus.battery));
                }
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_drone_status, container, false);

        landOrAir = (TextView) root.findViewById(R.id.land_or_air);
        location = (TextView) root.findViewById(R.id.status_location);
        velocity = (TextView) root.findViewById(R.id.status_velocity);
        battery = (TextView) root.findViewById(R.id.status_battery);

        return root;
    }

    @Override
    public void onStart(){
        super.onStart();
        if (BluetoothService.currentStatus != null){
            if (BluetoothService.motorsArmed){
                landOrAir.setText(R.string.drone_is_in_the_air);
            } else{
                landOrAir.setText(R.string.drone_is_landed);
            }
            String latitude = String.format("%f", BluetoothService.currentStatus.location.latitude);
            String longitude = String.format("%f", BluetoothService.currentStatus.location.longitude);
            String locationFormatted = "(" + latitude + "," + longitude + ")";
            location.setText(locationFormatted);
            velocity.setText(String.format("%f", BluetoothService.currentStatus.velocity));
            battery.setText(String.format("%d", BluetoothService.currentStatus.battery));
        }

        IntentFilter filter = new IntentFilter(CurrentThreeActivity.DRONE_ACTIVITY_BROADCAST);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiveActivityEvents, filter);
    }

    @Override
    public void onStop(){
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiveActivityEvents);
        super.onStop();
    }
}
