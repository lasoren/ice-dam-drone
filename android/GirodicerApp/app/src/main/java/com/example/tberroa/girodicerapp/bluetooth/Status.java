package com.example.tberroa.girodicerapp.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.nio.ByteBuffer;

public class Status implements Parcelable {
    public LatLng location;
    public Double velocity;
    public byte state;
    public int armable;

    protected Status(Parcel in) {
        location = in.readParcelable(LatLng.class.getClassLoader());
        velocity = in.readDouble();
        state = in.readByte();
        armable = in.readInt();
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
        byte state = buffer.get();
        int armable = buffer.getInt();
        return new Status(new LatLng(latitude, longitude), velocity, state, armable);
    }

    public Status(LatLng location, Double velocity, byte state, int armable) {
        this.location = location;
        this.velocity = velocity;
        this.state = state;
        this.armable = armable;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(location, flags);
        dest.writeDouble(velocity);
        dest.writeByte(state);
        dest.writeInt(armable);
    }
}
