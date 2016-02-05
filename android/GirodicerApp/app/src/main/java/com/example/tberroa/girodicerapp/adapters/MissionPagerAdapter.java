package com.example.tberroa.girodicerapp.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.tberroa.girodicerapp.data.Mission;
import com.example.tberroa.girodicerapp.fragments.TabAerialFragment;
import com.example.tberroa.girodicerapp.fragments.TabIceDamsFragment;
import com.example.tberroa.girodicerapp.fragments.TabSaltFragment;
import com.example.tberroa.girodicerapp.fragments.TabThermalFragment;

public class MissionPagerAdapter extends FragmentStatePagerAdapter {

    private final int numberOfTabs;
    private final int missionNumber;
    private final int numberOfAerials;
    private final int numberOfThermals;
    private final int numberOfIceDams;
    private final int numberOfSalts;
    private final String username;

    public MissionPagerAdapter(FragmentManager fm, int numberOfTabs, int missionNumber,
                               Mission mission, String username) {
        super(fm);
        this.numberOfTabs = numberOfTabs;
        this.missionNumber = missionNumber;
        this.numberOfAerials = mission.getNumberOfAerials();
        this.numberOfThermals = mission.getNumberOfThermals();
        this.numberOfIceDams = mission.getNumberOfIceDams();
        this.numberOfSalts = mission.getNumberOfSalts();
        this.username = username;
    }

    @Override
    public Fragment getItem(int position) {

        // create bundle with data required by fragments
        Bundle bundle = new Bundle();
        bundle.putInt("missionNumber", missionNumber);
        bundle.putInt("numberOfAerials", numberOfAerials);
        bundle.putInt("numberOfThermals", numberOfThermals);
        bundle.putInt("numberOfIceDams", numberOfIceDams);
        bundle.putInt("numberOfSalts", numberOfSalts);
        bundle.putString("username", username);

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
                TabIceDamsFragment tabIceDams = new TabIceDamsFragment();
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