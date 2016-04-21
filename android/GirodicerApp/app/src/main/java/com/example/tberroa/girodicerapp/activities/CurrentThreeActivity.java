package com.example.tberroa.girodicerapp.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.data.ClientId;
import com.example.tberroa.girodicerapp.data.CurrentInspectionInfo;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.fragments.DroneMapFragment;
import com.example.tberroa.girodicerapp.fragments.DroneStateFragment;
import com.example.tberroa.girodicerapp.bluetooth.Status;
import com.example.tberroa.girodicerapp.services.BluetoothService;

public class CurrentThreeActivity extends BaseActivity {

    public static final String DRONE_ACTIVITY_BROADCAST = "DRONE_ACTIVITY_BROADCAST";
    public static final String WHICH_FRAG = "WHICH_FRAG";
    public static final String STATUS_PACKAGE = "STATUS_PACKAGE";
    public static final String LOCATION_PACKAGE = "LOCATION_PACKAGE";
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_three);

        // no animation if starting due to a reload
        String action = getIntent().getAction();
        if (action != null && action.equals(Params.RELOAD)) {
            overridePendingTransition(0, 0);
        }

        // check if user should be in this activity
        if (new CurrentInspectionInfo().getPhase(this) == Params.CI_UPLOADING){
            // done inspection but still uploading, go to fourth activity
            startActivity(new Intent(this, CurrentFourActivity.class));
            finish();
            return;
        }
        if (BluetoothService.notRunning(this)) {
            // bluetooth needs to be setup, go back to first activity
            startActivity(new Intent(this, CurrentOneActivity.class));
            finish();
            return;
        }

        // set toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.current_inspection_title);
        }

        // initialize back button
        toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.back_button));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    new ClientId().clear(CurrentThreeActivity.this);
                    startActivity(new Intent(CurrentThreeActivity.this, ClientManagerActivity.class));
                    finish();
                }
            }
        });

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

        // initialize receiver, it's triggered when a status signal is received from the drone
        IntentFilter filter = new IntentFilter(Params.STATUS_UPDATE);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case Params.STATUS_UPDATE:
                        // get updated status
                        Status currentStatus = BluetoothService.currentStatus;

                        // create intent to broadcast to state fragment
                        Intent toStateFrag = new Intent(DRONE_ACTIVITY_BROADCAST);
                        toStateFrag.putExtra(WHICH_FRAG, DroneStateFragment.class.getName());
                        toStateFrag.putExtra(STATUS_PACKAGE, currentStatus);

                        // create intent to broadcast to map fragment
                        Intent toMapFrag = new Intent(DRONE_ACTIVITY_BROADCAST);
                        toMapFrag.putExtra(WHICH_FRAG, DroneMapFragment.class.getName());
                        toMapFrag.putExtra(LOCATION_PACKAGE, currentStatus.location);

                        // broadcast new status to both fragments
                        LocalBroadcastManager.getInstance(CurrentThreeActivity.this).sendBroadcast(toStateFrag);
                        LocalBroadcastManager.getInstance(CurrentThreeActivity.this).sendBroadcast(toMapFrag);
                }
            }
        };
        registerReceiver(broadcastReceiver, filter);

        // pass context to bluetooth data handler
        BluetoothService.BTDataHandler.passContext(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
        }
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
}
