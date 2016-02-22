package com.example.tberroa.girodicerapp.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.tberroa.girodicerapp.data.Mission;
import com.example.tberroa.girodicerapp.fragments.TabAerialFragment;
import com.example.tberroa.girodicerapp.fragments.TabIceDamFragment;
import com.example.tberroa.girodicerapp.fragments.TabSaltFragment;
import com.example.tberroa.girodicerapp.fragments.TabThermalFragment;

public class InspectionPagerAdapter extends FragmentStatePagerAdapter {

    private final int numberOfTabs;
    private final int missionNumber;
    private final int numberOfAerials;
    private final int numberOfThermals;
    private final int numberOfIceDams;
    private final int numberOfSalts;
    private final String username;

    public InspectionPagerAdapter(FragmentManager fm, int numOfT, int mNum, Mission m, String user) {
        super(fm);
        numberOfTabs = numOfT;
        missionNumber = mNum;
        numberOfAerials = m.getNumberOfAerials();
        numberOfThermals = m.getNumberOfThermals();
        numberOfIceDams = m.getNumberOfIceDams();
        numberOfSalts = m.getNumberOfSalts();
        username = user;
    }

    @Override
    public Fragment getItem(int position) {

        // create bundle with data required by fragments
        Bundle bundle = new Bundle();
        bundle.putInt("mission_number", missionNumber);
        bundle.putInt("number_of_aerials", numberOfAerials);
        bundle.putInt("number_of_thermals", numberOfThermals);
        bundle.putInt("number_of_icedams", numberOfIceDams);
        bundle.putInt("number_of_salts", numberOfSalts);
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