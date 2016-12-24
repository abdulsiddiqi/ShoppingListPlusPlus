package com.abdul.firebase.shoppinglistplusplus.ui.activeListDetails;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;

import com.abdul.firebase.shoppinglistplusplus.R;
import com.abdul.firebase.shoppinglistplusplus.model.ShoppingList;
import com.abdul.firebase.shoppinglistplusplus.utils.Constants;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;


/**
 * Lets user edit the list name for all copies of the current list
 */
public class EditListNameDialogFragment extends EditListDialogFragment {
    private String old_title;
    private String push_id;
    private long timestampCreated;
    private static final String LOG_TAG = ActiveListDetailsActivity.class.getSimpleName();

    /**
     * Public static constructor that creates fragment and passes a bundle with data into it when adapter is created
     */
    public static EditListNameDialogFragment newInstance(ShoppingList shoppingList,String push_id) {
        EditListNameDialogFragment editListNameDialogFragment = new EditListNameDialogFragment();
        Bundle bundle = EditListDialogFragment.newInstanceHelper(shoppingList, R.layout.dialog_edit_list);
        bundle.putString(Constants.KEY_LIST_NAME,shoppingList.getListName());
        bundle.putString(Constants.KEY_LIST_PUSH_ID,push_id);
        bundle.putLong(Constants.KEY_TIMESTAMP_CREATED,shoppingList.getTimeStampCreated());
        editListNameDialogFragment.setArguments(bundle);
        Log.v(LOG_TAG, "newInstance");
        return editListNameDialogFragment;
    }

    /**
     * Initialize instance variables with data from bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(LOG_TAG,"OnCreate");
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        /** {@link EditListDialogFragment#createDialogHelper(int)} is a
         * superclass method that creates the dialog
         **/
        Dialog dialog = super.createDialogHelper(R.string.positive_button_edit_item);
        Bundle bundle = getArguments();
        if (bundle != null) {
            old_title = bundle.getString(Constants.KEY_LIST_NAME);
            push_id = bundle.getString(Constants.KEY_LIST_PUSH_ID);
            timestampCreated = bundle.getLong(Constants.KEY_TIMESTAMP_CREATED);
            helpSetDefaultValueEditText(old_title);
        }
        return dialog;
    }


    /**
     * Changes the list name in all copies of the current list
     */
    @Override
    protected void doListEdit() {

        String newListName = mEditTextForList.getText().toString();
        if (newListName.length() > 0 &&!newListName.equals(old_title)) {
            DatabaseReference listRef = FirebaseDatabase.getInstance().getReference()
                    .child(Constants.FIREBASE_LOCATION_ACTIVE_LIST)
                    .child(push_id);
            //hashmap containing all the updated data
            HashMap<String,Object> shoppingListUpdatedProperties = new HashMap<>();
            shoppingListUpdatedProperties.put("listName", newListName);
            shoppingListUpdatedProperties.put("owner", "Anonymous");
            HashMap<String, Object> dateLastChangedObj = new HashMap<>();
            dateLastChangedObj.put("timestamp", ServerValue.TIMESTAMP);
            HashMap<String,Object> dateCreatedObj = new HashMap<>();
            dateCreatedObj.put("timestamp",timestampCreated);
            shoppingListUpdatedProperties.put("timestampLastChanged",dateLastChangedObj);
            shoppingListUpdatedProperties.put("timestampCreated", dateCreatedObj);
            listRef.updateChildren(shoppingListUpdatedProperties);
        }
    }
}

