package com.example.tberroa.girodicerapp.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;

@SuppressWarnings({"WeakerAccess", "unused", "MismatchedQueryAndUpdateOfCollection"})
@Table(name = "Client")
public class Client extends Model {

    /* A paying client of the Ice Dam Drone service. Client pays
    operator low cost to perform an inspection of his/her home.
    Client can review images with DroneOperator post-inspection or
    through online portal sent to their email. Clients don't need
    a password. They can access they're individual inspection
    using a unique, non-guessable path. */

    @Expose
    @Column(name = "client_id")
    public int id;

    @Expose
    @Column(name = "created")
    public String created;

    @Expose
    @Column(name = "user")
    public User user;

    @Expose
    @Column(name = "address")
    public String address;

    @Expose
    @Column(name = "deleted")
    public long deleted;

    public Client() {
        super();
    }

    public Client(int id, String created, User user, String address, long deleted) {
        this.id = id;
        this.created = created;
        this.user = user;
        this.address = address;
        this.deleted = deleted;
    }

    public Client(String firstName, String lastName, String email, String address) {
        User user = new User();
        user.first_name = firstName;
        user.last_name = lastName;
        user.email = email;
        this.user = user;
        this.address = address;
    }

    public Client(Client client) {
        this.id = client.id;
        this.created = client.created;
        this.user = client.user;
        this.address = client.address;
        this.deleted = client.deleted;
    }

    public void cascadeSave() {
        this.user.save();
        this.save();
    }
}