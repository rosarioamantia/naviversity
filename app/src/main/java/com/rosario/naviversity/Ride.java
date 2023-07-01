package com.rosario.naviversity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class Ride implements Serializable {
    Place start;
    Place stop;
    String owner;
    String date;
    String time;
    List<String> members;

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

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }
}
