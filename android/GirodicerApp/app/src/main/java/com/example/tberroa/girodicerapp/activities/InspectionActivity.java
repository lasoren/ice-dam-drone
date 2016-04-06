package com.example.tberroa.girodicerapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.example.tberroa.girodicerapp.adapters.InspectionPagerAdapter;
import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.data.InspectionId;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.database.LocalDB;
import com.example.tberroa.girodicerapp.models.Inspection;
import com.example.tberroa.girodicerapp.models.InspectionImage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class InspectionActivity extends BaseActivity {

    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection);
        LocalDB localDB = new LocalDB();

        // no animation if starting due to a reload
        String action = getIntent().getAction();
        if (action != null && action.equals(Params.RELOAD)) {
            overridePendingTransition(0, 0);
        }

        // get inspection
        Inspection inspection = localDB.getInspection(new InspectionId().get(this));

        // get images relating to this inspection
        List<InspectionImage> aerialImages = localDB.getInspectionImages(inspection.id, Params.I_TYPE_AERIAL);
        List<InspectionImage> thermalImages = localDB.getInspectionImages(inspection.id, Params.I_TYPE_THERMAL);
        List<InspectionImage> roofEdgeImages = localDB.getInspectionImages(inspection.id, Params.I_TYPE_ROOF_EDGE);

        // serialize the inspection images
        Type inspectionImagesList = new TypeToken<List<InspectionImage>>() {
        }.getType();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String aerialImagesJson = gson.toJson(aerialImages, inspectionImagesList);
        String thermalImagesJson = gson.toJson(thermalImages, inspectionImagesList);
        String roofEdgeImagesJson = gson.toJson(roofEdgeImages, inspectionImagesList);

        // store the images in a bundle
        Bundle inspectionImages = new Bundle();
        inspectionImages.putString(Integer.toString(Params.I_TYPE_AERIAL), aerialImagesJson);
        inspectionImages.putString(Integer.toString(Params.I_TYPE_THERMAL), thermalImagesJson);
        inspectionImages.putString(Integer.toString(Params.I_TYPE_ROOF_EDGE), roofEdgeImagesJson);

        // set toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.inspection_title) + " " + Integer.toString(inspection.id));
        }

        // set navigation menu
        navigationView.inflateMenu(R.menu.nav_client_inspections);

        // set tab layout
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_bar);
        tabLayout.addTab(tabLayout.newTab().setText(Params.AERIAL_TAB));
        tabLayout.addTab(tabLayout.newTab().setText(Params.THERMAL_TAB));
        tabLayout.addTab(tabLayout.newTab().setText(Params.ROOF_EDGE_TAB));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setVisibility(View.VISIBLE); // set to GONE in XML layout for clarity

        // populate the activity
        viewPager = (ViewPager) findViewById(R.id.inspection_view_pager);
        InspectionPagerAdapter inspectionPagerAdapter;
        FragmentManager fragmentManager = getSupportFragmentManager();
        int numberOfTabs = tabLayout.getTabCount();
        inspectionPagerAdapter = new InspectionPagerAdapter(fragmentManager, numberOfTabs, inspectionImages);
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
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            startActivity(new Intent(this, PastInspectionsActivity.class));
            finish();
        }
    }
}
