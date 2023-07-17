package com.rosario.naviversity;

import java.io.Serializable;

public class User implements Serializable {
    private String name;
    private String surname;
    private String phone;
    private boolean isCarOwner = false;
    private Car car = null;

    public User(String name, String surname, String phone){
        this.name = name;
        this.surname = surname;
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

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
    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }
}
