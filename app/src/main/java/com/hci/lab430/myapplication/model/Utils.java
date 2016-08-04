package com.hci.lab430.myapplication.model;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;

/**
 * Created by lab430 on 16/8/3.
 */
public class Utils {

    public static Drawable getDrawable(Context context, int drawableId) {
        if(Build.VERSION.SDK_INT < 21) {
            return context.getResources().getDrawable(drawableId);
        }
        else {
            return context.getResources().getDrawable(drawableId, null);
        }
    }

    public static MediaPlayer loadSongFromAssets(Context context, String fileName) {
        MediaPlayer mediaPlayer = null;
        try {
            AssetFileDescriptor descriptor;
            descriptor = context.getAssets().openFd(fileName);

            if(descriptor != null) {
                mediaPlayer = new MediaPlayer();

                long start = descriptor.getStartOffset();
                long end = descriptor.getLength();

                mediaPlayer.setDataSource(descriptor.getFileDescriptor(), start, end);
                mediaPlayer.prepareAsync();
            }

        }
        catch(Exception e) {
        }

        return mediaPlayer;
    }

}
