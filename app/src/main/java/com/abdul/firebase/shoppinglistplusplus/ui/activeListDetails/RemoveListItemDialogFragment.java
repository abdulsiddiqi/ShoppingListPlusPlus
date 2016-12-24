package com.abdul.firebase.shoppinglistplusplus.ui.activeListDetails;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.abdul.firebase.shoppinglistplusplus.R;
import com.abdul.firebase.shoppinglistplusplus.utils.Constants;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RemoveListItemDialogFragment extends DialogFragment {
    final static String LOG_TAG = RemoveListDialogFragment.class.getSimpleName();
    private String list_pushID, item_pushID;
    public static RemoveListItemDialogFragment newInstance(String list_pushID,String item_pushID) {
        RemoveListItemDialogFragment removeListItemDialogFragment = new RemoveListItemDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.KEY_LIST_PUSH_ID,list_pushID);
        bundle.putString(Constants.KEY_ITEM_PUSH_ID,item_pushID);
        removeListItemDialogFragment.setArguments(bundle);
        return removeListItemDialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            list_pushID = bundle.getString(Constants.KEY_LIST_PUSH_ID);
            item_pushID = bundle.getString(Constants.KEY_ITEM_PUSH_ID);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomTheme_Dialog)
                .setTitle(getActivity().getResources().getString(R.string.action_remove_item))
                .setMessage(getString(R.string.dialog_message_are_you_sure_remove_item))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        removeListItem();
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
    private void removeListItem() {
        DatabaseReference itemRef = FirebaseDatabase.getInstance().getReference()
                .child(Constants.FIREBASE_LOCATION_SHOPPING_LIST)
                .child(list_pushID)
                .child(item_pushID);
        itemRef.removeValue();
    }
}
