package com.example.tberroa.girodicerapp.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;

@SuppressWarnings({"WeakerAccess", "unused", "MismatchedQueryAndUpdateOfCollection"})
@Table(name = "Inspection")
public class Inspection extends Model {

    /* The service provided by an operator to a client. Involves
    drone inspecting exterior of roof for ice dams, checking roof
    of home for hotspots using thermal camera, and placing salt on
    affected areas to alleviate the building pressure of the ice
    dam. */

    @Expose
    @Column(name = "inspection_id")
    public int id;

    @Expose
    @Column(name = "created")
    public String created;

    @Expose
    @Column(name = "client")
    public Client client;

    @Expose
    @Column(name = "deleted")
    public long deleted;

    public Inspection(){
        super();
    }

    public Inspection(int id, String created, Client client, long deleted){
        super();
        this.id = id;
        this.created = created;
        this.client = client;
        this.deleted = deleted;
    }

    public void cascadeSave() {
        this.client.save();
        this.save();
    }
}
