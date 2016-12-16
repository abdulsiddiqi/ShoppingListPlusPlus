package com.abdul.firebase.shoppinglistplusplus.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.firebase.client.ServerValue;

import java.util.HashMap;

/**
 * Created by abdul on 12/13/2016.
 */
public class ShoppingList {
    private String listName;
    private String owner;
    private HashMap<String,Object> dateLastChanged;
    public ShoppingList() {
    }

    public ShoppingList(String listName, String owner) {
        this.owner = owner;
        this.listName = listName;
        HashMap<String, Object> dateLastChangedObj = new HashMap<String, Object>();
        dateLastChangedObj.put("date", ServerValue.TIMESTAMP);
        this.dateLastChanged = dateLastChangedObj;
    }

    public String getListName() {
        return listName;
    }

    public String getOwner() {
        return owner;
    }

    public HashMap<String, Object> getDateLastChanged() {
        return dateLastChanged;
    }

    @JsonIgnore
    public long getDateLastChangedLong() {
        return (long) dateLastChanged.get("date");
    }
}
