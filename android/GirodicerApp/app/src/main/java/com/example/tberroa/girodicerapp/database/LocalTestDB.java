package com.example.tberroa.girodicerapp.database;

import com.activeandroid.query.Select;
import com.example.tberroa.girodicerapp.models.Client;
import com.example.tberroa.girodicerapp.models.DroneOperator;
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
                //.where("client = ?", client)
                .execute();
    }

    public List<InspectionImage> getInspectionImages(Inspection inspection, String imageType){
        return new Select()
                .from(InspectionImage.class)
                .where("inspection = ?", inspection)
                .where("image_type = ?", imageType)
                .execute();
    }

    public int getNumberOfImages(Inspection inspection){
        return new Select()
                .from(InspectionImage.class)
                .where("inspection = ?", inspection)
                .execute()
                .size();
    }

    public int getNumberOfType(Inspection inspection, String imageType){
        return new Select()
                .from(InspectionImage.class)
                .where("inspection = ?", inspection)
                .where("image_type = ?", imageType)
                .execute()
                .size();
    }
}
