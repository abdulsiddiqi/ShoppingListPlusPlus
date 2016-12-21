package com.abdul.firebase.shoppinglistplusplus.ui.activeListDetails;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;

import com.abdul.firebase.shoppinglistplusplus.R;
import com.abdul.firebase.shoppinglistplusplus.model.ShoppingList;
import com.abdul.firebase.shoppinglistplusplus.utils.Constants;
import com.firebase.client.Firebase;

import java.util.HashMap;

/**
 * Lets user edit list item name for all copies of the current list
 */
public class EditListItemNameDialogFragment extends EditListDialogFragment {
    private String old_itemName;
    private String list_pushID, item_pushID;
    private static final String LOG_TAG = EditListItemNameDialogFragment.class.getSimpleName();
    /**
     * Public static constructor that creates fragment and passes a bundle with data into it when adapter is created
     */
    public static EditListItemNameDialogFragment newInstance(ShoppingList shoppingList,
                                                             String list_pushID, String item_pushID, String old_title) {
        EditListItemNameDialogFragment editListItemNameDialogFragment = new EditListItemNameDialogFragment();
        Bundle bundle = EditListDialogFragment.newInstanceHelper(shoppingList, R.layout.dialog_edit_item);
        bundle.putString(Constants.KEY_ITEM_PUSH_ID,item_pushID);
        bundle.putString(Constants.KEY_LIST_PUSH_ID,list_pushID);
        bundle.putString(Constants.KEY_ITEM_NAME,old_title);
        editListItemNameDialogFragment.setArguments(bundle);
        return editListItemNameDialogFragment;
    }

    /**
     * Initialize instance variables with data from bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        /** {@link EditListDialogFragment#createDialogHelper(int)} is a
         * superclass method that creates the dialog
         */
        Dialog dialog = super.createDialogHelper(R.string.positive_button_edit_item);
        Bundle bundle = getArguments();
        if (bundle != null) {
            list_pushID = bundle.getString(Constants.KEY_LIST_PUSH_ID);
            item_pushID = bundle.getString(Constants.KEY_ITEM_PUSH_ID);
            old_itemName = bundle.getString(Constants.KEY_ITEM_NAME);
            Log.d(LOG_TAG,"list_pushID:" + list_pushID);
            Log.d(LOG_TAG,"item_pushID:" + item_pushID);
            Log.d(LOG_TAG,"old_itemName:" + old_itemName);
            helpSetDefaultValueEditText(old_itemName);
        }
        return dialog;
    }

    /**
     * Change selected list item name to the editText input if it is not empty
     */
    protected void doListEdit() {
        String newItemName = mEditTextForList.getText().toString();
        Firebase rootRef = new Firebase(Constants.FIREBASE_URL_SHOPPING_LIST);
        Firebase listRef = rootRef.child(list_pushID);
        Firebase itemRef = listRef.child(item_pushID);
        if (newItemName.length() > 0 && !newItemName.equals(old_itemName)) {
            //hashmap containg all the updated data
            HashMap<String,Object> itemUpdatedProperties = new HashMap<>();
            itemUpdatedProperties.put("itemName", newItemName);
            itemUpdatedProperties.put("owner","Anonymous");
            itemRef.updateChildren(itemUpdatedProperties);
        }
    }
}
