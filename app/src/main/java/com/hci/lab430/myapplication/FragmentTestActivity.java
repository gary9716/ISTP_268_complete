package com.hci.lab430.myapplication;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.hci.lab430.myapplication.fragment.LogFragment;
import com.hci.lab430.myapplication.fragment.TestFragment1;

/**
 * Created by lab430 on 16/8/1.
 */
public class FragmentTestActivity extends AppCompatActivity implements View.OnClickListener{

    int fragmentContainerId;
    FragmentManager fragmentManager;
    Fragment[] fragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_test_layout);
        fragmentContainerId = R.id.fragmentContainer;
        findViewById(R.id.fragment_1).setOnClickListener(this);
        findViewById(R.id.fragment_2).setOnClickListener(this);
        findViewById(R.id.fragment_2_remove).setOnClickListener(this);

        fragmentManager = getFragmentManager();

        fragments = new Fragment[2];
        fragments[0] = TestFragment1.newInstance("Fragment 1 pre-allocated");
        ((LogFragment)fragments[0]).actualName = "F1";
        fragments[1] = TestFragment1.newInstance("Fragment 2 pre-allocated");
        ((LogFragment)fragments[1]).actualName = "F2";
    }

    @Override
    public void onBackPressed() {
        fragmentManager.popBackStack();
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if(viewId == R.id.fragment_1) {
            replaceWithTheFragment(fragments[0]);

        }
        else if(viewId == R.id.fragment_2) {
            replaceWithTheFragment(fragments[1]);

        }
        else if(viewId == R.id.fragment_2_remove) {
            removeTheFragment(fragments[1]);
        }

    }

    private void removeTheFragment(Fragment fragment) {
        if(fragment.isAdded()) {
            fragmentManager.beginTransaction().remove(fragment).commit();
        }
    }

    private void replaceWithTheFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(fragmentContainerId, fragment);
        transaction.addToBackStack(null); //let back button be able to reverse this commitment
        transaction.commit();
    }

}
