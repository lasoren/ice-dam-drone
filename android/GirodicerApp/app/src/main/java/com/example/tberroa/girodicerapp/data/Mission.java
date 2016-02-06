package com.example.tberroa.girodicerapp.data;

import android.os.Bundle;

public class Mission {
    private int numberOfAerials;
    private int numberOfThermals;
    private int numberOfIceDams;
    private int numberOfSalts;

    public Mission(){
    }

    public Mission(Bundle bundle){
        numberOfAerials = bundle.getInt("number_of_aerials", 0);
        numberOfThermals = bundle.getInt("number_of_thermals", 0);
        numberOfIceDams = bundle.getInt("number_of_icedams", 0);
        numberOfSalts = bundle.getInt("number_of_salts", 0);
    }

    public void setNumberOfAerials(int num){
        numberOfAerials = num;
    }

    public void setNumberOfThermals(int num){
        numberOfThermals = num;
    }

    public void setNumberOfIceDams(int num){
        numberOfIceDams = num;
    }

    public void setNumberOfSalts(int num){
        numberOfSalts = num;
    }

    public int getNumberOfAerials(){
        return numberOfAerials;
    }

    public int getNumberOfThermals(){
        return numberOfThermals;
    }

    public int getNumberOfIceDams(){
        return numberOfIceDams;
    }

    public int getNumberOfSalts(){
        return numberOfSalts;
    }
}
