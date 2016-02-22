package com.example.tberroa.girodicerapp.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "Hotspot")
public class Hotspot extends Model {

    /* A thermal hotspot that could cause future or current ice dam
    problems identified by the drone during inspection and
    confirmed by the operator. */

    @Column(name = "created")
    public long created;

    @Column(name = "deleted")
    public long deleted;

    @Column(name = "inspection_image")
    public InspectionImage inspection_image;

    public Hotspot(){
        super();
    }

    public Hotspot(long created, long deleted, InspectionImage inspection_image){
        super();
        this.created = created;
        this.deleted = deleted;
        this.inspection_image = inspection_image;
    }
}
