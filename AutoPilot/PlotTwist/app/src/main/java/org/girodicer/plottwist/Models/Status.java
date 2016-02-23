package org.girodicer.plottwist.Models;

import com.google.android.gms.maps.model.LatLng;

import java.nio.ByteBuffer;

/**
 * Created by Carlos on 2/22/2016.
 */
public class Status {
    public LatLng location;
    public Double velocity;
    public byte state;
    public int armable;

    public static final Status Unpack(byte[] data){
        ByteBuffer buffer = ByteBuffer.wrap(data);
        Double latitude = buffer.getDouble();
        Double longitude = buffer.getDouble();
        Double velocity = buffer.getDouble();
        byte state = buffer.get();
        int armable = buffer.getInt();
        return new Status(new LatLng(latitude, longitude), velocity, state, armable);
    }

    public Status(LatLng location, Double velocity, byte state, int armable){
        this.location = location;
        this.velocity = velocity;
        this.state = state;
        this.armable = armable;
    }
}
