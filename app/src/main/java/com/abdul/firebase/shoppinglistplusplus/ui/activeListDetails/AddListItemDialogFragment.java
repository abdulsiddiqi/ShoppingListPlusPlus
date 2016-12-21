package com.abdul.firebase.shoppinglistplusplus.ui.activeListDetails;


import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;

import com.abdul.firebase.shoppinglistplusplus.R;
import com.abdul.firebase.shoppinglistplusplus.model.Item;
import com.abdul.firebase.shoppinglistplusplus.model.ShoppingList;
import com.abdul.firebase.shoppinglistplusplus.utils.Constants;
import com.firebase.client.Firebase;

/**
 *
 */
public class AddListItemDialogFragment extends EditListDialogFragment {

    private static final String LOG_TAG = AddListItemDialogFragment.class.getSimpleName();
    private String push_id;
    /**
     * Public static constructor that creates fragment and passes a bundle with data into it when adapter is created
     */
    public static AddListItemDialogFragment newInstance(ShoppingList shoppingList, String push_id) {
        AddListItemDialogFragment addListItemDialogFragment = new AddListItemDialogFragment();

        Bundle bundle = newInstanceHelper(shoppingList, R.layout.dialog_edit_item);
        bundle.putString(Constants.KEY_LIST_PUSH_ID,push_id);
        addListItemDialogFragment.setArguments(bundle);

        return addListItemDialogFragment;
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
         **/
        Dialog dialog = super.createDialogHelper(R.string.positive_button_add_list_item);
        Bundle bundle = getArguments();
        push_id = bundle.getString(Constants.KEY_LIST_PUSH_ID);
        return dialog;
    }

    /**
     * Adds new item to the current shopping list
     */
    @Override
    protected void doListEdit() {
        Log.d(LOG_TAG, "doListEdit");
        String itemName = mEditTextForList.getText().toString();
        if (itemName.length() > 0) {
            Firebase shoppingRoofRef = new Firebase(Constants.FIREBASE_URL_SHOPPING_LIST).child(push_id);
            Firebase itemRef = shoppingRoofRef.push();
            Item item = new Item(itemName,"Anonymous Owner");
            itemRef.setValue(item);
        }
    }

}
