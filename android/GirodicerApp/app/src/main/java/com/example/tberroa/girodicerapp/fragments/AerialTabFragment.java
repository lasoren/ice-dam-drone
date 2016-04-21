package com.example.tberroa.girodicerapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.activeandroid.ActiveAndroid;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.database.ServerDB;
import com.example.tberroa.girodicerapp.helpers.GridSpacingItemDecoration;
import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.helpers.Utilities;
import com.example.tberroa.girodicerapp.adapters.InspectionImagesAdapter;
import com.example.tberroa.girodicerapp.models.InspectionImage;

import java.util.List;

public class AerialTabFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private Context context;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tab_images, group, false);

        if (isAdded()) {
            String inspectionImagesJson = "";

            // grab data passed to fragment
            Bundle bundle = this.getArguments();
            if (bundle != null) {
                // grab the serialized images
                inspectionImagesJson = bundle.getString(Integer.toString(Params.I_TYPE_AERIAL), "");
            }

            // grab context
            context = getActivity();

            // initialize swipe refresh layout
            swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh_layout);
            swipeRefreshLayout.setOnRefreshListener(this);

            // initialize recycler view
            int span = Utilities.getSpanGrid(context);
            int spacing = Utilities.getSpacingGrid(context);
            RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.inspection_recycler_view);
            recyclerView.setLayoutManager(new GridLayoutManager(context, span));
            recyclerView.addItemDecoration(new GridSpacingItemDecoration(span, spacing));

            // populate recycler view
            InspectionImagesAdapter inspectionImagesAdapter;
            inspectionImagesAdapter = new InspectionImagesAdapter(context, inspectionImagesJson);
            recyclerView.setAdapter(inspectionImagesAdapter);
        }
        return v;
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        new UpdateImages().execute();
    }

    class UpdateImages extends AsyncTask<Void, Void, Void> {

        boolean receivedNewImages;

        @Override
        protected Void doInBackground(Void... params) {
            if (Utilities.isInternetAvailable(context)) {
                List<InspectionImage> newImages = new ServerDB(context).getInspectionImages();
                if (newImages != null && !newImages.isEmpty()) {
                    receivedNewImages = true;

                    // save new images locally
                    ActiveAndroid.beginTransaction();
                    try {
                        for (InspectionImage image : newImages) {
                            image.save();
                        }
                        ActiveAndroid.setTransactionSuccessful();
                    } finally {
                        ActiveAndroid.endTransaction();
                    }
                } else {
                    receivedNewImages = false;
                }
            } else {
                receivedNewImages = false;
            }
            return null;
        }

        protected void onPostExecute(Void param) {
            swipeRefreshLayout.setRefreshing(false);
            if (receivedNewImages) {
                // reload the activity
                Intent intent = getActivity().getIntent();
                intent.setAction(Params.RELOAD).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                getActivity().finish();
            }
        }
    }
}
