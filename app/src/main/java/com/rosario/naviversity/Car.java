package com.rosario.naviversity;

import com.google.firebase.database.Exclude;

public class Car {
    @Exclude
    String id;
    private String manufacturer;
    private String model;
    private String color;

    public Car(){}

    public Car(String manufacturer, String model, String color){
        this.manufacturer = manufacturer;
        this.model = model;
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
