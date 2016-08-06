package com.hci.lab430.myapplication.fragment;

import android.app.Fragment;

/**
 * Created by lab430 on 16/8/5.
 */
public class ItemFragment extends LogFragment {

    public interface OnStateChangedListener {
        void onVisible(ItemFragment fragment);
    }

    public int itemIndex = -1;
    OnStateChangedListener mStateChangedListener = null;

    public void setOnStateChangedListener(OnStateChangedListener stateChangedListener) {
        mStateChangedListener = stateChangedListener;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(mStateChangedListener != null) {
            mStateChangedListener.onVisible(this);
        }
    }

}
