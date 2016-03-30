package com.example.tberroa.girodicerapp.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;

@SuppressWarnings({"WeakerAccess", "unused", "MismatchedQueryAndUpdateOfCollection"})
@Table(name = "IceDam")
public class IceDam extends Model {

    /* An ice dam that was identified by the drone during inspection
    and then confirmed by the operator. */

    @Expose
    @Column(name = "created")
    public long created;

    @Expose
    @Column(name = "deleted")
    public long deleted;

    @Expose
    @Column(name = "treated")
    public long treated;

    @Expose
    @Column(name = "inspection_image")
    public InspectionImage inspection_image;

    public IceDam(){
        super();
    }

    public IceDam(long created, long deleted, long treated, InspectionImage inspection_image){
        super();
        this.created = created;
        this.deleted = deleted;
        this.treated = treated;
        this.inspection_image = inspection_image;
    }

    public void cascadeSave() {
        this.inspection_image.save();
        this.save();
    }
}
