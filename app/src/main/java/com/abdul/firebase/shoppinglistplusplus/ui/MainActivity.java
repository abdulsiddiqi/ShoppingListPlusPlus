package com.abdul.firebase.shoppinglistplusplus.ui;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.abdul.firebase.shoppinglistplusplus.R;
import com.abdul.firebase.shoppinglistplusplus.model.User;
import com.abdul.firebase.shoppinglistplusplus.ui.activeLists.AddListDialogFragment;
import com.abdul.firebase.shoppinglistplusplus.ui.activeLists.ShoppingListsFragment;
import com.abdul.firebase.shoppinglistplusplus.ui.login.LoginActivity;
import com.abdul.firebase.shoppinglistplusplus.ui.meals.AddMealDialogFragment;
import com.abdul.firebase.shoppinglistplusplus.ui.meals.MealsFragment;
import com.abdul.firebase.shoppinglistplusplus.utils.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Represents the home screen of the app which
 * has a {@link ViewPager} with {@link ShoppingListsFragment} and {@link MealsFragment}
 */
public class MainActivity extends BaseActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /**
         * Link layout elements from XML and setup the toolbar
         */
        initializeScreen();
    }


    /**
     * Override onOptionsItemSelected to use main_menu instead of BaseActivity menu
     *
     * @param menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu; this adds items to the action bar if it is present. */
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Override onOptionsItemSelected to add action_settings only to the MainActivity
     *
     * @param item
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            SharedPreferences sp = getSharedPreferences(getPackageName(),Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.remove(getString(R.string.pref_email))
                    .remove(getString(R.string.pref_firebase_key))
                    .remove(getString(R.string.pref_provider))
                    .remove(getString(R.string.pref_user_name));
            editor.apply();
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Link layout elements from XML and setup the toolbar
     */
    public void initializeScreen() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        /**
         * Create SectionPagerAdapter, set it as adapter to viewPager with setOffscreenPageLimit(2)
         **/
        SectionPagerAdapter adapter = new SectionPagerAdapter(getSupportFragmentManager());
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(adapter);
        /**
         * Setup the mTabLayout with view pager
         */
        tabLayout.setupWithViewPager(viewPager);
        final SharedPreferences sp = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        String provider = sp.getString(getString(R.string.pref_provider),"Google");
        if (provider.equals("Google")) {
            String key = sp.getString(getString(R.string.pref_firebase_key),"");
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference()
                    .child(Constants.FIREBASE_LOCATION_USERS)
                    .child(key);
            //trying to retrieve user data for Google method
            usersRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    String firstName = user.getName();
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString(getString(R.string.pref_email),user.getEmail());
                    editor.apply();
                    firstName = firstName.split(" ")[0];
                    setTitle(firstName + "'s Lists");
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.v(LOG_TAG,"Error retrieving user info for Google method with error " + databaseError);
                }
            });

        }
        else {
            String displayName = sp.getString(getString(R.string.pref_user_name),"");
            setTitle(displayName.split(" ")[0] + "'s Lists");
        }
    }

    /**
     * Create an instance of the AddList dialog fragment and show it
     */
    public void showAddListDialog(View view) {
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = AddListDialogFragment.newInstance();
        dialog.show(MainActivity.this.getFragmentManager(), "AddListDialogFragment");
    }

    /**
     * Create an instance of the AddMeal dialog fragment and show it
     */
    public void showAddMealDialog(View view) {
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = AddMealDialogFragment.newInstance();
        dialog.show(MainActivity.this.getFragmentManager(), "AddMealDialogFragment");
    }

    /**
     * SectionPagerAdapter class that extends FragmentStatePagerAdapter to save fragments state
     */
    public class SectionPagerAdapter extends FragmentStatePagerAdapter {

        public SectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Use positions (0 and 1) to find and instantiate fragments with newInstance()
         *
         * @param position
         */
        @Override
        public Fragment getItem(int position) {

            Fragment fragment = null;

            /**
             * Set fragment to different fragments depending on position in ViewPager
             */
            switch (position) {
                case 0:
                    fragment = ShoppingListsFragment.newInstance();
                    break;
                case 1:
                    fragment = MealsFragment.newInstance();
                    break;
                default:
                    fragment = ShoppingListsFragment.newInstance();
                    break;
            }

            return fragment;
        }


        @Override
        public int getCount() {
            return 2;
        }

        /**
         * Set string resources as titles for each fragment by it's position
         *
         * @param position
         */
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.pager_title_shopping_lists);
                case 1:
                default:
                    return getString(R.string.pager_title_meals);
            }
        }
    }
}
