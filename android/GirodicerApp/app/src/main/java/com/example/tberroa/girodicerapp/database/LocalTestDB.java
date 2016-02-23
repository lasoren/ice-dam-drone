package com.example.tberroa.girodicerapp.database;

import com.activeandroid.query.Select;
import com.example.tberroa.girodicerapp.models.Client;
import com.example.tberroa.girodicerapp.models.DroneOperator;
import com.example.tberroa.girodicerapp.models.Inspection;
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
                .where("client_id = ?", 1)
                .executeSingle();
    }

    public List<Client> getClients(){
        DroneOperator droneOperator = getOperator();
        return new Select()
                .from(Client.class)
                .where("drone_operator = ?", droneOperator)
                .orderBy("Name ASC")
                .execute();
    }
    public List<Inspection> getInspections(){
        DroneOperator droneOperator = getOperator();
        Client client = getClient();
        return new Select()
                .from(Inspection.class)
                .where("drone_operator = ?", droneOperator)
                .where("client = ?", client)
                .orderBy("Name ASC")
                .execute();
    }
}
