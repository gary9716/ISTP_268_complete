package com.hci.lab430.myapplication;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.hci.lab430.myapplication.fragment.LogFragment;
import com.hci.lab430.myapplication.fragment.PokemonListFragment;
import com.hci.lab430.myapplication.fragment.TestFragment1;
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
public class DrawerActivity extends CustomizedActivity implements FragmentManager.OnBackStackChangedListener{

    private Toolbar toolbar;
    private Drawer naviDrawer;
    private AccountHeader headerResult = null;
    private IProfile profile;
    private FragmentManager fragmentManager;
    private Fragment[] fragments;
    private int prevBackStackCount = 0;
    private Fragment attachedFragment;
    private int defaultSelectedDrawerIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        //prepare fragments
        fragmentManager = getFragmentManager();
        fragmentManager.addOnBackStackChangedListener(this);
        fragments = new Fragment[3];
        fragments[0] = PokemonListFragment.newInstance();
        ((LogFragment)fragments[0]).actualName = "f0";
        fragments[1] = TestFragment1.newInstance("fake 1");
        ((LogFragment)fragments[1]).actualName = "f1";
        fragments[2] = TestFragment1.newInstance("fake 2");
        ((LogFragment)fragments[2]).actualName = "f2";

        // Set a Toolbar to replace the ActionBar.
        // so it would be laid below the drawer when the drawer comes out
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Drawable profileIcon = null;
        profileIcon = Utils.getDrawable(this, R.drawable.profile3);
        profile = new ProfileDrawerItem().withName("Batman").withEmail("batman@gmail.com").withIcon(profileIcon);

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
                        Log.d("stackTest", "onItemClick:" + position);
                        attachFragment(fragments[position - 1]);
                        return false; //return false to bound back the drawer after clicking
                    }
                })
                .withSavedInstance(savedInstanceState)
                .build();


        FragmentTransaction transaction = fragmentManager.beginTransaction();
        for(int i = fragments.length - 1;i >= 0;i--) {
            transaction.add(R.id.fragmentContainer, fragments[i]);
            if(i != defaultSelectedDrawerIndex) {
                transaction.detach(fragments[i]);
            }
            else {
                attachedFragment = fragments[defaultSelectedDrawerIndex];
            }
            //don't add back stack here
        }
        transaction.commit();

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
        else if(fragmentManager.getBackStackEntryCount() != 0) { //only popstack if stack is not empty
            fragmentManager.popBackStack();
        }
        else {
            super.onBackPressed();
        }

    }

    private void replaceWithTheFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.addToBackStack(null); //let back button be able to reverse this commitment
        transaction.commit();
    }

    private void attachFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if(attachedFragment != null) {
            Log.d("testFragment", "detach");
            transaction.detach(attachedFragment);
        }
        transaction.attach(fragment);
        transaction.addToBackStack(null); //let back button be able to reverse this commitment
        transaction.commit();
        attachedFragment = fragment;
    }

    @Override
    public void onBackStackChanged() {
        Log.d("stackTest", "stackCount:" + fragmentManager.getBackStackEntryCount());
        int currentBackStackCount = fragmentManager.getBackStackEntryCount();
        if(currentBackStackCount - prevBackStackCount < 0) { //if we're poping transection from stack
            for (int i = 0; i < fragments.length; i++) {
                if (fragments[i].isVisible()) {
                    attachedFragment = fragments[i];
                    naviDrawer.setSelectionAtPosition(i + 1, false);
                }
            }
        }
        prevBackStackCount = currentBackStackCount;
    }
}
