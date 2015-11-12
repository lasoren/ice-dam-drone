package com.example.tberroa.girodicerapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TabAerialFragment extends Fragment {

    int num_of_aerials;
    int mission_num;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tab_aerial, container, false);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            num_of_aerials = bundle.getInt("num_of_aerials", 0);
            mission_num = bundle.getInt("mission_num", 0);
        }

        if (isAdded()){
            // initialize recycler view
            RecyclerView missionsRecyclerView = (RecyclerView) v.findViewById(R.id.mission_recycler_view);
            // get screen dimensions
            Context context = getActivity();
            int screenWidth = new Utilities().getScreenWidth(context);
            int screenHeight = new Utilities().getScreenWidth(context);
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
            FragmentRecyclerAdapter recyclerAdapter = new FragmentRecyclerAdapter(getActivity(), mission_num, num_of_aerials);
            missionsRecyclerView.setAdapter(recyclerAdapter);
        }

        return v;
    }
}
