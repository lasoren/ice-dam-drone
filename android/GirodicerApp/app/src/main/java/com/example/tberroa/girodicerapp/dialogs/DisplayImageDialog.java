package com.example.tberroa.girodicerapp.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Window;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.adapters.DisplayImageAdapter;
import com.example.tberroa.girodicerapp.models.InspectionImage;

import java.util.List;

public class DisplayImageDialog extends Dialog {

    private final Context context;
    private final List<InspectionImage> images;
    private final int startingImage;

    public DisplayImageDialog(Context context, List<InspectionImage> images, int startingImage) {
        super(context, R.style.dialogStyle2);
        this.context = context;
        this.images = images;
        this.startingImage = startingImage;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_display_image);

        // initialize recycler view
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.display_image_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        // populate recycler view
        DisplayImageAdapter displayImageAdapter;
        displayImageAdapter = new DisplayImageAdapter(context, images);
        recyclerView.setAdapter(displayImageAdapter);

        // scrolling
        recyclerView.getLayoutManager().scrollToPosition(startingImage);

        setCancelable(true);
    }
}