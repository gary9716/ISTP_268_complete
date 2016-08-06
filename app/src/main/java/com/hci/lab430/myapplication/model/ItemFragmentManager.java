package com.hci.lab430.myapplication.model;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

import com.hci.lab430.myapplication.fragment.ItemFragment;

/**
 * Created by lab430 on 16/8/5.
 */
public class ItemFragmentManager implements ItemFragment.OnStateChangedListener, FragmentManager.OnBackStackChangedListener{

    public interface OnBackStackChangedListener {
        void onPushIntoBackStack();
        void onPopOutBackStack();
    }

    public FragmentManager mFragmentManager;
    public ItemFragment mVisibleFragment = null;

    OnBackStackChangedListener mBackStackChangedListener = null;
    int mFragmentContainerId;
    int preBackStackCount = 0;

    public ItemFragmentManager(Activity activity, int fragmentContainerId, ItemFragment[] itemFragments, int initAttachedFragmentIndex) {
        mFragmentManager = activity.getFragmentManager();
        mFragmentManager.addOnBackStackChangedListener(this);
        mFragmentContainerId = fragmentContainerId;
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        for(int i = 0;i < itemFragments.length;i++) {
            ItemFragment itemFragment = itemFragments[i];
            itemFragment.itemIndex = i;
            itemFragment.setOnStateChangedListener(this);
            transaction.add(mFragmentContainerId, itemFragment);
            if(initAttachedFragmentIndex == i) {
                transaction.attach(itemFragment);
            }
            else {
                transaction.detach(itemFragment);
            }
        }
        transaction.commit();

    }

    @Override
    public void onVisible(ItemFragment fragment) {
        mVisibleFragment = fragment;
    }

    public void attachFragment(ItemFragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        if(mVisibleFragment != null) {
            transaction.detach(mVisibleFragment);
        }
        transaction.attach(fragment);
        if(addToBackStack) {
            transaction.addToBackStack(null); //let back button be able to reverse this commitment
        }
        transaction.commit();
    }

    public void setOnBackStackChangedListener(OnBackStackChangedListener listener) {
        mBackStackChangedListener = listener;
    }

    @Override
    public void onBackStackChanged() {
        int currentBackStackCount = mFragmentManager.getBackStackEntryCount();
        if(mBackStackChangedListener != null) {
            if (currentBackStackCount - preBackStackCount > 0) {
                mBackStackChangedListener.onPushIntoBackStack();
            } else if (currentBackStackCount - preBackStackCount < 0) {
                mBackStackChangedListener.onPopOutBackStack();
            }
        }
        preBackStackCount = currentBackStackCount;
    }

}
