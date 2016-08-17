package com.hci.lab430.myapplication.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hci.lab430.myapplication.R;
import com.hci.lab430.myapplication.fragment.PokemonSearchFragment;
import com.hci.lab430.myapplication.model.SearchPokemonInfo;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lab430 on 16/8/11.
 */
public class PokemonSearchListViewAdapter extends ArrayAdapter<SearchPokemonInfo> {

    int mRowLayoutId;
    LayoutInflater mInflater;

    public PokemonSearchListViewAdapter(Context context, int resource, List<SearchPokemonInfo> objects, PokemonSearchFragment fragment) {
        super(context, resource, objects);
        mInflater = LayoutInflater.from(context);
        mRowLayoutId = resource;
        ViewHolder.searchFragment = new WeakReference<PokemonSearchFragment>(fragment);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SearchPokemonInfo dataItem = getItem(position);
        ViewHolder viewHolder;
        if(convertView == null) { //create a new one if it hasn't been initiated yet.
            convertView = mInflater.inflate(mRowLayoutId, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.setView(dataItem);

        return convertView;
    }

    private static class ViewHolder {
        ImageLoader imageLoader;
        TextView[] typeText = new TextView[2];
        TextView hpText;
        TextView nameText;
        ImageView imgView;
        public static WeakReference<PokemonSearchFragment> searchFragment;

        ViewHolder(View rowView) {
            imageLoader = ImageLoader.getInstance();
            typeText[0] = (TextView)rowView.findViewById(R.id.type1Text);
            typeText[1] = (TextView)rowView.findViewById(R.id.type2Text);
            hpText = (TextView)rowView.findViewById(R.id.hpText);
            nameText = (TextView)rowView.findViewById(R.id.nameText);
            imgView = (ImageView)rowView.findViewById(R.id.listImg);
        }

        public void setView(SearchPokemonInfo dataItem) {
            typeText[0].setText("");
            typeText[1].setText("");
            //keep a local strong reference, dont keep global reference
            PokemonSearchFragment localSearchFragment = searchFragment.get();
            if(localSearchFragment != null) {
                ArrayList<Integer> typeIndices = dataItem.getTypeIndices();
                for (int i = 0; i < typeIndices.size(); i++) {
                    if (localSearchFragment.typeList != null) {
                        int typeIndex = typeIndices.get(i);
                        if (localSearchFragment.typeList.get(0).equals("none")) {
                            typeIndex++;
                        }
                        typeText[i].setText(localSearchFragment.typeList.get(typeIndex));
                    }
                }
            }
            hpText.setText(String.valueOf(dataItem.getHP()));
            nameText.setText(dataItem.getName());
            ImageLoader.getInstance().displayImage("http://www.csie.ntu.edu.tw/~r03944003/listImg/" + dataItem.getPokedex() + ".png", imgView, imageLoadingListener);
        }

        ImageLoadingListener imageLoadingListener = new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {

            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if(searchFragment.get() != null)
                    searchFragment.get().adapter.notifyDataSetChanged();
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
        };
    }

    public void releaseAll() {
        mInflater = null;
    }

}
