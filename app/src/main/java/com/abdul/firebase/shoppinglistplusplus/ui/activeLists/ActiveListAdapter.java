package com.abdul.firebase.shoppinglistplusplus.ui.activeLists;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.abdul.firebase.shoppinglistplusplus.R;
import com.abdul.firebase.shoppinglistplusplus.model.ShoppingList;
import com.abdul.firebase.shoppinglistplusplus.model.User;
import com.abdul.firebase.shoppinglistplusplus.utils.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

/**
 * Populates the list_view_active_lists inside ShoppingListsFragment
 */
public class ActiveListAdapter extends ArrayAdapter<ShoppingList> {
    private Context mContext;
    private static final String LOG_TAG = ActiveListAdapter.class.getSimpleName();
    public ActiveListAdapter(Context context, int resource, List<ShoppingList> objects) {
        super(context,resource,objects);
        Log.v(LOG_TAG, "number of objects: " + objects.size());
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ShoppingList sl = getItem(position);
        int number_of_shoppers = sl.getShoppers().size();
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.single_active_list,parent,false);
        }
        TextView listName = (TextView) convertView.findViewById(R.id.text_view_list_name);
        final TextView owner = (TextView) convertView.findViewById(R.id.text_view_created_by_user);
        //TextView editTime = (TextView) convertView.findViewById(R.id.text_view_edit_time);
        TextView shoppers = (TextView) convertView.findViewById(R.id.text_view_number_of_shoppers);
        listName.setText(sl.getListName());
        final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child(Constants.FIREBASE_LOCATION_USERS)
                .child(sl.getOwner());
        //retrieving user for shopping list to get owner's name
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.v(LOG_TAG, "onDataChange");
                User user = dataSnapshot.getValue(User.class);
                owner.setText(user.getName());
                userRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(LOG_TAG, "onCancelled: " + databaseError.getMessage());
            }
        });
        if (number_of_shoppers == 1) {
            shoppers.setText(mContext.getString(R.string.person_shopping,1));
        }
        else if (number_of_shoppers > 1){
            shoppers.setText(mContext.getString(R.string.people_shopping,number_of_shoppers));
        }
        //String dateString = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(new Date(sl.getTimeStampCreated()));
        return convertView;
    }
}
