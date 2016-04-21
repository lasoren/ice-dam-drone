package com.example.tberroa.girodicerapp.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.bluetooth.GProtocol;
import com.example.tberroa.girodicerapp.bluetooth.Points;
import com.example.tberroa.girodicerapp.data.CurrentInspectionInfo;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.dialogs.ConfirmIceDamDialog;
import com.example.tberroa.girodicerapp.dialogs.MessageDialog;
import com.example.tberroa.girodicerapp.services.BluetoothService;
import com.example.tberroa.girodicerapp.activities.CurrentThreeActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class DroneMapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
        View.OnClickListener {

    private MapView mapView;
    private GoogleMap map;
    private Marker droneMarker;
    private Button finishSalting;
    private Button next;
    public static boolean plottedIceDamPoints = false;
    public static boolean sentIcedamPoints = false;
    public static List<LatLng> confirmedIceDamPoints;

    private final BroadcastReceiver receiveActivityEvents = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String frag = intent.getStringExtra(CurrentThreeActivity.WHICH_FRAG);

            if (frag.equals(DroneMapFragment.class.getName())) {
                LatLng currentLocation = intent.getParcelableExtra(CurrentThreeActivity.LOCATION_PACKAGE);

                // update camera
                if (currentLocation != null) {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 24));
                    droneMarker.setPosition(currentLocation);
                }

                // plot icedam points if they are ready and we need to
                if (BluetoothService.iceDamPointsReady && !plottedIceDamPoints) {
                    Log.d(Params.TAG_DBG + Params.TAG_MAP, "@DroneMapFragment: plotting icedam points");

                    // plot points in blue
                    for (LatLng point : BluetoothService.iceDamPoints) {
                        map.addMarker(new MarkerOptions().position(point)
                                .icon(BitmapDescriptorFactory
                                        .defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    }

                    plottedIceDamPoints = true;
                }

                // check if confirmation has been received and the service is in process
                if (next != null && BluetoothService.servicingIcedam) {
                    next.setEnabled(false);
                    next.setText(R.string.servicing_in_process);
                }

                // check if drone has landed and is ready to service another icedam
                if (next != null && sentIcedamPoints && BluetoothService.readyToServiceIcedam) {
                    next.setEnabled(true);
                    next.setText(R.string.service_icedam);
                }
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_drone_map, container, false);

        // initialize map
        mapView = (MapView) v.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();// needed to get the map to display immediately
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mapView.getMapAsync(this);

        // initialize finish salting button
        finishSalting = (Button) v.findViewById(R.id.finish_salting);
        finishSalting.setText(R.string.skip_salting);
        finishSalting.setOnClickListener(this);
        finishSalting.setVisibility(View.GONE);

        // initialize next button
        next = (Button) v.findViewById(R.id.maps_next);
        next.setOnClickListener(this);
        next.setVisibility(View.GONE);

        // show buttons only if in salting phase
        if (new CurrentInspectionInfo().getPhase(getContext()) == Params.CI_SALTING) {
            next.setVisibility(View.VISIBLE);
            finishSalting.setVisibility(View.VISIBLE);
        }

        // initialize next button text based off whether icedam points have already been sent or not
        if (sentIcedamPoints) {
            next.setText(R.string.service_icedam);
        } else {
            next.setText(R.string.send_icedam_points);
        }

        return v;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMarkerClickListener(this);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(BluetoothService.home, 24));
        droneMarker = map.addMarker(new MarkerOptions().position(BluetoothService.currentStatus.location).draggable(true));
        droneMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.drone_icon));
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d(Params.TAG_DBG + Params.TAG_MAP, "@DroneMapFragment: marker clicked");
        // get the image
        int imageIndex = BluetoothService.pointToImageIndex.get(marker.getPosition());
        Bitmap image = BluetoothService.imageIndexToImage.get(imageIndex);
        new ConfirmIceDamDialog(getContext(), marker, image).show();
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.maps_next:
                // icedam points have been sent, send a service icedam command
                if (sentIcedamPoints) { // make sure icedam points have been sent to drone
                    if (BluetoothService.receivedAllRGBImages) { // make sure all rgb images have come in
                        if (BluetoothService.readyToServiceIcedam) { // make sure drone is ready to service icedam
                            Log.d(Params.TAG_MAP + Params.TAG_DS, "@DroneMapFragment: sending service icedam command");
                            byte[] command = GProtocol.Pack(GProtocol.COMMAND_SERVICE_ICEDAM, 1, new byte[1], false);
                            BluetoothService.btConnectionThread.write(command);
                            next.setText(R.string.waiting_for_service_confirmation);
                            next.setEnabled(false);
                        } else { // drone is in the air, need to wait
                            new MessageDialog(getContext(), getString(R.string.drone_busy_in_flight)).show();
                        }
                    } else { // rgb images haven't finished transferring, need to wait
                        new MessageDialog(getContext(), getString(R.string.need_rgb_images)).show();
                    }
                } else { // icedam points have not been sent, check if drone is ready for them
                    if (confirmedIceDamPoints != null && !confirmedIceDamPoints.isEmpty()) {
                        Log.d(Params.TAG_MAP + Params.TAG_DS, "@DroneMapFragment: sending icedam points");
                        ArrayList<LatLng> pointsList = new ArrayList<>(confirmedIceDamPoints);
                        byte[] points = Points.Pack(pointsList);
                        byte[] pointsPacked = GProtocol.Pack(GProtocol.COMMAND_SEND_ICEDAM_POINTS, points.length, points, false);
                        BluetoothService.btConnectionThread.write(pointsPacked);
                        sentIcedamPoints = true;
                        next.setText(R.string.service_icedam);
                        finishSalting.setText(R.string.finish_salting);
                    } else { // let user know that there are no points to send
                        new MessageDialog(getContext(), getString(R.string.no_icedam_points_to_send)).show();
                    }
                }
                break;

            case R.id.finish_salting:
                Log.d(Params.TAG_DBG + Params.TAG_MAP, "@DroneMapFragment: finish salting button clicked");
                if (!BluetoothService.motorsArmed) {
                    next.setEnabled(false);
                    next.setVisibility(View.GONE);
                    finishSalting.setEnabled(false);
                    finishSalting.setVisibility(View.GONE);

                    // broadcast that transfer phase has started
                    new CurrentInspectionInfo().setPhase(getContext(), Params.CI_TRANSFERRING);
                    getContext().sendBroadcast(new Intent().setAction(Params.TRANSFER_STARTED));

                    if (BluetoothService.receivedAllRGBImages) {
                        // request thermal images
                        Log.d(Params.TAG_DBG + Params.TAG_DS, "@BS/DH/FINISHED_RGB: sending request for thermal images");
                        byte[] requestTherm = GProtocol.Pack(GProtocol.COMMAND_SEND_IMAGES_THERM, 1, new byte[1], false);
                        BluetoothService.btConnectionThread.write(requestTherm);
                    }
                } else {
                    new MessageDialog(getContext(), getString(R.string.drone_busy_in_flight)).show();
                }
                break;
        }
    }

    // register receiver and update view
    @Override
    public void onStart(){
        super.onStart();

        // update camera
        if (map != null && BluetoothService.currentStatus != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(BluetoothService.currentStatus.location, 24));
            droneMarker.setPosition(BluetoothService.currentStatus.location);
        }

        // plot icedam points if they are ready and we need to
        if (map != null && BluetoothService.iceDamPointsReady && !plottedIceDamPoints) {
            Log.d(Params.TAG_DBG + Params.TAG_MAP, "@DroneMapFragment: plotting icedam points");

            // plot points in blue
            for (LatLng point : BluetoothService.iceDamPoints) {
                map.addMarker(new MarkerOptions().position(point)
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            }

            plottedIceDamPoints = true;
        }

        // check if confirmation has been received and the service is in process
        if (next != null && BluetoothService.servicingIcedam) {
            next.setEnabled(false);
            next.setText(R.string.servicing_in_process);
        }

        // check if drone has landed and is ready to service another icedam
        if (next != null && sentIcedamPoints && BluetoothService.readyToServiceIcedam) {
            next.setEnabled(true);
            next.setText(R.string.service_icedam);
        }

        IntentFilter filter = new IntentFilter(CurrentThreeActivity.DRONE_ACTIVITY_BROADCAST);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiveActivityEvents, filter);
    }

    // handle map view lifecycle
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    // handle map view lifecycle
    @Override
    public void onPause(){
        mapView.onPause();
        super.onPause();
    }

    // unregister receiver
    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiveActivityEvents);
        super.onStop();
    }

    // handle map view life cycle
    @Override
    public void onDestroy() {
        mapView.onDestroy();
        plottedIceDamPoints = false;
        super.onDestroy();
    }

    // handle map view life cycle
    @Override
    public void onLowMemory() {
        mapView.onLowMemory();
        super.onLowMemory();
    }
}
