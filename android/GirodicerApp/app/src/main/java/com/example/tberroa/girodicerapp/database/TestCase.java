package com.example.tberroa.girodicerapp.database;

import android.util.Log;

import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.models.Client;
import com.example.tberroa.girodicerapp.models.DroneOperator;
import com.example.tberroa.girodicerapp.models.Inspection;
import com.example.tberroa.girodicerapp.models.InspectionImage;
import com.example.tberroa.girodicerapp.models.User;

import java.util.ArrayList;

/**
 * local database to be loaded up on app start for the purpose of testing
 */
public class TestCase {

    public TestCase(){
    }

    public void Create(){
        // create operator and client
        User operatorUser = new User(1, "today", "Thomas", "Berroa", "tberroa@outlook.com");
        operatorUser.save();
        User clientUser = new User(2, "today", "Luke", "Sorenson", "lasoren@gmail.com");
        clientUser.save();
        DroneOperator droneOperator = new DroneOperator(1, "today", operatorUser, "1234");
        droneOperator.CascadeSave();
        Client client = new Client(1, "today", clientUser, "328 Centre St", 0);
        client.CascadeSave();

        // create inspections
        ArrayList<Inspection> inspections = new ArrayList<>(4);
        Inspection inspection;
        for (int i=0; i<4; i++){
            inspection = new Inspection(i+1, "today", droneOperator, client, 0);
            inspection.CascadeSave();
            inspections.add(inspection);
        }

        // create inspection images
        String inspectionNumber[] = {"1", "2", "3", "4"};               // iterator
        String imageTypes[] = {"aerial", "thermal", "icedam", "salt"};  // iterator
        String imageNumber[] = {"1", "2", "3", "4", "5"};               // iterator
        ArrayList<InspectionImage> inspectionImages = new ArrayList<>(20);
        int i = 0;
        for (String iNum : inspectionNumber){
            for (String type : imageTypes){
                for (String num : imageNumber){

                    String url = Params.CLOUD_URL + iNum+"/images/"+type+num+".jpg";
                    Log.d("test1", "creation url for I"+iNum+"= "+url);
                    InspectionImage inspectionImage = new InspectionImage("today", "today", inspections.get(i), type, url, 0);
                    inspectionImage.CascadeSave();
                    inspectionImages.add(inspectionImage);
                }
            }
            i++;
        }
    }
}
