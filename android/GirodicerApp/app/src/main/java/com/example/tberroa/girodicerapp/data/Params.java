package com.example.tberroa.girodicerapp.data;


public class Params {

    private Params(){
    }

    // parameters related to device storage
    final public static String HOME_FOLDER = "/Girodicer/";

    // network parameters
    final public static String POST_MEDIA_TYPE = "application/json; charset=utf-8";

    // broadcast parameters
    final public static String INSPECTION_STARTED = "INSPECTION_STARTED";
    final public static String DRONE_DONE = "DRONE_DONE";
    final public static String TRANSFER_STARTED = "TRANSFER_STARTED";
    final public static String TRANSFER_COMPLETE = "TRANSFER_COMPLETE";
    final public static String UPLOAD_STARTED = "UPLOAD_STARTED";
    final public static String UPLOAD_COMPLETE = "UPLOAD_COMPLETE";
    final public static String RELOAD_AM_ACTIVITY = "RELOAD_AM_ACTIVITY";
    final public static String UPDATING_STARTED = "UPDATING_STARTED";
    final public static String UPDATING_COMPLETE = "UPDATING_COMPLETE";

    // UI parameters
    final public static String AERIAL_TAB = "aerial";
    final public static String THERMAL_TAB = "thermal";
    final public static String ICEDAM_TAB = "icedam";
    final public static String SALT_TAB = "salt";

    // parameters connected to Amazon Web Services
    final public static String CLOUD_URL = "http://s3.amazonaws.com/girodicer/";
    final public static String CLOUD_CREDENTIALS = "us-east-1:d64bdcf1-1d5e-441e-ba35-0a3876e4c82c";
    final public static String CLOUD_BUCKET_NAME = "girodicer";

    // parameters connected to backend
    final public static String BASE_URL = "http://ec2-54-86-133-171.compute-1.amazonaws.com:8000/";
}


