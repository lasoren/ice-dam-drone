package com.example.tberroa.girodicerapp.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "Inspection")
public class Inspection extends Model {

    /* The service provided by an operator to a client. Involves
    drone inspecting exterior of roof for ice dams, checking roof
    of home for hotspots using thermal camera, and placing salt on
    affected areas to alleviate the building pressure of the ice
    dam. */

    @Column(name = "created")
    public long created;

    @Column(name = "drone_operator")
    public DroneOperator drone_operator;

    @Column(name = "client")
    public Client client;

    @Column(name = "deleted")
    public long deleted;

    public Inspection(){
        super();
    }

    public Inspection(long created, DroneOperator drone_operator, Client client, long deleted){
        super();
        this.created = created;
        this.drone_operator = drone_operator;
        this.client = client;
        this.deleted = deleted;
    }
}
