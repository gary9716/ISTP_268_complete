package com.hci.lab430.myapplication.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hci.lab430.myapplication.R;

/**
 * Created by lab430 on 16/8/1.
 */
public class TestFragment1 extends ItemFragment {

    public final static String msgTextKey = TestFragment1.class.getName() + ".msg";
    View fragmentView = null;

    public static TestFragment1 newInstance(String msg) {
        TestFragment1 fragment1 = new TestFragment1();
        fragment1.actualName = TestFragment1.class.getName();
        Bundle dataBundle = new Bundle();
        dataBundle.putString(msgTextKey, msg);
        fragment1.setArguments(dataBundle);

        return fragment1;
    }

    public String mMessage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //retrieve the data from the saved arguments
        mMessage = getArguments().getString(msgTextKey);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if(fragmentView == null) {
            // generate View object by
            // inflating the layout that would be controlled by this fragment
            fragmentView = inflater.inflate(R.layout.fragment1_layout, container, false);

            //bind view with data
            ((TextView) fragmentView.findViewById(R.id.textView)).setText(mMessage);
        }

        return fragmentView;
    }


}
