package com.abdul.firebase.shoppinglistplusplus.ui.activeListDetails;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.abdul.firebase.shoppinglistplusplus.R;
import com.abdul.firebase.shoppinglistplusplus.model.ShoppingList;
import com.abdul.firebase.shoppinglistplusplus.ui.BaseActivity;
import com.abdul.firebase.shoppinglistplusplus.utils.Constants;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class ActiveListDetailsActivity extends BaseActivity {
    private static String LOG_TAG = ActiveListDetailsActivity.class.getSimpleName();
    private String push_id;
    private Firebase mActiveListRef;
    private ListView mListView;
    private ShoppingList mShoppingList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_list_details);
        //getting the push_id;
        Intent intent = getIntent();
        push_id = intent.getStringExtra(Constants.KEY_PUSH_ID);
        mActiveListRef = new Firebase(Constants.FIREBASE_URL_ACTIVE_LIST).child(push_id);
        initializeScreen();

        /* Calling invalidateOptionsMenu causes onCreateOptionsMenu to be called */
        invalidateOptionsMenu();

        mActiveListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ShoppingList sl = dataSnapshot.getValue(ShoppingList.class);
                if (sl != null) {
                    mShoppingList = sl;
                    setTitle(sl.getListName());
                }
                else {
                    finish();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(LOG_TAG, getString(R.string.log_error_the_read_failed) +
                               firebaseError.getMessage());
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //Check if the view isn't an empty footer view
                if (view.getId() != R.id.list_view_footer_empty) {
                    showEditListItemNameDialog();
                }
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu; this adds items to the action bar if it is present. */
        getMenuInflater().inflate(R.menu.menu_list_details,menu);

        /**
         * Get menu items
         */
        MenuItem remove = menu.findItem(R.id.action_remove_list);
        MenuItem edit = menu.findItem(R.id.action_edit_list_name);
        MenuItem share = menu.findItem(R.id.action_share_list);
        MenuItem archive = menu.findItem(R.id.action_archive);

        /* Only the edit and remove options are implemented */
        remove.setVisible(true);
        edit.setVisible(true);
        share.setVisible(false);
        archive.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        /**
         * Show edit list dialog when the edit action is selected
         */
        if (id == R.id.action_edit_list_name) {
            showEditListNameDialog();
            return true;
        }

        /**
         * removeList() when the remove action is selected
         */
        if (id == R.id.action_remove_list) {
            removeList();
            return true;
        }

        /**
         * Eventually we'll add this
         */
        if (id == R.id.action_share_list) {
            return true;
        }

        /**
         * archiveList() when the archive action is selected
         */
        if (id == R.id.action_archive) {
            archiveList();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initializeScreen() {
        mListView = (ListView) findViewById(R.id.list_view_shopping_list_items);
        Toolbar toolbar = (Toolbar) findViewById(R.id.details_bar);
        setSupportActionBar(toolbar);
        /* Adding back button to the action bar*/
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        View footer = getLayoutInflater().inflate(R.layout.footer_empty,null);
        mListView.addFooterView(footer);

    }
    /**
     * Archive current list when user selects "Archive" menu item
     */
    public void archiveList() {

    }


    /**
     * Start AddItemsFromMealActivity to add meal ingredients into the shopping list
     * when the user taps on "add meal" fab
     */
    public void addMeal(View view) {

    }

    /**
     * Remove current shopping list and its items from all nodes
     */
    public void removeList() {
        /* Create an instance of the dialog fragment and show it */
        Log.v(LOG_TAG, "removeList");
        DialogFragment dialog = RemoveListDialogFragment.newInstance(mShoppingList,push_id);
        dialog.show(getFragmentManager(), "RemoveListDialogFragment");
    }

    /**
     * Show the add list item dialog when user taps "Add list item" fab
     */
    public void showAddListItemDialog(View view) {
        /* Create an instance of the dialog fragment and show it */
        Log.v(LOG_TAG, "AddListItem");
        DialogFragment dialog = AddListItemDialogFragment.newInstance(mShoppingList);
        dialog.show(getFragmentManager(), "AddListItemDialogFragment");
    }

    /**
     * Show edit list name dialog when user selects "Edit list name" menu item
     */
    public void showEditListNameDialog() {
        /* Create an instance of the dialog fragment and show it */
        Log.v(LOG_TAG, "EditListName");
        DialogFragment dialog = EditListNameDialogFragment.newInstance(mShoppingList,push_id);
        dialog.show(this.getFragmentManager(), "EditListNameDialogFragment");
    }

    /**
     * Show the edit list item name dialog after longClick on the particular item
     */
    public void showEditListItemNameDialog() {
        /* Create an instance of the dialog fragment and show it */
        Log.v(LOG_TAG, "EditListItemName");
        DialogFragment dialog = EditListItemNameDialogFragment.newInstance(mShoppingList);
        dialog.show(this.getFragmentManager(), "EditListItemNameDialogFragment");
    }

    /**
     * This method is called when user taps "Start/Stop shopping" button
     */
    public void toggleShopping(View view) {

    }
}
