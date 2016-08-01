package com.hci.lab430.myapplication;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.hci.lab430.myapplication.fragment.TestFragment1;
import com.hci.lab430.myapplication.fragment.TestFragment2;

/**
 * Created by lab430 on 16/8/1.
 */
public class FragmentTestActivity extends AppCompatActivity implements View.OnClickListener{

    int fragmentContainerId;
    FragmentManager fragmentManager;
    final int mode = 2;
    Fragment[] fragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_test_layout);
        fragmentContainerId = R.id.fragmentContainer;
        findViewById(R.id.fragment_1).setOnClickListener(this);
        findViewById(R.id.fragment_2).setOnClickListener(this);

        fragmentManager = getFragmentManager();

        fragments = new Fragment[2];
        fragments[0] = TestFragment1.newInstance("Fragment 1 pre-allocated");
        fragments[1] = TestFragment2.newInstance(R.drawable.pokemon);

    }

    @Override
    public void onBackPressed() {
        fragmentManager.popBackStack();
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if(viewId == R.id.fragment_1) {
            if(mode == 1) {
                replaceWithTheFragment(fragments[0]);
            }
            else if(mode == 2) {
                displayFragment(fragments[0]);
            }
        }
        else if(viewId == R.id.fragment_2) {
            if(mode == 1) {
                replaceWithTheFragment(fragments[1]);
            }
            else if(mode == 2) {
                displayFragment(fragments[1]);
            }
        }

    }

    private void replaceWithTheFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(fragmentContainerId, fragment);
        transaction.addToBackStack(null); //let back button be able to reverse this commitment
        transaction.commit();
    }

    private void displayFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if(!fragment.isAdded()) {
            transaction.add(fragmentContainerId, fragment);
        }
        else {
            for(int i = 0;i < fragments.length; i++) {
                if(fragments[i].isVisible()) {
                    transaction.hide(fragments[i]);
                }
            }
            transaction.show(fragment);
        }

        transaction.commit();
    }

}
