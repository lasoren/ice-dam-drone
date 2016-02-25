package com.example.tberroa.girodicerapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.tberroa.girodicerapp.adapters.InspectionPagerAdapter;
import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.data.Params;

public class InspectionActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection);

        // grab inspection json and inspection number, these values are passed to the activity via intent
        String inspectionJson = getIntent().getExtras().getString("inspection_json");
        int inspectionNumber = getIntent().getExtras().getInt("inspection_number");

        // set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Inspection " + Integer.toString(inspectionNumber));
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
        final ViewPager viewPager = (ViewPager) findViewById(R.id.inspection_view_pager);
        InspectionPagerAdapter inspectionPagerAdapter;
        FragmentManager fragmentManager = getSupportFragmentManager();
        int numberOfTabs = tabLayout.getTabCount();
        inspectionPagerAdapter = new InspectionPagerAdapter(fragmentManager, numberOfTabs, inspectionJson);
        viewPager.setAdapter(inspectionPagerAdapter);
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

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, PastInspectionsActivity.class));
        finish();
    }

}
