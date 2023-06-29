package com.rosario.naviversity;

import java.io.Serializable;

public class Destination implements Serializable {
    String name;
    double latitude;
    double longitude;

    public void setName(String name){
        this.name = name;
    }

    public void setLatitude(double latitude){
        this.latitude = latitude;
    }

    public void setLongitude(double longitude){
        this.longitude = longitude;
    }

    public String getName(){
        return name;
    }

    public double getLatitude(){
        return latitude;
    }

    public double getLongitude(){
        return longitude;
    }

    @Override
    public String toString(){
        return name;
    }
}
