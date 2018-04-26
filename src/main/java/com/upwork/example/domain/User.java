package com.upwork.example.domain;

import java.io.Serializable;

public class User implements Serializable{

    private String username;
    private String password;

 
    public User(){}

    public User(Integer id, String username, String password) {
        this.username = username;
        this.password = password;
    }

    public User(String username, String password, String email, String firstname, String lastname, String division) {
        this.username = username;
        this.password = password;
    }

   
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
