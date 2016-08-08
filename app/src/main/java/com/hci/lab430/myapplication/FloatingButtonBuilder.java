package com.hci.lab430.myapplication;

import android.content.Context;

import com.getbase.floatingactionbutton.FloatingActionButton;
/**
 * Created by lab430 on 16/8/8.
 */
public class FloatingButtonBuilder {

    public static FloatingActionButton getFAB(Context context) {
        FloatingActionButton button = new FloatingActionButton(context);
        button.setSize(FloatingActionButton.SIZE_MINI);
        button.setColorNormalResId(R.color.pink);
        button.setColorPressedResId(R.color.pink_pressed);
        button.setStrokeVisible(false);
        return button;
    }

}
