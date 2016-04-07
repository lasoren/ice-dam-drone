package com.example.tberroa.girodicerapp.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.helpers.Utilities;
import com.squareup.picasso.Picasso;

public class DisplayImageDialog extends Dialog {

    private final Context context;
    private final String imagePath;

    public DisplayImageDialog(Context context, String imagePath) {
        super(context, R.style.dialogStyle2);
        this.context = context;
        this.imagePath = imagePath;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_image_display);

        // initialize image view
        ImageView image = (ImageView) findViewById(R.id.image);

        // create url for this inspection image
        String url = Params.CLOUD_URL + imagePath + ".jpg";

        // render image with Picasso
        int height = Utilities.getScreenHeight(context);
        int width = Utilities.getScreenWidth(context);
        Picasso.with(context).load(url).resize(width, height).centerInside().into(image);

        setCancelable(true);
    }
}