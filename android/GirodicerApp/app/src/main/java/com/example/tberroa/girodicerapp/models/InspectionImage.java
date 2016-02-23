package com.example.tberroa.girodicerapp.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;

@Table(name = "InspectionImage")
public class InspectionImage extends Model {

    /* SQL row, that represents an image collected by the drone
    and now stored on AWS bucket storage backend. */

    @Expose
    @Column(name = "created")
    public long created;

    @Expose
    @Column(name = "taken")
    public long taken;

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

    public InspectionImage(long created, long taken, Inspection inspection, String image_type, String link, long deleted){
        super();
        this.created = created;
        this.taken = taken;
        this.inspection = inspection;
        this.image_type = image_type;
        this.link = link;
        this.deleted = deleted;
    }

    public void CascadeSave() {
        this.inspection.save();
        this.save();
    }
}
