package com.example.tberroa.girodicerapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

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
        UserInfo userInfo = new UserInfo();
        username = userInfo.getUsername(this.getApplicationContext());

        // set tab layout
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Aerial"));
        tabLayout.addTab(tabLayout.newTab().setText("Thermal"));
        tabLayout.addTab(tabLayout.newTab().setText("IceDams"));
        tabLayout.addTab(tabLayout.newTab().setText("Salt"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // grab mission json and mission number from intent
        Intent intent = getIntent();
        String jsonMission = intent.getExtras().getString("mission");
        int missionNumber = intent.getExtras().getInt("missionNumber");
        // unpack mission json into Mission object
        Gson gson = new Gson();
        Type singleMission = new TypeToken<Mission>(){}.getType();
        Mission mission = gson.fromJson(jsonMission, singleMission);

        // set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Mission " + Integer.toString(missionNumber));
        setSupportActionBar(toolbar);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        final PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount(), missionNumber, mission, username);
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
