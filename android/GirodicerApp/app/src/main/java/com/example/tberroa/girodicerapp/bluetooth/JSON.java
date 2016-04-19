package com.example.tberroa.girodicerapp.bluetooth;

import java.io.UnsupportedEncodingException;

@SuppressWarnings({"WeakerAccess", "unused", "MismatchedQueryAndUpdateOfCollection"})
public class JSON {

    private final String json;

    public JSON(String json) {
        this.json = json;
    }

    public static JSON Unpack(byte[] data) {
        String string = "";
        try {
            string = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return new JSON(string);
    }

    public String getJson(){
        return this.json;
    }

}