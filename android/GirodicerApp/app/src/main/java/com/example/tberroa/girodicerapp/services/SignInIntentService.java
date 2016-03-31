package com.example.tberroa.girodicerapp.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.amazonaws.services.s3.AmazonS3Client;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.data.UserInfo;
import com.example.tberroa.girodicerapp.database.LocalDB;
import com.example.tberroa.girodicerapp.database.ServerDB;
import com.example.tberroa.girodicerapp.models.Client;
import com.example.tberroa.girodicerapp.models.DroneOperator;
import com.example.tberroa.girodicerapp.models.Inspection;
import com.example.tberroa.girodicerapp.models.InspectionImage;
import com.example.tberroa.girodicerapp.network.CloudTools;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class SignInIntentService extends IntentService {

    public SignInIntentService() {
        super("SignInIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ServerDB serverDB = new ServerDB();
        DroneOperator operator = new LocalDB().getOperator();

        // get clients from server & save them locally
        List<Client> clients = serverDB.getClients(operator);
        if (clients != null && !clients.isEmpty()) {
            for (int i = 0; i < clients.size(); i++) {
                Client client = clients.get(clients.size() - 1 - i);
                if (client.user != null) {
                    client.cascadeSave();
                }
            }
        }

        // get inspections from server & save them locally
        List<Inspection> inspections = serverDB.getInspections(operator);
        if (inspections != null && !inspections.isEmpty()) {
            for (Inspection inspection : inspections) {
                inspection.cascadeSave();
            }
        }

        // get inspection images from server & save them locally
        List<InspectionImage> images = serverDB.getInspectionImages(operator);
        if (images != null && !images.isEmpty()) {

            Type type = new TypeToken<List<InspectionImage>>(){}.getType();
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            Log.d("dbg", "@SignInIntentService: images is: " + gson.toJson(images, type));

            for (InspectionImage image : images) {
                image.save();
            }
        }

        // update user sign in status
        new UserInfo().setUserStatus(this, true);

        operator.save();
    }

    private boolean imageExists(String path) {
        try {
            // get s3 client
            AmazonS3Client s3Client = CloudTools.getAmazonS3Client(this);
            s3Client.getObjectMetadata(Params.CLOUD_BUCKET_NAME, path);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
