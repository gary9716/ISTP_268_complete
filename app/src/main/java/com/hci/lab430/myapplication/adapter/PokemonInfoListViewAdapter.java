package com.hci.lab430.myapplication.adapter;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hci.lab430.myapplication.R;
import com.hci.lab430.myapplication.model.OwningPokemonInfo;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.ParseFile;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by lab430 on 16/7/16.
 */
public class PokemonInfoListViewAdapter extends ArrayAdapter<OwningPokemonInfo> {

    int mRow_layout_id;
    LayoutInflater mInflater;
    Picasso mPicasso;
    public ArrayList<OwningPokemonInfo> selectedPokemons;
    OnPokemonInfoStateChangeListener stateChangeListener = null;

    public PokemonInfoListViewAdapter(Context context,
                                      int resource,
                                      ArrayList<OwningPokemonInfo> objects) {
        super(context, resource, objects);
        mRow_layout_id = resource;
        mInflater = LayoutInflater.from(context);
        mPicasso = Picasso.with(context);
        selectedPokemons = new ArrayList<>();

    }

    public PokemonInfoListViewAdapter(Context context,
                                      int resource,
                                      ArrayList<OwningPokemonInfo> objects,
                                      OnPokemonInfoStateChangeListener listener) {
        this(context, resource, objects);
        stateChangeListener = listener;

    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        OwningPokemonInfo dataItem = getItem(position);
        ViewHolder viewHolder = null;
        if(convertView == null) { //create a new one if it hasn't been initiated yet.
            convertView = mInflater.inflate(mRow_layout_id, parent, false);
            viewHolder = new ViewHolder(convertView, mPicasso, this);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.setView(dataItem);

        return convertView;
    }

    public interface OnPokemonInfoStateChangeListener {
        void onPokemonInfoSelectedChange(PokemonInfoListViewAdapter adapter);
    }

    void onPokemonSelectedChange(OwningPokemonInfo owningPokemonInfo) {
        if(stateChangeListener != null) {
            stateChangeListener.onPokemonInfoSelectedChange(this);
        }

        if(owningPokemonInfo.isSelected) {
            selectedPokemons.add(owningPokemonInfo);
        }
        else {
            selectedPokemons.remove(owningPokemonInfo);
        }

        if(stateChangeListener != null) {
            stateChangeListener.onPokemonInfoSelectedChange(this);
        }
    }

    public OwningPokemonInfo getItemWithName(String name) {

        for(int i = 0;i < getCount();i++) {
            OwningPokemonInfo owningPokemonInfo = getItem(i);
            if(name.equals(owningPokemonInfo.getName())) {
                return owningPokemonInfo;
            }
        }

        return null;
    }

    public void update(OwningPokemonInfo newData) {
        OwningPokemonInfo oldData = getItemWithName(newData.getName());
        oldData.setSkill(newData.getSkill());
        oldData.setCurrentHP(newData.getCurrentHP());
        oldData.setMaxHP(newData.getMaxHP());
        oldData.setLevel(newData.getLevel());
        notifyDataSetChanged();
    }

    public void selectRow(View rowView) {
        //once a row view has been initiated, it should be bound with a view holder
        ViewHolder viewHolder = (ViewHolder)rowView.getTag();
        viewHolder.setSelected();
    }

    public static class ViewHolder implements View.OnClickListener{

        private View mRowView = null;
        private ImageView mAppearanceImg = null;
        private TextView mNameTxt = null;
        private TextView mLevelTxt = null;
        private TextView mCurrentHPTxt = null;
        private TextView mMaxHPTxt = null;
        private ProgressBar mHPBar = null;

        private Picasso mPicasso;
        private OwningPokemonInfo mOwningPokemonInfo = null;
        private PokemonInfoListViewAdapter mAdapter;

        public ViewHolder(View row_view, Picasso picasso, PokemonInfoListViewAdapter adapter) {
            mRowView = row_view;
            mAppearanceImg = (ImageView)row_view.findViewById(R.id.appearance_image);
            mNameTxt = (TextView) row_view.findViewById(R.id.name);
            mLevelTxt = (TextView) row_view.findViewById(R.id.level);
            mCurrentHPTxt = (TextView) row_view.findViewById(R.id.currentHP);
            mMaxHPTxt = (TextView) row_view.findViewById(R.id.maxHP);
            mHPBar = (ProgressBar) row_view.findViewById(R.id.HP_progressBar);
            mPicasso = picasso;
            mAdapter = adapter;
        }

        public void setView(OwningPokemonInfo data) {
            mOwningPokemonInfo = data;
            ImageLoader.getInstance().displayImage(data.getListImgUrl(), mAppearanceImg);
            mRowView.setActivated(data.isSelected);
            mPicasso.load(data.getListImgId()).into(mAppearanceImg);
            mAppearanceImg.setOnClickListener(this);

            mNameTxt.setText(data.getName());
            mLevelTxt.setText(String.valueOf(data.getLevel()));
            mMaxHPTxt.setText(String.valueOf(data.getMaxHP()));

            if (data.isHealing && data.getCurrentHP() < data.getMaxHP()) {
                animateHPBarAndCurrentHP();
            }
            else {
                mCurrentHPTxt.setText(String.valueOf(data.getCurrentHP()));
                mHPBar.setProgress((int) (((float) data.getCurrentHP() / data.getMaxHP()) * 100));
            }
        }

        public void setSelected() {
            mOwningPokemonInfo.isSelected = !mOwningPokemonInfo.isSelected;
            mRowView.setActivated(mOwningPokemonInfo.isSelected);
            mAdapter.onPokemonSelectedChange(mOwningPokemonInfo);
        }

        @Override
        public void onClick(View view) {
            int viewId = view.getId();
            if (viewId == R.id.appearance_image) {
                setSelected();
            }
        }

        private void animateHPBarAndCurrentHP() {
            //play animation
            //it would be asynchronous operation so we need to get a reference
            final OwningPokemonInfo owningPokemonInfo = mOwningPokemonInfo;
            int animationDuration = 1500;
            final ObjectAnimator hpBarAnimator = ObjectAnimator.ofInt(mHPBar, "progress", mHPBar.getProgress(), 100);
            hpBarAnimator.setDuration(animationDuration);

            final ValueAnimator hpTextAnimator = new ValueAnimator();
            hpTextAnimator.setObjectValues(owningPokemonInfo.getCurrentHP(), owningPokemonInfo.getMaxHP());// here you set the range, from 0 to "count" value
            hpTextAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    if(!mOwningPokemonInfo.getName().equals(owningPokemonInfo.getName())) {
                        hpBarAnimator.cancel();
                        hpTextAnimator.cancel();
                    }

                    mCurrentHPTxt.setText(String.valueOf(animation.getAnimatedValue()));
                }
            });
            hpTextAnimator.setDuration(animationDuration); // here you set the duration of the anim
            hpTextAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    owningPokemonInfo.isHealing = false;
                    owningPokemonInfo.setCurrentHP(owningPokemonInfo.getMaxHP());
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });

            hpTextAnimator.start();
            hpBarAnimator.start();
        }

    }
}
