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
    @Column(name = "created")
    public String created;

    @Expose
    @Column(name = "taken")
    public String taken;

    @Expose
    @Column(name = "inspection")
    public Inspection inspection;

    @Expose
    @Column(name = "image_type")
    public String image_type;

    @Expose
    @Column(name = "link")
    public String link;

    @Expose
    @Column(name = "deleted")
    public long deleted;

    public InspectionImage(){
        super();
    }

    public InspectionImage(String created, String taken, Inspection inspection, String image_type, String link, long deleted){
        super();
        this.created = created;
        this.taken = taken;
        this.inspection = inspection;
        this.image_type = image_type;
        this.link = link;
        this.deleted = deleted;
    }

    public void cascadeSave() {
        this.inspection.save();
        this.save();
    }
}
