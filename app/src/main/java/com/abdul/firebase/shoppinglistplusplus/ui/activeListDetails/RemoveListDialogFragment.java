package com.abdul.firebase.shoppinglistplusplus.ui.activeListDetails;


import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.abdul.firebase.shoppinglistplusplus.R;
import com.abdul.firebase.shoppinglistplusplus.model.ShoppingList;
import com.abdul.firebase.shoppinglistplusplus.utils.Constants;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.HashMap;

/**
 * Lets the user remove active shopping list
 */
public class RemoveListDialogFragment extends DialogFragment {
    final static String LOG_TAG = RemoveListDialogFragment.class.getSimpleName();
    private String push_id;
    /**
     * Public static constructor that creates fragment and passes a bundle with data into it when adapter is created
     */
    public static RemoveListDialogFragment newInstance(ShoppingList shoppingList, String push_id) {
        RemoveListDialogFragment removeListDialogFragment = new RemoveListDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.KEY_LIST_PUSH_ID,push_id);
        removeListDialogFragment.setArguments(bundle);
        return removeListDialogFragment;
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
        Bundle bundle = getArguments();
        if (bundle != null) {
            push_id = bundle.getString(Constants.KEY_LIST_PUSH_ID);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomTheme_Dialog)
                .setTitle(getActivity().getResources().getString(R.string.action_remove_list))
                .setMessage(getString(R.string.dialog_message_are_you_sure_remove_list))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        removeList();
                        /* Dismiss the dialog */
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        /* Dismiss the dialog */
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert);

        return builder.create();
    }

    private void removeList() {
//        Firebase listRef = new Firebase(Constants.FIREBASE_URL_ACTIVE_LIST).child(push_id);
//        listRef.removeValue();
//        Firebase itemsRef = new Firebase(Constants.FIREBASE_URL_SHOPPING_LIST).child(push_id);
//        itemsRef.removeValue();
        Firebase ref = new Firebase(Constants.FIREBASE_URL);
        HashMap<String,Object> updatedShoppingList = new HashMap<>();
        updatedShoppingList.put(Constants.FIREBASE_LOCATION_ACTIVE_LIST + "/" + push_id,null);
        updatedShoppingList.put(Constants.FIREBASE_LOCATION_SHOPPING_LIST+ "/" + push_id,null);
        ref.updateChildren(updatedShoppingList, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    Log.e(LOG_TAG,"Error updating data: " + firebaseError.getMessage());
                }
            }
        });
        Log.d(LOG_TAG,"removeList");
    }

}
