package com.example.tberroa.girodicerapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TabAerialFragment extends Fragment {

    int numberOfAerials;
    int missionNumber;
    String username;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tab_aerial, container, false);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            numberOfAerials = bundle.getInt("numberOfAerials", 0);
            missionNumber = bundle.getInt("missionNumber", 0);
            username = bundle.getString("username", username);
        }

        if (isAdded()){
            // initialize recycler view
            RecyclerView missionsRecyclerView = (RecyclerView) v.findViewById(R.id.mission_recycler_view);
            // get screen dimensions
            int screenWidth = Utilities.getScreenWidth(this.getContext());
            int screenHeight = Utilities.getScreenHeight(this.getContext());
            // if screen is landscape, 4 columns, 2 otherwise
            int span;
            if (screenWidth > screenHeight){
                span = 4;
            }
            else{
                span = 2;
            }
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), span);
            missionsRecyclerView.setLayoutManager(gridLayoutManager);
            // populate recyclerView
            TabViewAdapter recyclerAdapter = new TabViewAdapter(getActivity(), missionNumber, numberOfAerials, "aerial", username);
            missionsRecyclerView.setAdapter(recyclerAdapter);
        }

        return v;
    }
}
