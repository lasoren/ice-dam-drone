package com.example.tberroa.girodicerapp.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.fragments.DroneMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class ConfirmIceDamDialog extends Dialog {

    private final Marker marker;
    private final Bitmap image;

    public ConfirmIceDamDialog(Context context, Marker marker, Bitmap image) {
        super(context, R.style.dialogStyle);
        this.marker = marker;
        this.image = image;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_confirm_icedam);

        // get point
        final LatLng point = marker.getPosition();

        // set point text view
        TextView pointView = (TextView) findViewById(R.id.point);
        String latitude = String.format("%f", point.latitude);
        String longitude = String.format("%f", point.longitude);
        String pointFormatted = "(" + latitude + "," + longitude + ")";
        pointView.setText(pointFormatted);

        // initialize image view
        ImageView imageView = (ImageView) findViewById(R.id.icedam_image);

        // initialize no image text view
        TextView noImageView = (TextView) findViewById(R.id.no_image);

        // set image
        if (image != null){
            noImageView.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(image);
        } else{
            imageView.setVisibility(View.GONE);
            noImageView.setVisibility(View.VISIBLE);
        }

        // set buttons
        Button noButton = (Button) findViewById(R.id.no_button);
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                marker.remove();
                dismiss();
            }
        });
        Button yesButton = (Button) findViewById(R.id.yes_button);
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DroneMapFragment.confirmedIceDamPoints.add(point);
                marker.remove();
                dismiss();
            }
        });

        setTitle(R.string.confirm_icedam);
        setCancelable(true);
    }
}
