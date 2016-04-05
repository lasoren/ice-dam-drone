package com.example.tberroa.girodicerapp.database;

import android.util.Log;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.example.tberroa.girodicerapp.models.Client;
import com.example.tberroa.girodicerapp.models.DroneOperator;
import com.example.tberroa.girodicerapp.models.Hotspot;
import com.example.tberroa.girodicerapp.models.IceDam;
import com.example.tberroa.girodicerapp.models.Inspection;
import com.example.tberroa.girodicerapp.models.InspectionImage;
import com.example.tberroa.girodicerapp.models.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("unused")
public class LocalDB {

    public LocalDB() {
    }

    public DroneOperator getOperator() {
        return new Select()
                .from(DroneOperator.class)
                .executeSingle();
    }

    public Client getClient(int id) {
        return new Select()
                .from(Client.class)
                .where("client_id = ?", id)
                .executeSingle();
    }

    public List<Client> getClients() {
        List<Client> clients = new Select()
                .from(Client.class)
                .orderBy("client_id DESC")
                .execute();

        for (Iterator<Client> iterator = clients.listIterator(); iterator.hasNext(); ) {
            Client client = iterator.next();
            if (client.user == null) {
                iterator.remove();
            }
        }
        return clients;
    }

    public Inspection getInspection(int id) {
        return new Select()
                .from(Inspection.class)
                .where("inspection_id = ?", id)
                .executeSingle();
    }

    public List<Inspection> getInspections(Client client) {
        List<Inspection> inspections = new Select()
                .from(Inspection.class)
                .orderBy("inspection_id DESC")
                .execute();

        for (Iterator<Inspection> iterator = inspections.listIterator(); iterator.hasNext(); ) {
            Inspection inspection = iterator.next();
            if (inspection.client.id != client.id) {
                iterator.remove();
            }
        }
        return inspections;
    }

    public String getInspectionThumbnail(Inspection inspection){
        List<InspectionImage> images = new Select()
                .from(InspectionImage.class)
                .where("inspection_id = ?", inspection.id)
                .execute();
        Type type = new TypeToken<List<InspectionImage>>(){}.getType();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        Log.d("dbg", "@LocalDB/getInspectionThumbnail: images is: " + gson.toJson(images, type));

        for (InspectionImage image : images){
            if (image != null && image.path != null){
                Log.d("dbg", "@LocalDB/getInspectionThumbnail: " + image.path + ".jpg");
                return image.path + ".jpg";
            }
        }
        return null;
    }

    public List<InspectionImage> getInspectionImages(int inspectionId) {
        return new Select()
                .from(InspectionImage.class)
                .where("inspection_id = ?", inspectionId)
                .execute();
    }

    public List<InspectionImage> getInspectionImages(Inspection inspection, String imageType) {
        return new Select()
                .from(InspectionImage.class)
                .where("inspection = ?", inspection.getId())
                .where("image_type = ?", imageType)
                .execute();
    }

    public void clear() {
        new Delete().from(Hotspot.class).execute();
        new Delete().from(IceDam.class).execute();
        new Delete().from(InspectionImage.class).execute();
        new Delete().from(Inspection.class).execute();
        new Delete().from(Client.class).execute();
        new Delete().from(DroneOperator.class).execute();
        new Delete().from(User.class).execute();
    }
}
