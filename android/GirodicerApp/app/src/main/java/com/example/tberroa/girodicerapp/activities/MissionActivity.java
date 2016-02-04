package com.example.tberroa.girodicerapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.tberroa.girodicerapp.data.Mission;
import com.example.tberroa.girodicerapp.adapters.MissionPagerAdapter;
import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.data.UserInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class MissionActivity extends BaseActivity {

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission);

        // grab username
        username = new UserInfo().getUsername(this);

        // grab mission JSON and mission number, these values were passed to the activity
        String jsonMission = getIntent().getExtras().getString("mission");
        int missionNumber = getIntent().getExtras().getInt("missionNumber");

        // unpack mission JSON into Mission object
        Type typeMission = new TypeToken<Mission>(){}.getType();
        Mission mission = new Gson().fromJson(jsonMission, typeMission);

        // set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Mission " + Integer.toString(missionNumber));
        setSupportActionBar(toolbar);

        // set tab layout
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Aerial"));
        tabLayout.addTab(tabLayout.newTab().setText("Thermal"));
        tabLayout.addTab(tabLayout.newTab().setText("IceDams"));
        tabLayout.addTab(tabLayout.newTab().setText("Salt"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // populate the activity
        final ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        MissionPagerAdapter adapter = new MissionPagerAdapter(getSupportFragmentManager(),
                tabLayout.getTabCount(), missionNumber, mission, username);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    // populate the navigation
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation, menu);

        MenuItem item = menu.findItem(R.id.title);
        item.setTitle("Logged in as: " + username);

        return true;
    }
}
