package com.example.tberroa.girodicerapp.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.tberroa.girodicerapp.fragments.AerialTabFragment;
import com.example.tberroa.girodicerapp.fragments.RoofEdgeTabFragment;
import com.example.tberroa.girodicerapp.fragments.ThermalTabFragment;

public class InspectionPagerAdapter extends FragmentStatePagerAdapter {

    private final int numberOfTabs;
    private final Bundle inspectionImages;

    public InspectionPagerAdapter(FragmentManager fragmentManager, int numberOfTabs, Bundle inspectionImages) {
        super(fragmentManager);
        this.numberOfTabs = numberOfTabs;
        this.inspectionImages = inspectionImages;
    }

    @Override
    public Fragment getItem(int position) {
        // load up the proper fragment based on tab position and pass it the images
        switch (position) {
            case 0:
                AerialTabFragment aerialTab = new AerialTabFragment();
                aerialTab.setArguments(inspectionImages);
                return aerialTab;
            case 1:
                ThermalTabFragment thermalTab = new ThermalTabFragment();
                thermalTab.setArguments(inspectionImages);
                return thermalTab;
            case 2:
                RoofEdgeTabFragment roofEdgeTab = new RoofEdgeTabFragment();
                roofEdgeTab.setArguments(inspectionImages);
                return roofEdgeTab;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numberOfTabs;
    }
}