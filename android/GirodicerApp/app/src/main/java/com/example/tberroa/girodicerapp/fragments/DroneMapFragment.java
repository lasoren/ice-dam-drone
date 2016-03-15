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
import android.widget.FrameLayout;

import com.example.tberroa.girodicerapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import com.example.tberroa.girodicerapp.activities.DroneActivity;

public class DroneMapFragment extends Fragment implements OnMapReadyCallback {

    private FrameLayout root;

    private GoogleMap map;


    private BroadcastReceiver receiveActivityEvents = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String frag = intent.getStringExtra(DroneActivity.WHICH_FRAG);

            if(frag.equals(DroneStateFragment.class.getName())){
                LatLng currentLocation = intent.getParcelableExtra(DroneActivity.LOCATION_PACKAGE);
                if(currentLocation != null)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16));
            }
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = (FrameLayout) inflater.inflate(R.layout.fragment_drone_map, container, false);

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MapView mapView = new MapView(getActivity());
        mapView.onCreate(savedInstanceState);
        root.addView(mapView);

        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(45.59514, -65.47880), 16));
    }

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiveActivityEvents, new IntentFilter(DroneActivity.DRONE_ACTIVITY_BROADCAST));
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
