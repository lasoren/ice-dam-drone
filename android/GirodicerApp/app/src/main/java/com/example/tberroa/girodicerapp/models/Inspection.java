package com.example.tberroa.girodicerapp.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;

@Table(name = "Inspection")
public class Inspection extends Model {

    /* The service provided by an operator to a client. Involves
    drone inspecting exterior of roof for ice dams, checking roof
    of home for hotspots using thermal camera, and placing salt on
    affected areas to alleviate the building pressure of the ice
    dam. */

    @Expose
    @Column(name = "created")
    public long created;

    @Expose
    @Column(name = "drone_operator")
    public DroneOperator drone_operator;

    @Expose
    @Column(name = "client")
    public Client client;

    @Expose
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

    public void CascadeSave() {
        this.drone_operator.save();
        this.client.save();
        this.save();
    }
}
