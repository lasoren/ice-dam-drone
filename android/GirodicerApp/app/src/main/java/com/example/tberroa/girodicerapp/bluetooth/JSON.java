package com.example.tberroa.girodicerapp.bluetooth;

import java.io.UnsupportedEncodingException;

public class JSON {
    private String json;

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