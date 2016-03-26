package com.example.tberroa.girodicerapp.activities;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.bluetooth.BluetoothException;
import com.example.tberroa.girodicerapp.bluetooth.ConnectionThread;
import com.example.tberroa.girodicerapp.bluetooth.GProtocol;
import com.example.tberroa.girodicerapp.data.ClientId;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.fragments.DroneMapFragment;
import com.example.tberroa.girodicerapp.fragments.DroneStateFragment;
import com.example.tberroa.girodicerapp.models.Status;
import com.example.tberroa.girodicerapp.services.BluetoothService;

public class CurrentThreeActivity extends BaseActivity {

    public static final String DRONE_ACTIVITY_BROADCAST = "DRONE_ACTIVITY_BROADCAST";
    public static final String WHICH_FRAG = "WHICH_FRAG";
    public static final String STATUS_PACKAGE = "STATUS_PACKAGE";
    public static final String LOCATION_PACKAGE = "LOCATION_PACKAGE";

    private final Messenger btMessageHandler = new Messenger(new BTMessageHandler());

    private static Messenger bluetoothMessenger; // only for the handler in this class
    private static boolean bluetoothServiceBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_three);

        // no animation if starting due to a reload
        String action = getIntent().getAction();
        if (action != null && action.equals(Params.RELOAD)) {
            overridePendingTransition(0, 0);
        }

        // set toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.current_inspection_title);
        }

        // set navigation menu
        navigationView.inflateMenu(R.menu.nav_client_inspections);

        // Create the adapter that will return a fragment for each of the three primary sections of the activity
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // initialize the tab layout
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_bar);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setVisibility(View.VISIBLE);

    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            new ClientId().clear(this);
            startActivity(new Intent(this, ClientManagerActivity.class));
            finish();
        }
    }

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
                case BluetoothService.READ:
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

                                LocalBroadcastManager.getInstance(CurrentThreeActivity.this).sendBroadcast(broadcastToFrag);
                                break;
                            case GProtocol.COMMAND_SEND_POINTS:
                                break;
                        }
                    } catch (BluetoothException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }
}
