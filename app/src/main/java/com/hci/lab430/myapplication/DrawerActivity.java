package com.hci.lab430.myapplication;

import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;

import com.hci.lab430.myapplication.fragment.PokemonListFragment;
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
public class DrawerActivity extends CustomizedActivity {

    private Toolbar toolbar;
    private Drawer result;
    private AccountHeader headerResult = null;
    private IProfile profile;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        // Set a Toolbar to replace the ActionBar.
        // so it would be laid below the drawer when the drawer comes out
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Drawable profileIcon = null;
        profileIcon = Utils.getDrawable(this, R.drawable.profile3);
        profile = new ProfileDrawerItem().withName("Batman").withEmail("batman@gmail.com").withIcon(profileIcon);

        buildDrawerHeader(false, savedInstanceState);

        //create the drawer
        result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult)
                .inflateMenu(R.menu.drawer_view)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // do something with the clicked item :D

                        return false; //return false to bound back the drawer after clicking
                    }
                })
                .withSavedInstance(savedInstanceState)
                .build();

        fragmentManager = getFragmentManager();
        Fragment fragment = PokemonListFragment.newInstance();
        fragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment).commit();

    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.pokemon_detail_action_bar_menu, menu);
//        return true;
//    }

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
        outState = result.saveInstanceState(outState);
        //add the values which need to be saved from the accountHeader to the bundle
        outState = headerResult.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (result != null && result.isDrawerOpen()) {
            result.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

}
