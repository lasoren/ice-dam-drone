package com.example.tberroa.girodicerapp;

public class Mission {
    public int num_of_aerials;
    public int num_of_thermals;
    public int num_of_iceDams;
    public int num_of_salts;
    public String date;

    public Mission(){
        num_of_aerials = 0;
        num_of_iceDams = 0;
        num_of_thermals = 0;
        num_of_salts = 0;
        date = "10/10/1990";
    }
    public void setNumOfAerials(int num){
        num_of_aerials = num;
    }
}
