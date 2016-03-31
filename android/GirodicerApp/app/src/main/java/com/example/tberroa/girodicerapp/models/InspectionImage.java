package com.example.tberroa.girodicerapp.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;

@SuppressWarnings({"WeakerAccess", "unused", "MismatchedQueryAndUpdateOfCollection"})
@Table(name = "InspectionImage")
public class InspectionImage extends Model {

    /* SQL row, that represents an image collected by the drone
    and now stored on AWS bucket storage backend. */

    @Expose
    @Column(name = "image_id")
    public String id;

    @Expose
    @Column(name = "created")
    public String created;

    @Expose
    @Column(name = "taken")
    public String taken;

    @Expose
    @Column(name = "inspection_id")
    public int inspection_id;

    @Expose
    @Column(name = "image_type")
    public int image_type;

    @Expose
    @Column(name = "path")
    public String path;

    @Expose
    @Column(name = "deleted")
    public long deleted;

    @Expose
    @Column(name = "icedam")
    public IceDam iceDam;

    @Expose
    @Column(name = "hotspot")
    public Hotspot hotspot;

    public InspectionImage(){
        super();
    }

    public InspectionImage(String taken, int inspection_id, int image_type){
        super();
        this.taken = taken;
        this.inspection_id = inspection_id;
        this.image_type = image_type;
    }
}
