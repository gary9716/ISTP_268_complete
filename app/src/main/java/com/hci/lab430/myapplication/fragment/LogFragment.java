package com.hci.lab430.myapplication.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by lab430 on 16/8/1.
 */
public class LogFragment extends Fragment {

    public final static String debug_tag = "testFragment";
    public String actualName = null; //it would be assigned in sub-class object

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(debug_tag, actualName + " is in onCreate");
    }

    @Override
    public void onDestroy() {
        Log.d(debug_tag, actualName + " is in onDestroy");
        super.onDestroy();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(debug_tag, actualName + " is in onAttach with Ctxt");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(debug_tag, actualName + " is in onAttach with Act");
    }

    @Override
    public void onDetach() {
        Log.d(debug_tag, actualName + " is in onDetach");
        super.onDetach();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(debug_tag, actualName + " is in onCreateView");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(debug_tag, actualName + " is in onViewCreated");
    }

}
