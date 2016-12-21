package com.abdul.firebase.shoppinglistplusplus.model;

/**
 * Created by abdul on 12/20/2016.
 */
public class Item {
    private String itemName;
    private String owner;

    public Item() {

    }
    public Item(String itemName, String owner) {
        this.itemName = itemName;
        this.owner = owner;
    }
    public String getItemName() {return itemName;}

    public String getOwner() {return owner;}


}
