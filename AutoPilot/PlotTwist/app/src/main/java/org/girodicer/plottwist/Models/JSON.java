package org.girodicer.plottwist.Models;

import java.io.UnsupportedEncodingException;

/**
 * Created by Larry on 4/15/2016.
 */
public class JSON {
    private String json;

    public JSON(String json) {
        this.json = json;
    }

    public static final JSON Unpack(byte[] data) {
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
