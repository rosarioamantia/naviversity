package com.rosario.naviversity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class Ride implements Serializable {
    Place start;
    Place stop;
    String owner;
    String members;
    String date;
    String time;

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

    public String getMembers() {
        return members;
    }

    public void setMembers(String members) {
        this.members = members;
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
}
