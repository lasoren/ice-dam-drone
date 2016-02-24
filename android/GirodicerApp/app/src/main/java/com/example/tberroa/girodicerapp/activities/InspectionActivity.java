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

import com.example.tberroa.girodicerapp.adapters.InspectionPagerAdapter;
import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.data.OperatorId;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.database.LocalDB;
import com.example.tberroa.girodicerapp.models.Inspection;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class InspectionActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection);

        // grab operatorName
        int operatorId = new OperatorId().get(this);
        operatorName = new LocalDB().getOperator(operatorId).user.first_name;

        // grab client JSON and client number, these values were passed to the activity
        String inspectionJson = getIntent().getExtras().getString("inspection_json");
        int inspectionNumber = getIntent().getExtras().getInt("inspection_number");

        // unpack inspection JSON into Inspection object
        //Type typeInspection = new TypeToken<Inspection>(){}.getType();
        //Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        //Inspection inspection = gson.fromJson(inspectionJson, typeInspection);

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
        final ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
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
