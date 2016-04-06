package com.example.tberroa.girodicerapp.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.fragments.ImageTabFragment;

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
                inspectionImages.putString("type", Integer.toString(Params.I_TYPE_AERIAL));
                ImageTabFragment tabAerial = new ImageTabFragment();
                tabAerial.setArguments(inspectionImages);
                return tabAerial;
            case 1:
                inspectionImages.putString("type", Integer.toString(Params.I_TYPE_THERMAL));
                ImageTabFragment tabThermal = new ImageTabFragment();
                tabThermal.setArguments(inspectionImages);
                return tabThermal;
            case 2:
                inspectionImages.putString("type", Integer.toString(Params.I_TYPE_ROOF_EDGE));
                ImageTabFragment tabRoofEdge = new ImageTabFragment();
                tabRoofEdge.setArguments(inspectionImages);
                return tabRoofEdge;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numberOfTabs;
    }
}