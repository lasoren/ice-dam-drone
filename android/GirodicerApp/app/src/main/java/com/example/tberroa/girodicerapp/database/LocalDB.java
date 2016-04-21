package com.example.tberroa.girodicerapp.database;

import android.support.annotation.Nullable;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.example.tberroa.girodicerapp.models.Client;
import com.example.tberroa.girodicerapp.models.DroneOperator;
import com.example.tberroa.girodicerapp.models.Hotspot;
import com.example.tberroa.girodicerapp.models.IceDam;
import com.example.tberroa.girodicerapp.models.Inspection;
import com.example.tberroa.girodicerapp.models.InspectionImage;
import com.example.tberroa.girodicerapp.models.User;

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

    public Client getClient(int clientId) {
        return new Select()
                .from(Client.class)
                .where("client_id = ?", clientId)
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

    public Inspection getInspection(int inspectionId) {
        return new Select()
                .from(Inspection.class)
                .where("inspection_id = ?", inspectionId)
                .executeSingle();
    }

    public List<Inspection> getInspections(int clientId) {
        List<Inspection> inspections = new Select()
                .from(Inspection.class)
                .orderBy("inspection_id DESC")
                .execute();

        for (Iterator<Inspection> iterator = inspections.listIterator(); iterator.hasNext(); ) {
            Inspection inspection = iterator.next();
            if (inspection.client.id != clientId) {
                iterator.remove();
            }
        }
        return inspections;
    }

    @Nullable
    public String getInspectionThumbnail(int inspectionId){
        List<InspectionImage> images = new Select()
                .from(InspectionImage.class)
                .where("inspection_id = ?", inspectionId)
                .execute();

        for (InspectionImage image : images){
            if (image != null && image.path != null){ // loop through until a valid image is found
                return image.path + ".jpg"; // once a valid image is found, use that as the thumbnail
            }
        }
        return null;
    }

    public List<InspectionImage> getInspectionImages(int inspectionId, int imageType) {
        return new Select()
                .from(InspectionImage.class)
                .where("inspection_id = ?", inspectionId)
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
