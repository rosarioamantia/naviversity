package com.rosario.naviversity;

import java.io.Serializable;

public class User implements Serializable {
    private String name;
    private String surname;
    private boolean isCarOwner;
    private String carModel;
    private String carPlate;
    private String carColor;

    public User(){}
    //public User(String name, String surname, String phoneNumber)
    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return this.name;
    }
    public String getSurname() {
        return surname;
    }
    public void setSurname(String surname) {
        this.surname = surname;
    }

    public boolean isCarOwner() {
        return isCarOwner;
    }

    public void setCarOwner(boolean carOwner) {
        isCarOwner = carOwner;
    }

    public String getCarModel() {
        return carModel;
    }

    public void setCarModel(String carModel) {
        this.carModel = carModel;
    }

    public String getCarPlate() {
        return carPlate;
    }

    public void setCarPlate(String carPlate) {
        this.carPlate = carPlate;
    }

    public String getCarColor() {
        return carColor;
    }

    public void setCarColor(String carColor) {
        this.carColor = carColor;
    }
}
