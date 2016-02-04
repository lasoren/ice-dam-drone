package com.example.tberroa.girodicerapp.data;

public class Mission {
    private int numberOfAerials;
    private int numberOfThermals;
    private int numberOfIceDams;
    private int numberOfSalts;

    public Mission(){
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
