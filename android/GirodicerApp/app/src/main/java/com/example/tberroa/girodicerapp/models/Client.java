package com.example.tberroa.girodicerapp.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;

@Table(name = "Client")
public class Client extends Model {

    /* A paying client of the Ice Dam Drone service. Client pays
    operator low cost to perform an inspection of his/her home.
    Client can review images with DroneOperator post-inspection or
    through online portal sent to their email. Clients don't need
    a password. They can access they're individual inspection
    using a unique, non-guessable link. */

    @Expose
    @Column(name = "created")
    public long created;

    @Expose
    @Column(name = "user")
    public User user;

    @Expose
    @Column(name = "address")
    public String address;

    @Expose
    @Column(name = "deleted")
    public long deleted;

    public Client(){
        super();
    }

    public Client(long created, User user, String address, long deleted){
        this.created = created;
        this.user = user;
        this.address = address;
        this.deleted = deleted;
    }

    public void CascadeSave() {
        this.user.save();
        this.save();
    }
}