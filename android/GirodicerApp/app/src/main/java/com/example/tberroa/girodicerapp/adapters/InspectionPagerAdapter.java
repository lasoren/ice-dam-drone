package com.example.tberroa.girodicerapp.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.tberroa.girodicerapp.fragments.TabAerialFragment;
import com.example.tberroa.girodicerapp.fragments.TabIceDamFragment;
import com.example.tberroa.girodicerapp.fragments.TabSaltFragment;
import com.example.tberroa.girodicerapp.fragments.TabThermalFragment;
import com.example.tberroa.girodicerapp.models.Inspection;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class InspectionPagerAdapter extends FragmentStatePagerAdapter {

    private final int numberOfTabs;
    private final String inspectionJson;

    public InspectionPagerAdapter(FragmentManager fragmentManager, int numberOfTabs, String inspectionJson) {
        super(fragmentManager);
        this.numberOfTabs = numberOfTabs;
        this.inspectionJson = inspectionJson;
    }

    @Override
    public Fragment getItem(int position) {

        // create bundle with data required by fragments
        Bundle bundle = new Bundle();
        bundle.putString("inspection_json", inspectionJson);

        // load up the proper fragment based on tab position and pass it the bundle
        switch (position) {
            case 0:
                TabAerialFragment tabAerial = new TabAerialFragment();
                tabAerial.setArguments(bundle);
                return tabAerial;
            case 1:
                TabThermalFragment tabThermal = new TabThermalFragment();
                tabThermal.setArguments(bundle);
                return tabThermal;
            case 2:
                TabIceDamFragment tabIceDams = new TabIceDamFragment();
                tabIceDams.setArguments(bundle);
                return tabIceDams;
            case 3:
                TabSaltFragment tabSalt = new TabSaltFragment();
                tabSalt.setArguments(bundle);
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