package com.example.tberroa.girodicerapp.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "User")
public class User extends Model {

    // Generic user object storing data for operators, clients, or other future models.

    @Column(name = "id")
    public int id;

    @Column(name = "created")
    public long created;

    @Column(name = "first_name")
    public String first_name;

    @Column(name = "last_name")
    public String last_name;

    @Column(name = "email")
    public String email;

    public User(){
        super();
    }

    public User(int id, long created, String first_name, String last_name, String email){
        super();
        this.id = id;
        this.created = created;
        this.first_name = first_name;
        this.last_name = last_name;
        this.email = email;
    }
}
