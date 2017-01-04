package com.abdul.firebase.shoppinglistplusplus.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by abdul on 12/13/2016.
 */
public class ShoppingList {
    private String listName;
    private String owner;
    private HashMap<String,Object> timestampLastChanged;
    private HashMap<String,Object> timestampCreated;
    private ArrayList<String> shoppers = new ArrayList<>();
    public ShoppingList() {

    }

    public ShoppingList(String listName, String owner) {
        this.owner = owner;
        this.listName = listName;
        HashMap<String, Object> dateLastChangedObj = new HashMap<String, Object>();
        dateLastChangedObj.put("timestamp", ServerValue.TIMESTAMP);
        this.timestampLastChanged = dateLastChangedObj;
        this.timestampCreated = dateLastChangedObj;
    }

    public String getListName() {
        return listName;
    }

    public String getOwner() {
        return owner;
    }

    public HashMap<String, Object> getTimestampLastChanged() { return timestampLastChanged;}

    public HashMap<String,Object> getTimestampCreated() { return timestampCreated;}

    public ArrayList<String> getShoppers() {return shoppers;}

    @Exclude
    public long getTimeStampLastChanged() {
        return (long) timestampLastChanged.get("timestamp");
    }

    @Exclude
    public long getTimeStampCreated() {
        return (long) timestampCreated.get("timestamp");
    }

    @Exclude
    public void removeUserFromShoppersList(int pos) {
        shoppers.remove(pos);
    }

    @Exclude
    public void pushUserToShoppingList(String userId) {
        shoppers.add(userId);
    }
}
