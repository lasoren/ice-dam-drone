package com.example.tberroa.girodicerapp.data;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;

@SuppressWarnings("unused")
public class Params {

    private Params() {
    }

    // log tags, feel free to concatenate them
    final public static String TAG_DBG = " TAG_DBG";
    final public static String TAG_BT = " TAG_BT";
    final public static String TAG_GP = " TAG_GP";
    final public static String TAG_DS = " TAG_DS";
    final public static String TAG_STATUS = "TAG_STATUS";
    final public static String TAG_MAP = "TAG_MAP";
    final public static String TAG_HTTP = "TAG_HTTP";
    final public static String TAG_ERROR = " TAG_ERROR";
    final public static String TAG_EXCEPTION = " TAG_EXCEPTION";

    // menu button ids
    final public static int TERMINATE_INSPECTION = 1055;

    // parameters related to device storage
    final public static String HOME_FOLDER = "/Girodicer/";

    // network parameters
    final public static String POST_MEDIA_TYPE = "application/json; charset=utf-8";

    @SuppressLint("SimpleDateFormat")
    final public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
    @SuppressLint("SimpleDateFormat")
    final public static SimpleDateFormat OUT_FORMAT = new SimpleDateFormat("h:mm aa - E M/d");

    // broadcasts
    // authentication related
    final public static String SIGN_IN_SERVICE_COMPLETE = "SIGN_IN_SERVICE_COMPLETE";
    // bluetooth connection related
    final public static String BLUETOOTH_TIMEOUT = "BLUETOOTH_TIMEOUT";
    final public static String CONNECTING_TO_DRONE = "CONNECTING_TO_DRONE";
    final public static String DRONE_CONNECT_SUCCESS = "DRONE_CONNECT_SUCCESS";
    final public static String DRONE_CONNECT_FAILURE = "DRONE_CONNECT_FAILURE";
    final public static String DRONE_CONNECTION_LOST = "DRONE_CONNECTION_LOST";
    // drone status related
    final public static String INITIAL_STATUS_RECEIVED = "INITIAL_STATUS_RECEIVED";
    final public static String HOUSE_BOUNDARY_RECEIVED = "HOUSE_BOUNDARY_RECEIVED";
    final public static String STATUS_UPDATE = "STATUS_UPDATE";
    // inspection phase related
    final public static String INSPECTION_TERMINATED = "INSPECTION_TERMINATED";
    final public static String INSPECTION_STARTED = "INSPECTION_STARTED"; // start of scanning
    final public static String SALTING_STARTED = "SALTING_STARTED";
    final public static String TRANSFER_STARTED = "TRANSFER_STARTED";
    final public static String UPLOAD_STARTED = "UPLOAD_STARTED";
    final public static String INSPECTION_COMPLETE = "INSPECTION_COMPLETE"; // end of uploading

    // bluetooth states
    final public static int BTS_NOT_CONNECTED = 0;
    final public static int BTS_CONNECTING = 1;
    final public static int BTS_CONNECTED = 2;
    final public static int BTS_CONNECTION_LOST = 3;

    // bluetooth error codes
    final public static int BTE_NO_ERROR = 0;
    final public static int BTE_CONNECT_FAILED = -1;
    final public static int BTE_NOT_ENABLED = -2;
    final public static int BTE_TIMEOUT = -3;

    // inspection phases
    final public static int CI_INACTIVE = 0;
    final public static int CI_SCANNING = 1;
    final public static int CI_SALTING = 2;
    final public static int CI_TRANSFERRING = 3;
    final public static int CI_UPLOADING = 4;

    // UI parameters
    final public static String RELOAD = "reload"; // reload activity without animation
    final public static String IMAGE_UPLOAD_COMPLETE = "IMAGE_UPLOAD_COMPLETE";
    final public static String AERIAL_TAB = "aerial";
    final public static String THERMAL_TAB = "thermal";
    final public static String ROOF_EDGE_TAB = "roof edge";

    // parameters connected to Amazon Web Services
    final public static String CLOUD_URL = "http://s3.amazonaws.com/girodicer/";
    final public static String CLOUD_CREDENTIALS = "us-east-1:d64bdcf1-1d5e-441e-ba35-0a3876e4c82c";
    final public static String CLOUD_BUCKET_NAME = "girodicer";
    final public static int THUMB_SIZE = 128;

    // parameters connected to google static map api
    final public static String GOOGLE_STATIC_MAPS_URL = "https://maps.googleapis.com/maps/api/staticmap?";
    final public static String GOOGLE_STATIC_MAPS_KEY = "AIzaSyB8EAWQMMdbJAIonkn_lmI6AWlnuQsEJsc";

    // parameters connected to the backend
    final public static String BASE_URL = "http://ec2-54-86-133-171.compute-1.amazonaws.com:8000/";
    final public static String CREATE_CLIENT_URL = Params.BASE_URL + "users/client/create.json";
    final public static String CREATE_INSPECTION_URL = Params.BASE_URL + "inspections/create.json";
    final public static String CREATE_INSPECTION_IMAGES_URL = Params.BASE_URL + "inspections/images/create.json";
    final public static String GET_CLIENTS_URL = Params.BASE_URL + "users/clients/get.json";
    final public static String GET_INSPECTIONS_URL = Params.BASE_URL + "inspections/get.json";
    final public static String GET_INSPECTION_IMAGES_URL = Params.BASE_URL + "inspections/images/get.json";
    final public static String CLIENT_INSPECTION_PORTAL = Params.BASE_URL + "inspections/email/client.json";

    // image types
    final public static int I_TYPE_NOT_SPECIFIED = 1;
    final public static int I_TYPE_ROOF_EDGE = 2;
    final public static int I_TYPE_THERMAL = 3;
    final public static int I_TYPE_AERIAL = 4;

    // treatment options
    final public static int NOTHING_DONE = 1;
    final public static int SALTED_BY_DRONE = 2;
    final public static int POWER_WASHED = 3;
    final public static int ICE_PICKED = 4;
    final public static int OTHER = 5;
}


