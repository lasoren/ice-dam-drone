package com.example.tberroa.girodicerapp.database;

import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.models.Client;
import com.example.tberroa.girodicerapp.models.DroneOperator;
import com.example.tberroa.girodicerapp.models.Inspection;
import com.example.tberroa.girodicerapp.models.InspectionImage;
import com.example.tberroa.girodicerapp.models.User;

import java.util.ArrayList;

// local database to be loaded up on app start for the purpose of testing

public class TestCase {

    public TestCase(){
    }

    public void Create(DroneOperator droneOperator){
        // create 8 clients
        User clientUser1 = new User(50, "today", "Thomas", "Berroa", "tberroa@outlook.com");
        clientUser1.save();
        Client client1 = new Client(50, "today", clientUser1, "328+Centre+St,Boston,MA,02122", 0);
        client1.CascadeSave();

        User clientUser2 = new User(51, "today", "John", "McCain", "jmccain@gmail.com");
        clientUser2.save();
        Client client2 = new Client(51, "today", clientUser2, "3671+Colonial+Drive,Ocean+Springs,MS,39564", 0);
        client2.CascadeSave();

        User clientUser3 = new User(52, "today", "Stevie", "Wonder", "swonder@gmail.com");
        clientUser3.save();
        Client client3 = new Client(53, "today", clientUser3, "6729+Monroe+Drive,Newnan,GA,30263", 0);
        client3.CascadeSave();

        User clientUser4 = new User(54, "today", "Roger", "Goodell", "rgoodell@outlook.com");
        clientUser4.save();
        Client client4 = new Client(54, "today", clientUser4, "5106+Main+Street+West,Rosedale,NY,11422", 0);
        client4.CascadeSave();

        User clientUser5 = new User(55, "today", "Tom", "Brady", "tbrady@outlook.com");
        clientUser5.save();
        Client client5 = new Client(55, "today", clientUser5, "9789+Elizabeth+St,Martinsville,VA,24112", 0);
        client5.CascadeSave();

        User clientUser6 = new User(56, "today", "Justin", "Timberlake", "jtimberlake@yahoo.com");
        clientUser6.save();
        Client client6 = new Client(56, "today", clientUser6, "9008+Briarwood+Court,Waukegan,IL,60085", 0);
        client6.CascadeSave();

        User clientUser7 = new User(57, "today", "Lawrence", "Taylor", "ltaylor@outlook.com");
        clientUser7.save();
        Client client7 = new Client(57, "today", clientUser7, "842+Cypress+Court,Fishers,IN,46037", 0);
        client7.CascadeSave();

        User clientUser8 = new User(58, "today", "Jonas", "Hill", "jhill@aol.com");
        clientUser8.save();
        Client client8 = new Client(58, "today", clientUser8, "98+Jones+St,Xenia,OH,45385", 0);
        client8.CascadeSave();

        // create 4 inspections (for client 1)
        ArrayList<Inspection> inspections = new ArrayList<>(4);
        Inspection inspection;
        for (int i=0; i<4; i++){
            String creationDate;
            switch(i){
                case 0:
                    creationDate = "2/24/2016";
                    break;
                case 1:
                    creationDate = "1/15/2016";
                    break;
                case 2:
                    creationDate = "12/25/2015";
                    break;
                case 3:
                    creationDate = "12/5/2015";
                    break;
                default:
                    creationDate = "1/1/2016";
            }
            inspection = new Inspection(i+1, creationDate, droneOperator, client1, 0);
            inspection.CascadeSave();
            inspections.add(inspection);
        }

        // create 20 inspection images per inspection (5 of each type) (for client 1)
        String inspectionNumber[] = {"1", "2", "3", "4"};               // iterator
        String imageTypes[] = {"aerial", "thermal", "icedam", "salt"};  // iterator
        String imageNumber[] = {"1", "2", "3", "4", "5"};               // iterator
        InspectionImage inspectionImage;
        int i = 0;
        for (String iNum : inspectionNumber){
            for (String type : imageTypes){
                for (String num : imageNumber){

                    String url = Params.CLOUD_URL + iNum+"/images/"+type+num+".jpg";
                    inspectionImage = new InspectionImage("today", "today", inspections.get(i), type, url, 0);
                    inspectionImage.CascadeSave();
                }
            }
            i++;
        }
    }
}
