package com.rosario.naviversity;

import java.io.Serializable;

public class User implements Serializable {
    private int _id;
    private String _name;

    public User(){
        this._id = 0;
        this._name = "";
    }

    public void setId(int id){
        this._id = id;
    }

    public int getId(){
        return this._id;
    }

    public void setName(String name){
        this._name = name;
    }

    public String getName(){
        return this._name;
    }
}
