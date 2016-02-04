package com.example.tberroa.girodicerapp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tberroa.girodicerapp.GridSpacingItemDecoration;
import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.helpers.Utilities;
import com.example.tberroa.girodicerapp.adapters.MissionViewAdapter;

public class TabAerialFragment extends Fragment {

    int numberOfAerials;
    int missionNumber;
    String username;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tab_aerial, container, false);

        // grab data passed to fragment
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            numberOfAerials = bundle.getInt("numberOfAerials", 0);
            missionNumber = bundle.getInt("missionNumber", 0);
            username = bundle.getString("username", username);
        }

        if (isAdded()){
            // initialize recycler view
            RecyclerView missionsRecyclerView =
                    (RecyclerView) v.findViewById(R.id.mission_recycler_view);
            int span = Utilities.getSpanGrid(getActivity());
            missionsRecyclerView.setLayoutManager(
                    new GridLayoutManager(getActivity(), span));
            missionsRecyclerView.addItemDecoration(new GridSpacingItemDecoration(
                    span, Utilities.getSpacingGrid(getActivity()), true));

            // populate recyclerView
            MissionViewAdapter recyclerAdapter = new MissionViewAdapter(
                    getActivity(), missionNumber, numberOfAerials, "aerial", username);
            missionsRecyclerView.setAdapter(recyclerAdapter);
        }
        return v;
    }
}
