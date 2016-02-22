package com.example.tberroa.girodicerapp.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "DroneOperator")
public class DroneOperator extends Model {

    /* Operator of the Ice Dam Drone (Girodicer). Potentially the
    owner of an ice removal company or an employee of a
    municipality. Visits afflicted homes to offer inspection and
    ice dam removal using the drone. Each inspection would be very
    low cost. */

    @Column(name = "created")
    public long created;

    @Column(name = "user")
    public User user;

    @Column(name = "session_id")
    public String session_id;


    public DroneOperator(){
        super();
    }

    public DroneOperator(long created, User user, String session_id){
        super();
        this.created = created;
        this.user = user;
        this.session_id = session_id;
    }
}
