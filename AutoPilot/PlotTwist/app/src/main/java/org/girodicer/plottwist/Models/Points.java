package org.girodicer.plottwist.Models;

import com.google.android.gms.maps.model.LatLng;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by Carlos on 2/21/2016.
 *
 * Data is packed in the order of latitude, then longitude. Rinse, repeat.
 */
public class Points {

    public static final byte[] Pack(ArrayList<LatLng> points){
        ByteBuffer builder = ByteBuffer.allocate(points.size() * 8 * 2);

        for (LatLng point : points){
            builder.putDouble(point.latitude);
            builder.putDouble(point.longitude);
        }

        return builder.array();
    }

    public static final ArrayList<LatLng> Unpack(byte[] data){
        ArrayList<LatLng> points = new ArrayList<>();
        ByteBuffer buffer = ByteBuffer.wrap(data);

        while(buffer.hasRemaining()){
            LatLng point = new LatLng(buffer.getDouble(), buffer.getDouble());
            points.add(point);
        }

        return points;
    }
}
