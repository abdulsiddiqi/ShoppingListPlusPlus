package com.abdul.firebase.shoppinglistplusplus.ui.activeLists;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.abdul.firebase.shoppinglistplusplus.R;
import com.abdul.firebase.shoppinglistplusplus.model.ShoppingList;
import com.abdul.firebase.shoppinglistplusplus.ui.activeListDetails.ActiveListDetailsActivity;
import com.abdul.firebase.shoppinglistplusplus.utils.Constants;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

//import com.abdul.firebase.shoppinglistplusplus.ui.activeListDetails.ActiveListDetailsActivity;


/**
 * A simple {@link Fragment} subclass that shows a list of all shopping lists a user can see.
 * Use the {@link ShoppingListsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShoppingListsFragment extends Fragment {
    private static String LOG_TAG = ShoppingListsFragment.class.getSimpleName();
    private ListView mShoppingListsView;
    private ActiveListAdapter mShoppingListAdapter;
    private List<ShoppingList> mShoppingLists;
    private List<String>       mShoppingListsIds;
    // Firebase instance variables
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mShoppingListDatabaseReference;
    private ChildEventListener mChildEventListener;

    public ShoppingListsFragment() {
        /* Required empty public constructor */
    }

    /**
     * Create fragment and pass bundle with data as it's arguments
     * Right now there are not arguments...but eventually there will be.
     */
    public static ShoppingListsFragment newInstance() {
        ShoppingListsFragment fragment = new ShoppingListsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * Initialize instance variables with data from bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /**
         * Initalize UI elements
         */
        View rootView = inflater.inflate(R.layout.fragment_shopping_lists, container, false);
        initializeScreen(rootView);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mShoppingListDatabaseReference = mFirebaseDatabase.getReference().child(Constants.FIREBASE_LOCATION_ACTIVE_LIST);
        // Initialize message ListView and its adapter
        mShoppingLists = new ArrayList<>();
        mShoppingListsIds = new ArrayList<>();
        mShoppingListAdapter = new ActiveListAdapter(getContext(),R.layout.fragment_shopping_lists, mShoppingLists);
        mShoppingListsView.setAdapter(mShoppingListAdapter);


        /**
         * Set interactive bits, such as click events and adapters
         */
        mShoppingListsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String shoppingList_pushId = mShoppingListsIds.get(position);
                Log.d(LOG_TAG, "onItemClick with id: " + shoppingList_pushId);
                Intent intent = new Intent(getActivity(), ActiveListDetailsActivity.class);
                intent.putExtra(Constants.KEY_LIST_PUSH_ID, shoppingList_pushId);
                startActivity(intent);
            }
        });
        //Detects changes in the shopping lists and manipulates the adapter
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(LOG_TAG, "onChildAdded");
                int index = 0;
                if (previousChildName != null) {
                    index = mShoppingListsIds.indexOf(previousChildName) + 1;
                }
                mShoppingListsIds.add(index,dataSnapshot.getKey());
                mShoppingLists.add(index, dataSnapshot.getValue(ShoppingList.class));
                mShoppingListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(LOG_TAG, "onChildChanged");
                int index = 0;
                if (previousChildName != null) {
                    index = mShoppingListsIds.indexOf(previousChildName) + 1;
                }
                mShoppingLists.set(index,dataSnapshot.getValue(ShoppingList.class));
                mShoppingListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(LOG_TAG, "onChildRemoved");
                int index = mShoppingListsIds.indexOf(dataSnapshot.getKey());
                mShoppingListsIds.remove(index);
                mShoppingLists.remove(index);
                mShoppingListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(LOG_TAG, "onChildMoved");
                int oldIndex = mShoppingListsIds.indexOf(dataSnapshot.getKey());
                mShoppingListsIds.remove(oldIndex);
                mShoppingLists.remove(oldIndex);
                mShoppingListsIds.add(dataSnapshot.getKey());
                mShoppingLists.add(dataSnapshot.getValue(ShoppingList.class));
                mShoppingListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(LOG_TAG, "onCancelled");
            }
        };
        mShoppingListDatabaseReference.addChildEventListener(mChildEventListener);

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //mShoppingListAdapter.cleanup();
    }


    /**
     * Link layout elements from XML
     */
    private void initializeScreen(View rootView) {
        mShoppingListsView = (ListView) rootView.findViewById(R.id.list_view_active_lists);
    }



}
