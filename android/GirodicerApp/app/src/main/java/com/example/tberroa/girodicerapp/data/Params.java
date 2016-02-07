package com.example.tberroa.girodicerapp.data;


public class Params {

    // parameters related to device storage
    final public static String HOME_FOLDER = "/Girodicer/";

    // network parameters
    final public static String POST_MEDIA_TYPE = "application/x-www-form-urlencoded; charset=utf-8";

    // broadcast parameters
    final public static String DRONE_READY = "DRONE_READY";
    final public static String TRANSFER_COMPLETE = "TRANSFER_COMPLETE";
    final public static String UPLOAD_COMPLETE = "UPLOAD_COMPLETE";
    final public static String RELOAD_ACTIVE_MISSION_ACTIVITY = "RELOAD_ACTIVE_MISSION_ACTIVITY";
    final public static String FETCHING_COMPLETE = "FETCHING_COMPLETE";

    // UI parameters
    final public static String AERIAL_TAB = "aerial";
    final public static String THERMAL_TAB = "thermal";
    final public static String ICEDAM_TAB = "icedam";
    final public static String SALT_TAB = "salt";

    // parameters connected to Amazon Web Services
    final public static String CLOUD_URL = "http://s3.amazonaws.com/girodicer/";
    final public static String CLOUD_CREDENTIALS = "us-east-1:d64bdcf1-1d5e-441e-ba35-0a3876e4c82c";
    final public static String CLOUD_BUCKET_NAME = "girodicer";

    // parameters connected to Altervista server
    final public static String LOGIN_URL = "http://girodicer.altervista.org/login.php";
    final public static String REGISTER_URL = "http://girodicer.altervista.org/register.php";
    final public static String LOGIN_SUCCESS = "success";
    final public static String REGISTER_SUCCESS = "account successfully created";

    private Params(){
    }
}


