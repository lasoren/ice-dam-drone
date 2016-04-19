package org.girodicer.plottwist.Fragments;

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

import org.girodicer.plottwist.DroneActivity;
import org.girodicer.plottwist.Models.Status;
import org.girodicer.plottwist.R;

/**
 * Created by Carlos on 2/23/2016.
 */
public class DroneStateFragment extends Fragment {

    private TextView location, velocity, battery;
    private Status currentStatus;

    private BroadcastReceiver receiveActivityEvents = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String frag = intent.getStringExtra(DroneActivity.WHICH_FRAG);

            if(frag.equals(DroneStateFragment.class.getName())){
                currentStatus = intent.getParcelableExtra(DroneActivity.STATUS_PACKAGE);

                location.setText("(" + currentStatus.location.latitude + "," +
                                currentStatus.location.longitude + ")");

                velocity.setText(currentStatus.velocity.toString());

                battery.setText(Integer.toString(currentStatus.battery));
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_drone_status, container, false);

        location = (TextView) root.findViewById(R.id.status_location);
        velocity = (TextView) root.findViewById(R.id.status_velocity);
        battery = (TextView) root.findViewById(R.id.status_battery);

        return root;
    }

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiveActivityEvents,
                new IntentFilter(DroneActivity.DRONE_ACTIVITY_BROADCAST));
        super.onResume();
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiveActivityEvents);
        super.onPause();
    }
}
