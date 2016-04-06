package com.example.tberroa.girodicerapp.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.data.UserInfo;
import com.example.tberroa.girodicerapp.database.LocalDB;
import com.example.tberroa.girodicerapp.database.ServerDB;
import com.example.tberroa.girodicerapp.models.Client;
import com.example.tberroa.girodicerapp.models.Inspection;
import com.example.tberroa.girodicerapp.models.InspectionImage;
import com.example.tberroa.girodicerapp.network.Http;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
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
            for (int i = 0; i < clients.size(); i++) {
                Client client = clients.get(clients.size() - 1 - i);
                if (client.user != null) {
                    client.cascadeSave();
                }
            }
        }

        // get inspections from server & save them locally
        List<Inspection> inspections = serverDB.getInspections();
        if (inspections != null && !inspections.isEmpty()) {
            for (Inspection inspection : inspections) {
                inspection.cascadeSave();
            }
        }

        // get inspection images from server & save them locally
        List<InspectionImage> images = serverDB.getInspectionImages();
        Http http = new Http();
        if (images != null && !images.isEmpty()) {
            Type type = new TypeToken<List<InspectionImage>>() {
            }.getType();
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            Log.d("dbg", "@SignInIntentService: images is: " + gson.toJson(images, type));

            for (InspectionImage image : images) {
                // make sure image & corresponding thumbnail are actually in S3 bucket
                if (validImage(http, image.path + ".jpg") && validImage(http, image.path + "_s.jpg")) {
                    image.save();
                }
                else{
                    Log.d("dbg", "@SignInIntentService: image not saved due to invalid path: " + image.path);
                }
            }
        }

        // remove any inspections which can't produce a thumbnail url
        LocalDB localDB = new LocalDB();
        if (inspections != null && !inspections.isEmpty()) {
            for (Iterator<Inspection> iterator = inspections.listIterator(); iterator.hasNext(); ) {
                Inspection inspection = iterator.next();
                String url = localDB.getInspectionThumbnail(inspection.id);
                if (url == null) {
                    iterator.remove();
                    inspection.delete();
                }
            }
        }


        // update user sign in status
        new UserInfo().setUserStatus(this, true);

        // broadcast that service is complete
        sendBroadcast(new Intent().setAction(Params.SIGN_IN_SERVICE_COMPLETE));
    }

    private boolean validImage(Http http, String path) {
        String url = Params.CLOUD_URL + path;
        int code = 0;
        try {
            code = http.getRequestCode(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (code == 200);
    }
}
