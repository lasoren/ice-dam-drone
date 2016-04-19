package com.example.tberroa.girodicerapp.bluetooth;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayInputStream;

@SuppressWarnings({"WeakerAccess", "unused", "MismatchedQueryAndUpdateOfCollection"})
public class Images {

    private final Bitmap image;

    public Images(Bitmap image){
        this.image = image;
    }

    public static Images Unpack(byte[] data){
        ByteArrayInputStream imageStream = new ByteArrayInputStream(data);
        Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
        return new Images(bitmap);
    }

    public Bitmap getImage(){
        return this.image;
    }
}
