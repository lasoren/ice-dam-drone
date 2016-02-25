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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection);
        LocalDB localDB = new LocalDB();

        // get inspection
        Inspection inspection = localDB.getInspection(new InspectionId().get(this));

        // get images relating to this inspection
        List<InspectionImage> aerialImages = localDB.getInspectionImages(inspection, "aerial");
        List<InspectionImage> thermalImages = localDB.getInspectionImages(inspection, "thermal");
        List<InspectionImage> iceDamImages = localDB.getInspectionImages(inspection, "icedam");
        List<InspectionImage> saltImages = localDB.getInspectionImages(inspection, "salt");

        // serialize the inspection images
        Type inspectionImagesList = new TypeToken<List<InspectionImage>>(){}.getType();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String aerialImagesJson = gson.toJson(aerialImages, inspectionImagesList);
        String thermalImagesJson = gson.toJson(thermalImages, inspectionImagesList);
        String iceDamImagesJson = gson.toJson(iceDamImages, inspectionImagesList);
        String saltImagesJson = gson.toJson(saltImages, inspectionImagesList);

        // store the images in a bundle
        Bundle inspectionImages = new Bundle();
        inspectionImages.putString("aerial_images_json", aerialImagesJson);
        inspectionImages.putString("thermal_images_json", thermalImagesJson);
        inspectionImages.putString("icedam_images_json", iceDamImagesJson);
        inspectionImages.putString("salt_images_json", saltImagesJson);

        // set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Inspection " + Integer.toString(inspection.id));
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
        startActivity(new Intent(this, PastInspectionsActivity.class));
        finish();
    }

}
