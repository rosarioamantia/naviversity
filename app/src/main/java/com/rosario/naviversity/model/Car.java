package com.rosario.naviversity.model;

import com.google.firebase.database.Exclude;

import java.io.Serializable;

public class Car implements Serializable {
    @Exclude
    String id;
    private String plate;
    private String model;
    private String color;

    public Car(){}

    public Car(String model, String plate, String color){
        this.plate = plate;
        this.model = model;
        this.color = color;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getPlate() {
        return plate;
    }
    public void setPlate(String plate) {
        this.plate = plate;
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
    @Exclude
    public String getCompleteName(){
        return this.model + " " + this.color + " (" + this.plate + ") ";
    }
}
