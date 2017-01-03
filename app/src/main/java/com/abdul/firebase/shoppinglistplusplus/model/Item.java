package com.abdul.firebase.shoppinglistplusplus.model;

import com.google.firebase.database.Exclude;

/**
 * Created by abdul on 12/20/2016.
 */
public class Item {
    private String itemName;
    private String owner;
    private String boughtBy;
    private Boolean hasBought;

    public Item() {

    }
    public Item(String itemName, String owner) {
        this.itemName = itemName;
        this.owner = owner;
        this.hasBought = false;
        this.boughtBy = "";
    }

    public String getItemName() {return itemName;}

    public String getOwner() {return owner;}

    public Boolean getHasBought() {return hasBought;}

    public String getBoughtBy() {return boughtBy;}

    @Exclude
    public void setBoughtBy(String boughtBy) {this.boughtBy = boughtBy; }

    @Exclude
    public void setHasBought(Boolean b) { hasBought = b;}
}
