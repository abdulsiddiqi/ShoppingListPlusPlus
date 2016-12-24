package com.abdul.firebase.shoppinglistplusplus.ui.activeLists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.abdul.firebase.shoppinglistplusplus.R;
import com.abdul.firebase.shoppinglistplusplus.model.ShoppingList;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Populates the list_view_active_lists inside ShoppingListsFragment
 */
public class ActiveListAdapter extends ArrayAdapter<ShoppingList> {

    public ActiveListAdapter(Context context, int resource, List<ShoppingList> objects) {
        super(context,resource,objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ShoppingList sl = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.single_active_list,parent,false);
        }
        TextView listName = (TextView) convertView.findViewById(R.id.text_view_list_name);
        TextView owner = (TextView) convertView.findViewById(R.id.text_view_created_by_user);
        TextView editTime = (TextView) convertView.findViewById(R.id.text_view_edit_time);

        listName.setText(sl.getListName());
        owner.setText(sl.getOwner());
        String dateString = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(new Date(sl.getTimeStampCreated()));
        editTime.setText(dateString);
        return convertView;
    }
}
