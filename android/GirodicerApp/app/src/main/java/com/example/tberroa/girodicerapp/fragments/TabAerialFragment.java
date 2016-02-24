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
import com.example.tberroa.girodicerapp.adapters.FragmentViewAdapter;
import com.example.tberroa.girodicerapp.models.Inspection;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class TabAerialFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tab_aerial, group, false);

        if (isAdded()){
            Inspection inspection = new Inspection();

            // grab data passed to fragment
            Bundle bundle = this.getArguments();
            if (bundle != null) {
                Type typeInspection = new TypeToken<Inspection>(){}.getType();
                inspection = new Gson().fromJson(bundle.getString("inspection_json", ""), typeInspection);
            }

            // grab context
            Context context = getActivity();

            // initialize recycler view
            RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.mission_recycler_view);
            int span = Utilities.getSpanGrid(context);
            int spacing = Utilities.getSpacingGrid(context);
            recyclerView.setLayoutManager(new GridLayoutManager(context, span));
            recyclerView.addItemDecoration(new GridSpacingItemDecoration(span, spacing));

            // populate recycler view
            FragmentViewAdapter fragmentViewAdapter;
            fragmentViewAdapter = new FragmentViewAdapter(context, inspection, Params.AERIAL_TAB);
            recyclerView.setAdapter(fragmentViewAdapter);
        }
        return v;
    }
}
