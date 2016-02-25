package com.example.tberroa.girodicerapp.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.tberroa.girodicerapp.fragments.TabAerialFragment;
import com.example.tberroa.girodicerapp.fragments.TabIceDamFragment;
import com.example.tberroa.girodicerapp.fragments.TabSaltFragment;
import com.example.tberroa.girodicerapp.fragments.TabThermalFragment;

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
                TabAerialFragment tabAerial = new TabAerialFragment();
                tabAerial.setArguments(inspectionImages);
                return tabAerial;
            case 1:
                TabThermalFragment tabThermal = new TabThermalFragment();
                tabThermal.setArguments(inspectionImages);
                return tabThermal;
            case 2:
                TabIceDamFragment tabIceDams = new TabIceDamFragment();
                tabIceDams.setArguments(inspectionImages);
                return tabIceDams;
            case 3:
                TabSaltFragment tabSalt = new TabSaltFragment();
                tabSalt.setArguments(inspectionImages);
                return tabSalt;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numberOfTabs;
    }
}