package com.example.tberroa.girodicerapp.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;

@SuppressWarnings({"WeakerAccess", "unused", "MismatchedQueryAndUpdateOfCollection"})
@Table(name = "DroneOperator")
public class DroneOperator extends Model {

    /* Operator of the Ice Dam Drone (Girodicer). Potentially the
    owner of an ice removal company or an employee of a
    municipality. Visits afflicted homes to offer inspection and
    ice dam removal using the drone. Each inspection would be very
    low cost. */

    @Expose
    @Column(name = "operator_id") // changed to avoid duplicate id scenario
    public int id;

    @Expose
    @Column(name = "created")
    public String created;

    @Expose
    @Column(name = "user")
    public User user;

    @Expose
    @Column(name = "session_id")
    public String session_id;

    public DroneOperator(){
        super();
    }

    public DroneOperator(int id, String created, User user, String session_id){
        super();
        this.id = id;
        this.created = created;
        this.user = user;
        this.session_id = session_id;
    }

    public void cascadeSave() {
        this.user.save();
        this.save();
    }
}
