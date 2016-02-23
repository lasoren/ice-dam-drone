package com.example.tberroa.girodicerapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.helpers.GridSpacingItemDecoration;
import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.helpers.Utilities;
import com.example.tberroa.girodicerapp.adapters.InspectionViewAdapter;

public class TabAerialFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tab_aerial, group, false);

        if (isAdded()){
            int missionNum = 0;
            int numberOfAerials = 0;
            String user = "";

            // grab data passed to fragment
            Bundle bundle = this.getArguments();
            if (bundle != null) {
                numberOfAerials = bundle.getInt("number_of_aerials", 0);
                missionNum = bundle.getInt("mission_number", 0);
                user = bundle.getString("username", user);
            }

            // grab context
            Context c = getActivity();

            // initialize recycler view
            RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.mission_recycler_view);
            int span = Utilities.getSpanGrid(c);
            int spacing = Utilities.getSpacingGrid(c);
            recyclerView.setLayoutManager(new GridLayoutManager(c, span));
            recyclerView.addItemDecoration(new GridSpacingItemDecoration(span, spacing));

            // populate recycler view
            InspectionViewAdapter a;
            a = new InspectionViewAdapter(c, missionNum, numberOfAerials, Params.AERIAL_TAB, user);
            recyclerView.setAdapter(a);
        }
        return v;
    }
}
