package com.example.tberroa.girodicerapp.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.data.UserInfo;
import com.example.tberroa.girodicerapp.database.LocalDB;
import com.example.tberroa.girodicerapp.database.ServerDB;
import com.example.tberroa.girodicerapp.models.Client;
import com.example.tberroa.girodicerapp.models.Inspection;
import com.example.tberroa.girodicerapp.models.InspectionImage;

import java.util.Iterator;
import java.util.List;

public class SignInIntentService extends IntentService {

    public SignInIntentService() {
        super("SignInIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ServerDB serverDB = new ServerDB(this);

        // get clients from server & save them locally
        List<Client> clients = serverDB.getClients();
        if (clients != null && !clients.isEmpty()) {
            Log.d(Params.TAG_DBG, "@SignInIntentService: clients not null and not empty");
            ActiveAndroid.beginTransaction();
            try {
                for (Client client : clients) {
                    if (client.user != null) {
                        client.cascadeSave();
                    }
                }
                ActiveAndroid.setTransactionSuccessful();
            } finally {
                ActiveAndroid.endTransaction();
            }
        }

        // get inspections from server & save them locally
        List<Inspection> inspections = serverDB.getInspections();
        if (inspections != null && !inspections.isEmpty()) {
            Log.d(Params.TAG_DBG, "@SignInIntentService: inspections not null and not empty");
            ActiveAndroid.beginTransaction();
            try {
                for (Inspection inspection : inspections) {
                    inspection.cascadeSave();
                }
                ActiveAndroid.setTransactionSuccessful();
            } finally {
                ActiveAndroid.endTransaction();
            }
        }

        // get inspection images from server & save them locally
        List<InspectionImage> images = serverDB.getInspectionImages();
        if (images != null && !images.isEmpty()) {
            Log.d(Params.TAG_DBG, "@SignInIntentService: images not null and not empty");
            ActiveAndroid.beginTransaction();
            try {
                for (InspectionImage image : images) {
                    image.save();
                }
                ActiveAndroid.setTransactionSuccessful();
            } finally {
                ActiveAndroid.endTransaction();
            }
        }

        // remove any inspections which can't produce a thumbnail url
        if (inspections != null && !inspections.isEmpty()) {
            LocalDB localDB = new LocalDB();
            ActiveAndroid.beginTransaction();
            try {
                for (Iterator<Inspection> iterator = inspections.listIterator(); iterator.hasNext(); ) {
                    Inspection inspection = iterator.next();
                    String url = localDB.getInspectionThumbnail(inspection.id);
                    if (url == null) {
                        iterator.remove();
                        inspection.delete();
                    }
                }
            } finally {
                ActiveAndroid.setTransactionSuccessful();
            }
            ActiveAndroid.endTransaction();
        }

        // update user sign in status
        new UserInfo().setUserStatus(this, true);

        // broadcast that service is complete
        sendBroadcast(new Intent().setAction(Params.SIGN_IN_SERVICE_COMPLETE));
        Log.d(Params.TAG_DBG, "@SignInIntentService: service complete");
    }
}
