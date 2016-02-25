package com.example.tberroa.girodicerapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tberroa.girodicerapp.helpers.GridSpacingItemDecoration;
import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.helpers.Utilities;
import com.example.tberroa.girodicerapp.adapters.ImagesViewAdapter;

public class TabAerialFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tab_images, group, false);

        if (isAdded()){
            String inspectionImagesJson = "";

            // grab data passed to fragment
            Bundle bundle = this.getArguments();
            if (bundle != null) {
                // grab the serialized images
                inspectionImagesJson = bundle.getString("aerial_images_json", "");
            }

            // grab context
            Context context = getActivity();

            // initialize recycler view
            RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.inspection_recycler_view);
            int span = Utilities.getSpanGrid(context);
            int spacing = Utilities.getSpacingGrid(context);
            recyclerView.setLayoutManager(new GridLayoutManager(context, span));
            recyclerView.addItemDecoration(new GridSpacingItemDecoration(span, spacing));

            // populate recycler view
            ImagesViewAdapter imagesViewAdapter;
            imagesViewAdapter = new ImagesViewAdapter(context, inspectionImagesJson);
            recyclerView.setAdapter(imagesViewAdapter);
        }
        return v;
    }
}
