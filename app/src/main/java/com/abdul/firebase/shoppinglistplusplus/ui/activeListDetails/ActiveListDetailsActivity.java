package com.abdul.firebase.shoppinglistplusplus.ui.activeListDetails;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.abdul.firebase.shoppinglistplusplus.R;
import com.abdul.firebase.shoppinglistplusplus.model.Item;
import com.abdul.firebase.shoppinglistplusplus.model.ShoppingList;
import com.abdul.firebase.shoppinglistplusplus.ui.BaseActivity;
import com.abdul.firebase.shoppinglistplusplus.utils.Constants;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ActiveListDetailsActivity extends BaseActivity {
    private static String LOG_TAG = ActiveListDetailsActivity.class.getSimpleName();
    private String list_pushID;
    private ListView mListItemsView;
    private List<Item> mItems;
    private List<String> mItemsIds;
    private ShoppingList mShoppingList;
    private ActiveListItemsAdapter mItemsListAdapter;
    // Firebase instance variables
    private DatabaseReference mShoppingListDatabaseReference;
    private ChildEventListener mChildEventListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_list_details);
        //getting the list_pushID;
        Intent intent = getIntent();
        list_pushID = intent.getStringExtra(Constants.KEY_LIST_PUSH_ID);
        mItems = new ArrayList<>();
        mItemsIds = new ArrayList<>();
        initializeScreen();

        //Setting up references to items database, adapters and listview
        FirebaseDatabase rootRef = FirebaseDatabase.getInstance();
        mShoppingListDatabaseReference = rootRef.getReference()
                .child(Constants.FIREBASE_LOCATION_SHOPPING_LIST)
                .child(list_pushID);
        mItemsListAdapter = new ActiveListItemsAdapter(this,
                R.layout.fragment_shopping_lists, mItems, list_pushID, ActiveListDetailsActivity.this);

        mListItemsView.setAdapter(mItemsListAdapter);
        mListItemsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(LOG_TAG, "onItemClick");
                TextView itemView = (TextView) view.findViewById(R.id.text_view_active_list_item_name);
                showEditListItemNameDialog(mItemsIds.get(position), itemView.getText().toString());
            }
        });
        /* Calling invalidateOptionsMenu causes onCreateOptionsMenu to be called */
        invalidateOptionsMenu();
        DatabaseReference mListTitleRef = rootRef.getReference()
                .child(Constants.FIREBASE_LOCATION_ACTIVE_LIST)
                .child(list_pushID);
        //Setting listener for changes in title
        mListTitleRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ShoppingList sl = dataSnapshot.getValue(ShoppingList.class);
                if (sl != null) {
                    mShoppingList = sl;
                    Log.d(LOG_TAG, "listName: " + sl.getListName());
                    setTitle(sl.getListName());
                }
                else {
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(LOG_TAG, getString(R.string.log_error_the_read_failed) +
                        databaseError.getMessage());
            }
        });

        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(LOG_TAG, "onChildAdded");
                int index = 0;
                if (previousChildName != null) {
                    index = mItemsIds.indexOf(previousChildName) + 1;
                }
                mItemsIds.add(index, dataSnapshot.getKey());
                mItems.add(index, dataSnapshot.getValue(Item.class));
                mItemsListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(LOG_TAG, "onChildChanged");
                int index = 0;
                if (previousChildName != null) {
                    index = mItemsIds.indexOf(previousChildName) + 1;
                }
                mItems.set(index,dataSnapshot.getValue(Item.class));
                mItemsListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(LOG_TAG, "onChildRemoved");
                int index = mItemsIds.indexOf(dataSnapshot.getKey());
                mItemsIds.remove(index);
                mItems.remove(index);
                mItemsListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(LOG_TAG, "onChildMoved");
                int oldIndex = mItemsIds.indexOf(dataSnapshot.getKey());
                mItemsIds.remove(oldIndex);
                mItems.remove(oldIndex);
                mItemsIds.add(dataSnapshot.getKey());
                mItems.add(dataSnapshot.getValue(Item.class));
                mItemsListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(LOG_TAG, "onCancelled");
            }
        };
        mShoppingListDatabaseReference.addChildEventListener(mChildEventListener);
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
        String shoppingListOwner = mShoppingList.getOwner();
        String loggedInUser;
        SharedPreferences sp = getSharedPreferences(getPackageName(),Context.MODE_PRIVATE);
        loggedInUser = sp.getString(getString(R.string.pref_email),"");
        if (shoppingListOwner.equals(loggedInUser)) {
            remove.setVisible(true);
            edit.setVisible(true);
        }
        else {
            remove.setVisible(false);
            edit.setVisible(false);
        }
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        //mItemsListAdapter.cleanup();
        //mActiveListRef.removeEventListener(mActiveListRefListener);
    }

    private void initializeScreen() {
        mListItemsView = (ListView) findViewById(R.id.list_view_shopping_list_items);
        Toolbar toolbar = (Toolbar) findViewById(R.id.details_bar);
        setSupportActionBar(toolbar);
        /* Adding back button to the action bar*/
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        View footer = getLayoutInflater().inflate(R.layout.footer_empty,null);
        mListItemsView.addFooterView(footer);
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
        DialogFragment dialog = RemoveListDialogFragment.newInstance(mShoppingList, list_pushID);
        dialog.show(getFragmentManager(), "RemoveListDialogFragment");
    }

    /**
     * Show the add list item dialog when user taps "Add list item" fab
     */
    public void showAddListItemDialog(View view) {
        /* Create an instance of the dialog fragment and show it */
        Log.v(LOG_TAG, "AddListItem");
        DialogFragment dialog = AddListItemDialogFragment.newInstance(mShoppingList, list_pushID);
        dialog.show(getFragmentManager(), "AddListItemDialogFragment");
    }

    /**
     * Show edit list name dialog when user selects "Edit list name" menu item
     */
    public void showEditListNameDialog() {
        /* Create an instance of the dialog fragment and show it */
        Log.v(LOG_TAG, "EditListName");
        DialogFragment dialog = EditListNameDialogFragment.newInstance(mShoppingList, list_pushID);
        dialog.show(this.getFragmentManager(), "EditListNameDialogFragment");
    }

    /**
     * Show the edit list item name dialog after longClick on the particular item
     */
    public void showEditListItemNameDialog(String item_pushID, String oldListName) {
        /* Create an instance of the dialog fragment and show it */
        Log.v(LOG_TAG, "EditListItemName");
        DialogFragment dialog = EditListItemNameDialogFragment.newInstance(mShoppingList,list_pushID,item_pushID,oldListName);
        dialog.show(this.getFragmentManager(), "EditListItemNameDialogFragment");
    }

    /**
     * This method is called when user taps "Start/Stop shopping" button
     */
    public void toggleShopping(View view) {

    }

    public class ActiveListItemsAdapter extends ArrayAdapter<Item> {
        private final String LOG_TAG = ActiveListItemsAdapter.class.getSimpleName();
        private String list_pushID;
        private Activity mActivity;
        public ActiveListItemsAdapter(Context context, int resource, List<Item> objects, String list_pushID, Activity activity) {
            super(context,resource,objects);
            mActivity = activity;
            this.list_pushID = list_pushID;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.single_active_list_item,parent,false);
            }
            Item item = getItem(position);
            TextView itemName = (TextView) convertView.findViewById(R.id.text_view_active_list_item_name);
            final ImageView deleteItem = (ImageView) convertView.findViewById(R.id.button_remove_item);
            final String item_pushID = mItemsIds.get(position);
            deleteItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogFragment dialog = RemoveListItemDialogFragment.newInstance(list_pushID, item_pushID);
                    dialog.show(mActivity.getFragmentManager(), "RemoveListItemDialogFragment");
                }
            });
            itemName.setText(item.getItemName());
            return convertView;
        }
    }
}
