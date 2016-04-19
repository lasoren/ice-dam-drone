package com.example.tberroa.girodicerapp.bluetooth;

import android.util.Log;

import com.example.tberroa.girodicerapp.data.Params;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings({"WeakerAccess", "unused", "MismatchedQueryAndUpdateOfCollection"})
public class ImageDetails {

    public int image_num;
    public int image_type;
    public boolean detected;
    public String origin;
    public float depth;
    public String[] ice_locations;

    public ImageDetails() {
    }

    public List<LatLng> getIceDamPoints() {
        List<LatLng> points = new ArrayList<>();

        if (ice_locations != null && ice_locations.length > 0) {
            Log.d(Params.TAG_DBG, "@ImageDetails/getIceDamPoints: ice_locations is not empty");
            for (String point : ice_locations) {
                Log.d(Params.TAG_DBG, "@ImageDetails/getIceDamPoints: ice_locations point is " + point);
                List<String> coordinates = Arrays.asList(point.split(",", 2));
                try {
                    float latitude = Float.parseFloat(coordinates.get(0));
                    float longitude = Float.parseFloat(coordinates.get(1));
                    points.add(new LatLng(latitude, longitude));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        String jsonPoints = new Gson().toJson(points);
        Log.d(Params.TAG_DBG, "@ImageDetails/getIceDamPoints: points are " + jsonPoints);
        return points;
    }
}
