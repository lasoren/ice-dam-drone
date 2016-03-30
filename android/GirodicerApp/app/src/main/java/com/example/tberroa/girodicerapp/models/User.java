package com.example.tberroa.girodicerapp.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;

@SuppressWarnings({"WeakerAccess", "unused", "MismatchedQueryAndUpdateOfCollection"})
@Table(name = "User")
public class User extends Model {

    // Generic user object storing data for operators, clients, or other future models.

    @Expose
    @Column(name = "user_id") // changed to avoid duplicate id scenario
    public int id;

    @Expose
    @Column(name = "created")
    public String created;

    @Expose
    @Column(name = "first_name")
    public String first_name;

    @Expose
    @Column(name = "last_name")
    public String last_name;

    @Expose
    @Column(name = "email")
    public String email;

    public User(){
        super();
    }

    public User(int id, String created, String first_name, String last_name, String email){
        super();
        this.id = id;
        this.created = created;
        this.first_name = first_name;
        this.last_name = last_name;
        this.email = email;
    }
}
