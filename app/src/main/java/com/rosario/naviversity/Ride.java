package com.rosario.naviversity;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Ride implements Serializable {
    @Exclude
    String id;
    Place start;
    Place stop;
    String owner;
    String date;
    String time;
    HashMap<String, User> members;
    Car car;

    public Ride(Place start, Place stop, String owner, String date, String time, Car car, HashMap<String, User> members){
        this.start = start;
        this.stop = stop;
        this.owner = owner;
        this.date = date;
        this.time = time;
        this.car = car;
        this.members = members;
    }
    public Ride(){}

    public Place getStart() {
        return start;
    }

    public void setStart(Place start) {
        this.start = start;
    }

    public Place getStop() {
        return stop;
    }

    public void setStop(Place stop) {
        this.stop = stop;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public HashMap<String, User> getMembers() {
        return members;
    }

    public void setMembers(HashMap<String, User> members) {
        this.members = members;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("start", start);
        result.put("stop", stop);
        result.put("owner", owner);
        result.put("date", date);
        result.put("time", time);
        result.put("members", members);
        result.put("car", car);

        return result;
    }
}
