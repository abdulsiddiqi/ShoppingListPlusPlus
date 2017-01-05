package com.abdul.firebase.shoppinglistplusplus.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;

/**
 * Created by abdul on 12/28/2016.
 */
public class User {
    private String name;
    private String email;
    private HashMap<String,Object> timestampJoined;

    public User() {

    }
    public User(String name, String email) {
        this.name = name;
        this.email = email;
        HashMap<String, Object> dateJoined = new HashMap<>();
        dateJoined.put("timestamp", ServerValue.TIMESTAMP);
        this.timestampJoined = dateJoined;
    }
    public String getName() { return name;}
    public String getEmail() { return email;}
    public HashMap<String,Object> getTimestampJoined() { return timestampJoined;}

    public boolean equals(Object o) {
        if (!(o instanceof User)) {
            return false;
        }
        User other = (User) o;
        return email.equals(other.getEmail());
    }

    public int hashCode() {
        return email.hashCode();
    }

    @Exclude
    public long getTimeStampJoined() {return (long) timestampJoined.get("timestamp");}


}
