package com.example.tberroa.girodicerapp;



import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;
    int missionNum;
    int numOfAerials;

    public PagerAdapter(FragmentManager fm, int NumOfTabs, int missionNum, int numOfAerials) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
        this.missionNum = missionNum;
        this.numOfAerials = numOfAerials;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                TabAerialFragment tabAerial = new TabAerialFragment();
                // store mission data in bundle for the fragments to access
                Bundle bundle = new Bundle();
                bundle.putInt("num_of_aerials", numOfAerials);
                bundle.putInt("mission_num", missionNum);
                tabAerial.setArguments(bundle);
                return tabAerial;
            case 1:
                TabThermalFragment tabThermal = new TabThermalFragment();
                return tabThermal;
            case 2:
                TabIceDamsFragment tabIceDams = new TabIceDamsFragment();
                return tabIceDams;
            case 3:
                TabSaltFragment tabSalt = new TabSaltFragment();
                return tabSalt;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}