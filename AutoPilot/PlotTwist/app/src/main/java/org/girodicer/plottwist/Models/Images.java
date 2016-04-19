package org.girodicer.plottwist.Models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayInputStream;

/**
 * Created by Larry on 4/16/2016.
 */
public class Images {
    private Bitmap image;

    public Images(Bitmap image){
        this.image = image;
    }

    public static final Images Unpack(byte[] data){
        ByteArrayInputStream imageStream = new ByteArrayInputStream(data);
        Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
        return new Images(bitmap);
    }

    public Bitmap getImage(){
        return this.image;
    }
}
