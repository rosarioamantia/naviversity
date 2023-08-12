package com.rosario.naviversity;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class User implements Serializable {
    private String id;
    private String name;
    private String surname;
    private String phone;
    private boolean carOwner = false;
    private Car car = null;
    private String votedOwner = null;
    private int ratingReceived = 0;
    private int score = 0;

    public User(String name, String surname, String phone){
        this.name = name;
        this.surname = surname;
        this.phone = phone;
    }

    public User(){}

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
        return carOwner;
    }

    public void setCarOwner(boolean carOwner) {
        this.carOwner = carOwner;
    }
    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVotedOwner() {
        return votedOwner;
    }

    public void setVotedOwner(String votedOwner) {
        this.votedOwner = votedOwner;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getRatingReceived() {
        return ratingReceived;
    }

    public void setRatingReceived(int ratingReceived) {
        this.ratingReceived = ratingReceived;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("surname", surname);
        result.put("phone", phone);
        result.put("carOwner", carOwner);
        result.put("car", car);
        result.put("votedOwner", votedOwner);
        result.put("score", score);
        result.put("ratingReceived", ratingReceived);
        return result;
    }
}
