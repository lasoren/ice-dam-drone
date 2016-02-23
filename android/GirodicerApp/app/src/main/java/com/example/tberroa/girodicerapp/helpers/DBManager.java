package com.example.tberroa.girodicerapp.helpers;


import com.activeandroid.query.Select;
import com.example.tberroa.girodicerapp.models.User;

public class DBManager {

    public static String getOperatorName(int id) {
        User operator =  new Select()
                .from(User.class)
                .where("id = ?", id)
                .executeSingle();
        return operator.first_name;
    }



}
