package com.abdul.firebase.shoppinglistplusplus.ui.activeListDetails;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.abdul.firebase.shoppinglistplusplus.R;
import com.abdul.firebase.shoppinglistplusplus.model.Item;
import com.abdul.firebase.shoppinglistplusplus.model.ShoppingList;
import com.abdul.firebase.shoppinglistplusplus.model.User;
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
    // Variables for items
    private ListView mListItemsView;
    private List<Item> mItems;
    private List<String> mItemsIds;
    private ActiveListItemsAdapter mItemsListAdapter;
    // Reference to shoppinglist whose items are being displayed
    private ShoppingList mShoppingList;
    // Firebase instance variables
    private DatabaseReference mShoppingListDatabaseReference;
    private DatabaseReference mListTitleRef;
    DatabaseReference mUserRef;
    //User specific variables
    private boolean isShopping = false;
    private User mUser;
    //Listeners
    private ChildEventListener mChildEventListener;
    private ValueEventListener mUserEventListener;

    private Button btn_toggle_shopping;
    private TextView txt_view_shoppers;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreate");
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
        //determine if user is shopping currently or not in this shoppinglist
        Log.v(LOG_TAG, "isShopping: " + Boolean.toString(isShopping));
        mItemsListAdapter = new ActiveListItemsAdapter(this,
                R.layout.fragment_shopping_lists, mItems, list_pushID, ActiveListDetailsActivity.this);

        mListItemsView.setAdapter(mItemsListAdapter);
        mListItemsView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                TextView itemView = (TextView) view.findViewById(R.id.text_view_active_list_item_name);
                showEditListItemNameDialog(mItemsIds.get(position), itemView.getText().toString());
                return true;
            }
        });
        //getting username and storing in sharedpreferences
        SharedPreferences sp = getSharedPreferences(getPackageName(),Context.MODE_PRIVATE);
        String encoded_email = sp.getString(getString(R.string.pref_firebase_key),null);
        mUserRef = FirebaseDatabase.getInstance().getReference()
                .child(Constants.FIREBASE_LOCATION_USERS)
                .child(encoded_email);
        mUserEventListener = mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mUser = dataSnapshot.getValue(User.class);
                }
                else {
                    Log.e(LOG_TAG, "username doesn't exist");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(LOG_TAG, "OnCancelled: " + databaseError.getMessage());
            }
        });

        mListItemsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(LOG_TAG, "onItemClick");
                String username = mUser.getName();
                if (!isShopping)
                    return;
                String item_pushId = mItemsIds.get(position);
                DatabaseReference itemRef = FirebaseDatabase.getInstance().getReference()
                        .child(Constants.FIREBASE_LOCATION_SHOPPING_LIST)
                        .child(list_pushID)
                        .child(item_pushId);
                Item item = mItems.get(position);
                String boughtBy = item.getBoughtBy();
                //nobody has bought the item
                if (boughtBy.length() == 0) {
                    item.setBoughtBy(username);
                    item.setHasBought(true);
                }
                //only YOU can unbuy an item if it has been bought
                else if (item.getHasBought() && boughtBy.equals(username)){
                    item.setBoughtBy("");
                    item.setHasBought(false);
                }
                //no need to notify and manipulate arraylist if no modification
                else {
                    return;
                }
                itemRef.setValue(item);
            }
        });

        /* Calling invalidateOptionsMenu causes onCreateOptionsMenu to be called */
        invalidateOptionsMenu();
        mListTitleRef = rootRef.getReference()
                .child(Constants.FIREBASE_LOCATION_ACTIVE_LIST)
                .child(list_pushID);
        //Setting listener for changes in title
        mListTitleRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ShoppingList sl = dataSnapshot.getValue(ShoppingList.class);
                if (sl != null) {
                    mShoppingList = sl;
                    setTitle(sl.getListName());
                    ArrayList<User> shoppers = mShoppingList.getShoppers();
                    displayShoppers(shoppers);
                    //if current user not found, then start of shopping
                    Log.v(LOG_TAG, "mUser email: " + mUser.getEmail());
                    if (shoppers != null && shoppers.indexOf(mUser) != -1) {
                        Log.v(LOG_TAG, "user is shopping");
                        isShopping = true;
                        btn_toggle_shopping.setBackgroundColor(
                                ContextCompat.getColor(ActiveListDetailsActivity.this,R.color.dark_grey));
                        btn_toggle_shopping.setText(getString(R.string.button_stop_shopping));
                    }
                    else {
                        Log.v(LOG_TAG, "user is not shopping");
                        isShopping = false;
                        btn_toggle_shopping.setBackgroundColor(
                                ContextCompat.getColor(ActiveListDetailsActivity.this,R.color.primary_dark));
                        btn_toggle_shopping.setText(getString(R.string.button_start_shopping));
                    }
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

        //Setting up child listener for items in shoppinglist
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
        loggedInUser = sp.getString(getString(R.string.pref_firebase_key),"");
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
        mShoppingListDatabaseReference.removeEventListener(mChildEventListener);

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
        btn_toggle_shopping = (Button) findViewById(R.id.button_shopping);
        txt_view_shoppers = (TextView) findViewById(R.id.text_view_people_shopping);
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
        ArrayList<User> shoppers = mShoppingList.getShoppers();
        if (isShopping) {
            int index = shoppers.indexOf(mUser);
            if (index == -1) {
                Log.e(LOG_TAG, "ERROR finding user in shoppinglist shoppers list");
            }
            else {
                mShoppingList.removeUserFromShoppersList(index);
                mListTitleRef.setValue(mShoppingList);
                Log.v(LOG_TAG, "Successfully removed user from shoppers list");
            }
        }
        else {
            Log.v(LOG_TAG, "Shopping started");
            mShoppingList.pushUserToShoppingList(mUser);
            mListTitleRef.setValue(mShoppingList);
        }
    }

    private void displayShoppers(ArrayList<User> shoppers) {
        if (shoppers.size() < 1) {
            txt_view_shoppers.setText(getString(R.string.text_nobody_shopping));
            return;
        }
        int index = shoppers.indexOf(mUser);
        if (index != -1) {
            txt_view_shoppers.setText(getString(R.string.text_you_are_shopping));
        }
        else {
            txt_view_shoppers.setText(getString(R.string.text_other_is_shopping,shoppers.get(0).getName()));
        }
        if (shoppers.size() > 1) {
            if (shoppers.size() == 2) {
                //checking if current user shopping
                if (index != -1) {
                    index = (index + 1) % 2;
                    String otherShopper = shoppers.get(index).getName();
                    txt_view_shoppers.setText(getString(R.string.text_you_and_other_are_shopping,otherShopper));
                }
                else {
                    txt_view_shoppers.setText(getString(R.string.text_other_and_other_are_shopping,
                            shoppers.get(0).getName(),
                            shoppers.get(1).getName()));
                }
            }
            //more than 2 shoppers
            else {
                //checking if current user shopping
                if (index != -1) {
                    txt_view_shoppers.setText(getString(R.string.text_you_and_other_are_shopping, shoppers.size() - 1));
                }
                else {
                    txt_view_shoppers.setText(getString(R.string.text_other_and_other_are_shopping,
                            shoppers.get(0).getName(),
                            shoppers.size() - 1));
                }
            }
        }

    }

    private void changeItem(String boughtBy) {

    }


    public class ActiveListItemsAdapter extends ArrayAdapter<Item> {
        private final String LOG_TAG = ActiveListItemsAdapter.class.getSimpleName();
        private String list_pushID;
        private Activity mActivity;
        private SharedPreferences mSp;
        public ActiveListItemsAdapter(Context context, int resource, List<Item> objects, String list_pushID, Activity activity) {
            super(context,resource,objects);
            mActivity = activity;
            this.list_pushID = list_pushID;
            mSp = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.single_active_list_item,parent,false);
            }
            Item item = getItem(position);
            TextView itemName = (TextView) convertView.findViewById(R.id.text_view_active_list_item_name);
            TextView boughtByLabel = (TextView) convertView.findViewById(R.id.text_view_bought_by);
            TextView boughtByUser = (TextView) convertView.findViewById(R.id.text_view_bought_by_user);
            final ImageView deleteItem = (ImageView) convertView.findViewById(R.id.button_remove_item);

            String loggedInUser = mSp.getString(getString(R.string.pref_firebase_key),"");
            final String item_pushID = mItemsIds.get(position);
            //Initially only delete button is shown
            if (mShoppingList.getOwner().equals(loggedInUser) && item.getOwner().equals(loggedInUser)) {
                deleteItem.setVisibility(View.VISIBLE);
            }
            else {
                deleteItem.setVisibility(View.INVISIBLE);
            }
            boughtByLabel.setVisibility(View.INVISIBLE);
            boughtByUser.setVisibility(View.INVISIBLE);
            deleteItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogFragment dialog = RemoveListItemDialogFragment.newInstance(list_pushID, item_pushID);
                    dialog.show(mActivity.getFragmentManager(), "RemoveListItemDialogFragment");
                }
            });
            if (item.getHasBought()) {
                deleteItem.setVisibility(View.INVISIBLE);
                boughtByLabel.setVisibility(View.VISIBLE);
                boughtByUser.setVisibility(View.VISIBLE);
                if (item.getBoughtBy().equals(mUser.getName()))
                    boughtByUser.setText(getString(R.string.text_you));
                else
                    boughtByUser.setText(item.getBoughtBy());
                itemName.setPaintFlags(itemName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
            else {
                itemName.setPaintFlags(itemName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
            itemName.setText(item.getItemName());
            return convertView;
        }
    }
}
