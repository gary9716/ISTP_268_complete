package com.hci.lab430.myapplication.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hci.lab430.myapplication.R;
import com.squareup.picasso.Picasso;

/**
 * Created by lab430 on 16/8/1.
 */
public class TestFragment2 extends LogFragment {

    public final static String imgKey = TestFragment2.class.getName() + ".img";
    View fragmentView = null;

    public static TestFragment2 newInstance(int imgResId) {
        TestFragment2 fragment2 = new TestFragment2();
        fragment2.actualName = TestFragment2.class.getName();
        Bundle dataBundle = new Bundle();
        dataBundle.putInt(imgKey, imgResId);
        fragment2.setArguments(dataBundle);

        return fragment2;
    }

    public Picasso mPicasso;
    public int mImgResId;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actualName = TestFragment2.class.getName();
        mImgResId = getArguments().getInt(imgKey);
        mPicasso = Picasso.with(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if(fragmentView == null) {
            fragmentView = inflater.inflate(R.layout.fragment2_layout, container, false);
            mPicasso.load(mImgResId).into((ImageView) fragmentView.findViewById(R.id.imageView));
        }

        return fragmentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }
}
