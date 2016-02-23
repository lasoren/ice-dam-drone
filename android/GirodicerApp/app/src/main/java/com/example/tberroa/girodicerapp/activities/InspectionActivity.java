package com.example.tberroa.girodicerapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.tberroa.girodicerapp.data.Mission;
import com.example.tberroa.girodicerapp.adapters.InspectionPagerAdapter;
import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.helpers.DBManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class InspectionActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection);

        // grab operatorName
        int operatorId = operatorInfo.getId(this);
        operatorName = DBManager.getOperatorName(operatorId);

        // grab mission JSON and mission number, these values were passed to the activity
        String jsonMission = getIntent().getExtras().getString("mission");
        int missionNumber = getIntent().getExtras().getInt("mission_number");

        // unpack mission JSON into Mission object
        Type typeMission = new TypeToken<Mission>(){}.getType();
        Mission mission = new Gson().fromJson(jsonMission, typeMission);

        // set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Mission " + Integer.toString(missionNumber));
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.back_button);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(InspectionActivity.this, PastInspectionsActivity.class));
                finish();
            }
        });
        toolbar.setVisibility(View.VISIBLE);

        // set tab layout
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_bar);
        tabLayout.addTab(tabLayout.newTab().setText(Params.AERIAL_TAB));
        tabLayout.addTab(tabLayout.newTab().setText(Params.THERMAL_TAB));
        tabLayout.addTab(tabLayout.newTab().setText(Params.ICEDAM_TAB));
        tabLayout.addTab(tabLayout.newTab().setText(Params.SALT_TAB));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setVisibility(View.VISIBLE);

        // populate the activity
        final ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        InspectionPagerAdapter adapter;
        FragmentManager fM = getSupportFragmentManager();
        int numOfT = tabLayout.getTabCount();
        adapter = new InspectionPagerAdapter(fM, numOfT, missionNumber, mission, operatorName);
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
        item.setTitle("Logged in as: " + operatorName);
        return true;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, PastInspectionsActivity.class));
        finish();
    }

}
