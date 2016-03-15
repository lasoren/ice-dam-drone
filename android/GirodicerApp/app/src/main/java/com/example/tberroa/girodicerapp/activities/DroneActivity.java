package com.example.tberroa.girodicerapp.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.widget.Toast;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.bluetooth.BluetoothException;
import com.example.tberroa.girodicerapp.bluetooth.ConnectionThread;
import com.example.tberroa.girodicerapp.bluetooth.GProtocol;
import com.example.tberroa.girodicerapp.fragments.DroneMapFragment;
import com.example.tberroa.girodicerapp.fragments.DroneStateFragment;
import com.example.tberroa.girodicerapp.models.Status;
import com.example.tberroa.girodicerapp.services.BluetoothService;

public class DroneActivity extends AppCompatActivity {

    public static final String DRONE_ACTIVITY_BROADCAST = "DRONE_ACTIVITY_BROADCAST";
    public static final String WHICH_FRAG = "WHICH_FRAG";
    public static final String STATUS_PACKAGE = "STATUS_PACKAGE";
    public static final String LOCATION_PACKAGE = "LOCATION_PACKAGE";

    private final Messenger btMessageHandler = new Messenger(new BTMessageHandler());

    private static Messenger bluetoothMessenger; // only for the handler in this class
    private static boolean bluetoothServiceBound = false;

    private ServiceConnection bluetoothConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bluetoothMessenger = new Messenger(service);
            bluetoothServiceBound = true;

            Message msg = Message.obtain(null, BluetoothService.MESSAGE_NEW_CLIENT);
            msg.replyTo = btMessageHandler;
            try {
                bluetoothMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Message msg = Message.obtain(null, BluetoothService.MESSAGE_DETACH_CLIENT);
            try {
                bluetoothMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            bluetoothMessenger = null;
            bluetoothServiceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_drone);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

    }

    @Override
    protected void onResume() {
        super.onResume();
        bindService(new Intent(this, BluetoothService.class), bluetoothConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(bluetoothConnection);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private static final int NUM_PAGES = 2;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    return new DroneStateFragment();
                case 1:
                    return new DroneMapFragment();
            }

            return null;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Status";
                case 1:
                    return "Map";
            }
            return null;
        }
    }

    private class BTMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothService.MESSAGE_READ:
                    Bundle bundle = msg.getData();
                    byte[] data = bundle.getByteArray(ConnectionThread.BT_DATA);
                    try {
                        GProtocol received = GProtocol.Unpack(data);
                        switch (received.getCommand()) {
                            case GProtocol.COMMAND_STATUS:
                                Status currentStatus = (Status) received.read();
                                Intent broadcastToFrag = new Intent(DRONE_ACTIVITY_BROADCAST);
                                broadcastToFrag.putExtra(WHICH_FRAG, DroneStateFragment.class.getName());
                                broadcastToFrag.putExtra(STATUS_PACKAGE, currentStatus);

                                LocalBroadcastManager.getInstance(DroneActivity.this).sendBroadcast(broadcastToFrag);
                                break;
                            case GProtocol.COMMAND_SEND_POINTS:
                                break;
                        }
                    } catch (BluetoothException e) {
                        e.printStackTrace();
                    }
                    break;
                case BluetoothService.MESSAGE_BT_CONNECTION_LOST:
                    Toast.makeText(DroneActivity.this, "Connection lost", Toast.LENGTH_SHORT).show();
                    Toast.makeText(DroneActivity.this, "Reconnecting....", Toast.LENGTH_LONG).show();
                    break;
                case BluetoothService.MESSAGE_BT_FAILED_RECONNECT:
                    Toast.makeText(DroneActivity.this, "Still reconnecting", Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothService.MESSAGE_BT_SUCCESS_RECONNECT:
                    Toast.makeText(DroneActivity.this, "Successfully reconnected", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
