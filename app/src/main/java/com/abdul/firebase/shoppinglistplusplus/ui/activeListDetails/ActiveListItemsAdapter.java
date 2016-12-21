package com.abdul.firebase.shoppinglistplusplus.ui.activeListDetails;

import android.app.Activity;
import android.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.abdul.firebase.shoppinglistplusplus.R;
import com.abdul.firebase.shoppinglistplusplus.model.Item;
import com.firebase.client.Query;
import com.firebase.ui.FirebaseListAdapter;

/**
 * Created by abdul on 12/20/2016.
 */
public class ActiveListItemsAdapter extends FirebaseListAdapter<Item> {
    private static final String LOG_TAG = ActiveListItemsAdapter.class.getSimpleName();
    private String list_pushID;
    public ActiveListItemsAdapter(Activity activity, Class<Item> modelClass, int modelLayout,
                                  Query ref, String list_pushID) {
        super(activity, modelClass, modelLayout, ref);
        this.mActivity = activity;
        this.list_pushID = list_pushID;
    }

    @Override
    protected void populateView(View view, final Item list, int position) {
        super.populateView(view,list);
        Log.d(LOG_TAG, "populateView");
        TextView itemName = (TextView) view.findViewById(R.id.text_view_active_list_item_name);
        final ImageView deleteItem = (ImageView) view.findViewById(R.id.button_remove_item);
        //TextView boughtBy = (TextView) view.findViewById(R.id.text_view_bought_by);
        itemName.setText(list.getItemName());
        final String item_pushID = this.getRef(position).getKey();

        deleteItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialog = RemoveListItemDialogFragment.newInstance(list_pushID,item_pushID);
                dialog.show(mActivity.getFragmentManager(),"RemoveListItemDialogFragment");
            }
        });
    }

}
