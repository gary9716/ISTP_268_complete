package com.hci.lab430.myapplication.adapter;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hci.lab430.myapplication.R;
import com.hci.lab430.myapplication.model.OwnedPokemonInfo;
import com.hci.lab430.myapplication.model.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import eu.davidea.flipview.FlipView;

/**
 * Created by lab430 on 16/7/16.
 */
public class PokemonInfoListViewAdapter extends ArrayAdapter<OwnedPokemonInfo> {

    int mRow_layout_id;
    LayoutInflater mInflater;
    public ArrayList<OwnedPokemonInfo> selectedPokemons;
    public WeakReference<OnPokemonInfoStateChangeListener> stateChangeListener = null;

    public PokemonInfoListViewAdapter(Context context,
                                      int resource,
                                      ArrayList<OwnedPokemonInfo> objects) {
        super(context, resource, objects);
        mRow_layout_id = resource;
        mInflater = LayoutInflater.from(context);
        selectedPokemons = new ArrayList<>();
        ViewHolder.mContext = context;
        ViewHolder.mAdapter = this;
        ViewHolder.mPicasso = Picasso.with(context);
    }

    @Override
    public View getView(int position, View rowView, ViewGroup parent) {
        OwnedPokemonInfo dataItem = getItem(position);
        ViewHolder viewHolder = null;
        if(rowView == null) { //create a new one if it hasn't been initiated yet.
            rowView = mInflater.inflate(mRow_layout_id, null);
            viewHolder = new ViewHolder(rowView);
            rowView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) rowView.getTag();
        }

        viewHolder.setView(dataItem);

        return rowView;
    }

    public interface OnPokemonInfoStateChangeListener {
        void onPokemonInfoSelectedChange(PokemonInfoListViewAdapter adapter);
    }

    void onPokemonSelectedChange(OwnedPokemonInfo ownedPokemonInfo) {
        if(stateChangeListener.get() != null) {
            stateChangeListener.get().onPokemonInfoSelectedChange(this);
        }

        if (ownedPokemonInfo.isSelected) {
            selectedPokemons.add(ownedPokemonInfo);
        } else {
            selectedPokemons.remove(ownedPokemonInfo);
        }

        if(stateChangeListener.get() != null) {
            stateChangeListener.get().onPokemonInfoSelectedChange(this);
        }
    }

    public OwnedPokemonInfo getItemWithName(String name) {

        for(int i = 0;i < getCount();i++) {
            OwnedPokemonInfo ownedPokemonInfo = getItem(i);
            if(name.equals(ownedPokemonInfo.getName())) {
                return ownedPokemonInfo;
            }
        }

        return null;
    }

    public void update(OwnedPokemonInfo newData) {
        OwnedPokemonInfo oldData = getItemWithName(newData.getName());
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
        private FlipView flipView = null;


        public static Picasso mPicasso;
        public static PokemonInfoListViewAdapter mAdapter;
        public static Context mContext;

        private OwnedPokemonInfo mOwnedPokemonInfo = null;

        public ViewHolder(View row_view) {
            mRowView = row_view;
            mAppearanceImg = (ImageView)row_view.findViewById(R.id.appearance_image);
            mNameTxt = (TextView) row_view.findViewById(R.id.name);
            mLevelTxt = (TextView) row_view.findViewById(R.id.level);
            mCurrentHPTxt = (TextView) row_view.findViewById(R.id.currentHP);
            mMaxHPTxt = (TextView) row_view.findViewById(R.id.maxHP);
            mHPBar = (ProgressBar) row_view.findViewById(R.id.HP_progressBar);
            if(mAppearanceImg == null) {
                flipView = (FlipView) row_view.findViewById(R.id.flip_horizontal_oval_view_big);
                if(flipView != null) {
                    flipView.setOnClickListener(this);

                    mAppearanceImg = flipView.getFrontImageView();
                    mAppearanceImg.setAdjustViewBounds(true);
                    mAppearanceImg.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    mAppearanceImg.setScaleX(2);
                    mAppearanceImg.setScaleY(2);
                }
            }
            else {
                mAppearanceImg.setOnClickListener(this);
            }

        }

        public void setView(OwnedPokemonInfo data) {
            mOwnedPokemonInfo = data;
            mRowView.setActivated(data.isSelected);

            ImageLoader.getInstance().displayImage(data.getListImgUrl(), mAppearanceImg);

            if(flipView != null)
                flipView.flipSilently(data.isSelected);

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
            mOwnedPokemonInfo.isSelected = !mOwnedPokemonInfo.isSelected;
            mRowView.setActivated(mOwnedPokemonInfo.isSelected);
            mAdapter.onPokemonSelectedChange(mOwnedPokemonInfo);
        }

        @Override
        public void onClick(View view) {
            setSelected();
            if(flipView != null)
                flipView.showNext();
        }

        private void animateHPBarAndCurrentHP() {
            //play animation
            //it would be asynchronous operation so we need to get a reference
            final OwnedPokemonInfo ownedPokemonInfo = mOwnedPokemonInfo;
            int animationDuration = 1500;
            final ObjectAnimator hpBarAnimator = ObjectAnimator.ofInt(mHPBar, "progress", mHPBar.getProgress(), 100);
            hpBarAnimator.setDuration(animationDuration);

            final ValueAnimator hpTextAnimator = new ValueAnimator();
            hpTextAnimator.setObjectValues(ownedPokemonInfo.getCurrentHP(), ownedPokemonInfo.getMaxHP());// here you set the range, from 0 to "count" value
            hpTextAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    if(!mOwnedPokemonInfo.getName().equals(ownedPokemonInfo.getName())) {
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
                    ownedPokemonInfo.isHealing = false;
                    ownedPokemonInfo.setCurrentHP(ownedPokemonInfo.getMaxHP());
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

    public void releaseAll() {
        mInflater = null;
        ViewHolder.mAdapter = null;
        ViewHolder.mPicasso = null;
        stateChangeListener = null;
        selectedPokemons.clear();
        selectedPokemons = null;
    }

}
