package com.example.mars.entities;

import com.google.firebase.firestore.Exclude;
import java.io.Serializable;

public class User implements Serializable {
    public String uid;
    public String name;
    @SuppressWarnings("WeakerAccess")
    public String email;
    @Exclude
    public boolean isAuthenticated;
    @Exclude
    public boolean isNew, isCreated;

    public User() {}

    public User(String uid, String name, String email) {
        this.uid = uid;
        this.name = name;
        this.email = email;
    }

    public String toString() {
        return "id: " + uid
            + " name: " + name
            + " email: " + email
            + " isAuthenticated: " + isAuthenticated
            + " isNew: " + isNew
            + " isCreated: " + isCreated;
    }
}
