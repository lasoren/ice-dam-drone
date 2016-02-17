package org.girodicer.plottwist.services;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by carlos on 10/6/2015.
 */
public class GetAddress extends IntentService {
    public static final String RECEIVER = "GetAddress.RECEIVER";
    public static final String LOCATION_DATA = "GetAddress.LOCATION_DATA";
    public static final String RECEIVER_DATA = "GetAddress.RECEIVER_DATA";
    private static final String TAG = "dbg";
    private static final int okay = 0;
    private static final int failure = -1;
    private ResultReceiver mReceiver;

    public GetAddress() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String errorMessage = "";
        mReceiver = intent.getParcelableExtra(RECEIVER);

        if (mReceiver == null){
            return;
        }

        String location = intent.getStringExtra(LOCATION_DATA);
        if(location == null){
            return;
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        // Address found using the Geocoder.
        List<Address> addresses = null;

        try {
            // Using getFromLocation() returns an array of Addresses for the area immediately
            // surrounding the given latitude and longitude. The results are a best guess and are
            // not guaranteed to be accurate.
            addresses = geocoder.getFromLocationName(location, 1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = "Service not available";

        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = "Invalid lat lng used";
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size()  == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = "No addresses found";

            }
            deliverResultToReceiver(failure, errorMessage);
        } else {
            Address address = addresses.get(0);
            LatLng coordinates = new LatLng(address.getLatitude(), address.getLongitude());

            deliverResultToReceiver(okay, coordinates);
        }
    }

    /**
     * Sends a resultCode and message to the receiver.
     */
    private void deliverResultToReceiver(int resultCode, LatLng coordinates) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(RECEIVER_DATA, coordinates);
        mReceiver.send(resultCode, bundle);
    }

    private void deliverResultToReceiver(int resultCode, String message){
        Bundle bundle = new Bundle();
        bundle.putString(RECEIVER_DATA, message);
        mReceiver.send(resultCode, bundle);
    }
}
