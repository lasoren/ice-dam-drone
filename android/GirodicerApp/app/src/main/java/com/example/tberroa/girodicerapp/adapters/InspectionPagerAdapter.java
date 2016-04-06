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
        ImageTabFragment tab = new ImageTabFragment();
        switch (position) {
            case 0:
                inspectionImages.putString("type", Integer.toString(Params.I_TYPE_AERIAL));
                tab.setArguments(inspectionImages);
                break;
            case 1:
                inspectionImages.putString("type", Integer.toString(Params.I_TYPE_THERMAL));
                tab.setArguments(inspectionImages);
                break;
            case 2:
                inspectionImages.putString("type", Integer.toString(Params.I_TYPE_ROOF_EDGE));
                tab.setArguments(inspectionImages);
                break;
            default:
                return null;
        }
        return tab;
    }

    @Override
    public int getCount() {
        return numberOfTabs;
    }
}