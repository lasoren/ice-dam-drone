package com.example.tberroa.girodicerapp.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.nio.ByteBuffer;

@SuppressWarnings({"WeakerAccess", "unused", "MismatchedQueryAndUpdateOfCollection"})
public class Status implements Parcelable {
    public final LatLng location;
    public final Double velocity;
    public final int battery;

    protected Status(Parcel in) {
        location = in.readParcelable(LatLng.class.getClassLoader());
        velocity = in.readDouble();
        battery = in.readByte();
    }

    public static final Creator<Status> CREATOR = new Creator<Status>() {
        @Override
        public Status createFromParcel(Parcel in) {
            return new Status(in);
        }

        @Override
        public Status[] newArray(int size) {
            return new Status[size];
        }
    };

    public static Status Unpack(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        Float latitude = buffer.getFloat();
        Float longitude = buffer.getFloat();
        Double velocity = buffer.getDouble();
        int battery = buffer.getInt();
        return new Status(new LatLng(latitude, longitude), velocity, battery);
    }

    public Status(LatLng location, Double velocity, int battery) {
        this.location = location;
        this.velocity = velocity;
        this.battery = battery;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(location, flags);
        dest.writeDouble(velocity);
    }
}