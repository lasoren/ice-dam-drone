package com.example.tberroa.girodicerapp;



import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class PagerAdapter extends FragmentStatePagerAdapter {

    int numberOfTabs;
    int missionNumber;
    int numberOfAerials;
    int numberOfThermals;
    int numberOfIceDams;
    int numberOfSalts;
    String username;

    public PagerAdapter(FragmentManager fm, int numberOfTabs, int missionNumber, Mission mission, String username) {
        super(fm);
        this.numberOfTabs = numberOfTabs;
        this.missionNumber = missionNumber;
        this.numberOfAerials = mission.numberOfAerials;
        this.numberOfThermals = mission.numberOfThermals;
        this.numberOfIceDams = mission.numberOfIceDams;
        this.numberOfSalts = mission.numberOfSalts;
        this.username = username;
    }

    @Override
    public Fragment getItem(int position) {
        Bundle bundle = new Bundle();
        bundle.putInt("missionNumber", missionNumber);
        bundle.putInt("numberOfAerials", numberOfAerials);
        bundle.putInt("numberOfThermals", numberOfThermals);
        bundle.putInt("numberOfIceDams", numberOfIceDams);
        bundle.putInt("numberOfSalts", numberOfSalts);
        bundle.putString("username", username);
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