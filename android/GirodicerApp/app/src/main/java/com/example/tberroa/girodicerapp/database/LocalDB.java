package com.example.tberroa.girodicerapp.database;


import com.activeandroid.query.Select;
import com.example.tberroa.girodicerapp.models.Client;
import com.example.tberroa.girodicerapp.models.DroneOperator;
import com.example.tberroa.girodicerapp.models.Inspection;
import com.example.tberroa.girodicerapp.models.User;

import java.util.List;

public class LocalDB {

    public LocalDB(){
    }

    public DroneOperator getOperator(int id){
        return new Select()
                .from(DroneOperator.class)
                .where("operator_id = ?", id)
                .executeSingle();
    }

    public Client getClient(int id){
        return new Select()
                .from(Client.class)
                .where("client_id = ?", id)
                .executeSingle();
    }

    public List<Client> getClients(DroneOperator droneOperator){
        return new Select()
                .from(Client.class)
                .where("drone_operator = ?", droneOperator)
                .execute();
    }
    public List<Inspection> getInspections(DroneOperator droneOperator, Client client){
        return new Select()
                .from(Inspection.class)
                .where("drone_operator = ?", droneOperator)
                .where("client = ?", client)
                .execute();
    }
}
