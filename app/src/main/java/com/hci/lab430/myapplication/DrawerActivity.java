package com.hci.lab430.myapplication;

import android.app.Application;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.hci.lab430.myapplication.fragment.ItemFragment;
import com.hci.lab430.myapplication.fragment.LogFragment;
import com.hci.lab430.myapplication.fragment.PokemonListFragment;
import com.hci.lab430.myapplication.fragment.TestFragment1;
import com.hci.lab430.myapplication.model.ItemFragmentManager;
import com.hci.lab430.myapplication.model.Utils;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

/**
 * Created by lab430 on 16/8/2.
 */
public class DrawerActivity extends CustomizedActivity implements ItemFragmentManager.OnBackStackChangedListener {

    private Toolbar toolbar;
    private Drawer naviDrawer;
    private AccountHeader headerResult = null;
    private IProfile profile;
    private ItemFragmentManager itemFragmentManager;
    private ItemFragment[] fragments;
    private int defaultSelectedDrawerIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        //prepare fragments

        fragments = new ItemFragment[3];
        fragments[0] = PokemonListFragment.newInstance();
        ((LogFragment)fragments[0]).actualName = "f0";
        fragments[1] = TestFragment1.newInstance("fake 1");
        ((LogFragment)fragments[1]).actualName = "f1";
        fragments[2] = TestFragment1.newInstance("fake 2");
        ((LogFragment)fragments[2]).actualName = "f2";

        itemFragmentManager = new ItemFragmentManager(this, R.id.fragmentContainer, fragments, defaultSelectedDrawerIndex);
        itemFragmentManager.setOnBackStackChangedListener(this);

        // Set a Toolbar to replace the ActionBar.
        // so it would be laid below the drawer when the drawer comes out
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        SharedPreferences preferences = getSharedPreferences(Application.class.getName(), MODE_PRIVATE);
        String profileName = preferences.getString(MainActivity.nameTextKey, "Batman");
        String email = preferences.getString(MainActivity.emailKey, "batman@gmail.com");
        String imgUrl = preferences.getString(MainActivity.profileImgKey, null);
        if(imgUrl == null) {
            Drawable profileIcon = null;
            profileIcon = Utils.getDrawable(this, R.drawable.profile3);
            profile = new ProfileDrawerItem().withName(profileName).withEmail(email).withIcon(profileIcon);
        }
        else {
            Log.d("testFBUrl",imgUrl);
            profile = new ProfileDrawerItem().withName(profileName).withEmail(email).withIcon(imgUrl);
        }

        buildDrawerHeader(false, savedInstanceState);

        //create the drawer
        naviDrawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult)
                .inflateMenu(R.menu.drawer_view)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // do something with the clicked item :D
                        //first item come with index 1
                        itemFragmentManager.attachFragment(fragments[position - 1], true);
                        return false; //return false to bound back the drawer after clicking
                    }
                })
                .withSavedInstance(savedInstanceState)
                .build();

        //don't fire the listener
        naviDrawer.setSelectionAtPosition(defaultSelectedDrawerIndex + 1, false);

    }

    private void buildDrawerHeader(boolean compact, Bundle savedInstanceState) {
        // Create the AccountHeader
        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .withCompactStyle(compact)
                .addProfiles(profile)
                .withSavedInstance(savedInstanceState)
                .build();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the drawer to the bundle
        outState = naviDrawer.saveInstanceState(outState);
        //add the values which need to be saved from the accountHeader to the bundle
        outState = headerResult.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (naviDrawer != null && naviDrawer.isDrawerOpen()) {
            naviDrawer.closeDrawer();
        }
        else if(itemFragmentManager.mFragmentManager.getBackStackEntryCount() != 0) { //only popstack if stack is not empty
            itemFragmentManager.mFragmentManager.popBackStack();
        }
        else {
            super.onBackPressed();
        }

    }

    @Override
    public void onPushIntoBackStack() {
        //do nothing
    }

    @Override
    public void onPopOutBackStack() {
        naviDrawer.setSelectionAtPosition(itemFragmentManager.mVisibleFragment.itemIndex + 1, false);
    }
}
