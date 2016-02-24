package org.girodicer.plottwist.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import org.girodicer.plottwist.DroneActivity;
import org.girodicer.plottwist.R;

/**
 * Created by Carlos on 2/23/2016.
 */
public class DroneMapFragment extends Fragment implements OnMapReadyCallback {

    private FrameLayout root;
    private MapView mapView;

    private GoogleMap map;
    private LatLng currentLocation;


    private BroadcastReceiver receiveActivityEvents = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String frag = intent.getStringExtra(DroneActivity.WHICH_FRAG);

            if(frag.equals(DroneStateFragment.class.getName())){
                currentLocation = intent.getParcelableExtra(DroneActivity.LOCATION_PACKAGE);
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

        mapView = new MapView(getActivity());
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
