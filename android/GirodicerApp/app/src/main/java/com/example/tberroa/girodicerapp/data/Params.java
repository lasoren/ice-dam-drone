package com.example.tberroa.girodicerapp.data;


public class Params {

    private Params(){
    }

    // parameters related to device storage
    final public static String HOME_FOLDER = "/Girodicer/";

    // network parameters
    final public static String POST_MEDIA_TYPE = "application/json; charset=utf-8";

    // broadcast parameters
    final public static String BLUETOOTH_NOT_ENABLED = "BLUETOOTH_NOT_ENABLED";
    final public static String CONNECTING_TO_DRONE = "CONNECTING_TO_DRONE";             // phase -7
    final public static String DRONE_CONNECT_SUCCESS = "DRONE_CONNECT_SUCCESS";         // phase -6
    final public static String DRONE_CONNECT_FAILURE = "DRONE_CONNECT_FAILURE";         // phase -5
    final public static String DRONE_CONNECTION_LOST = "DRONE_CONNECTION_LOST";         // phase -4
    final public static String DRONE_RECONNECT_SUCCESS = "DRONE_RECONNECT_SUCCESS";     // phase -3
    final public static String DRONE_RECONNECT_FAILURE = "DRONE_RECONNECT_FAILURE";     // phase -2
    final public static String HOUSE_BOUNDARY_RECEIVED = "HOUSE_BOUNDARY_RECEIVED";
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
    final public static String RELOAD = "reload";

    // parameters connected to Amazon Web Services
    final public static String CLOUD_URL = "http://s3.amazonaws.com/girodicer/";
    final public static String CLOUD_CREDENTIALS = "us-east-1:d64bdcf1-1d5e-441e-ba35-0a3876e4c82c";
    final public static String CLOUD_BUCKET_NAME = "girodicer";

    // parameters connected to google static map api
    final public static String GOOGLE_STATIC_MAPS_URL = "https://maps.googleapis.com/maps/api/staticmap?";
    final public static String GOOGLE_STATIC_MAPS_KEY = "AIzaSyB8EAWQMMdbJAIonkn_lmI6AWlnuQsEJsc";

    // parameters connected to backend
    final public static String BASE_URL = "http://ec2-54-86-133-171.compute-1.amazonaws.com:8000/";
}


