package com.rosario.naviversity;

import java.io.Serializable;

public class User implements Serializable {
    private String name;
    private String surname;

    private String username;

    public User(){}

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
