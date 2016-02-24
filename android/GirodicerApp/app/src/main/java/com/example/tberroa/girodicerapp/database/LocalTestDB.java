package com.example.tberroa.girodicerapp.database;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.example.tberroa.girodicerapp.models.Client;
import com.example.tberroa.girodicerapp.models.DroneOperator;
import com.example.tberroa.girodicerapp.models.Hotspot;
import com.example.tberroa.girodicerapp.models.IceDam;
import com.example.tberroa.girodicerapp.models.Inspection;
import com.example.tberroa.girodicerapp.models.InspectionImage;
import com.example.tberroa.girodicerapp.models.User;

import java.util.List;

public class LocalTestDB {


    public LocalTestDB() {
    }

    public DroneOperator getOperator(){
        return new Select()
                .from(DroneOperator.class)
                .where("operator_id = ?", 1)
                .executeSingle();
    }

    public Client getClient(){
        return new Select()
                .from(Client.class)
                //.where("client_id = ?", id)
                .executeSingle();
    }

    public List<Client> getClients(){
        return new Select()
                .from(Client.class)
                .execute();
    }

    public List<Inspection> getInspections(Client client){
        return new Select()
                .from(Inspection.class)
                .where("client = ?", client.getId())
                .execute();
    }

    public List<InspectionImage> getInspectionImages(Inspection inspection, String imageType){
        return new Select()
                .from(InspectionImage.class)
                .where("inspection = ?", inspection.getId())
                .where("image_type = ?", imageType)
                .execute();
    }

    public void Clear(){
        new Delete().from(Hotspot.class).execute();
        new Delete().from(IceDam.class).execute();
        new Delete().from(InspectionImage.class).execute();
        new Delete().from(Inspection.class).execute();
        new Delete().from(Client.class).execute();
        new Delete().from(DroneOperator.class).execute();
        new Delete().from(User.class).execute();
    }
}
