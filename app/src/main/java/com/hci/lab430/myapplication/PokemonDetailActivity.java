package com.hci.lab430.myapplication;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hci.lab430.myapplication.model.PokemonInfo;
import com.squareup.picasso.Picasso;

/**
 * Created by lab430 on 16/7/28.
 */
public class PokemonDetailActivity extends AppCompatActivity{

    //constant
    public final static int updateData = 1;
    public final static int removeFromList = 2;

    int resultCode;
    PokemonInfo mData;
    String packageName;
    Resources mRes;
    Picasso mPicasso;
    Intent returnIntent;

    //UI
    ImageView appearanceImg;
    TextView nameText;
    TextView levelText;
    TextView currentHPText;
    TextView maxHPText;
    TextView[] skillsText = new TextView[PokemonInfo.numCurrentSkills];
    ProgressBar HPProgressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mData = getIntent().getParcelableExtra(PokemonInfo.parcelKey);
        mRes = getResources();
        packageName = getPackageName();
        mPicasso = Picasso.with(this);
        returnIntent = new Intent();

        setView();
    }

    //setup UI elements
    private void setView() {
        setContentView(R.layout.detail_view);
        appearanceImg = (ImageView)findViewById(R.id.detail_appearance_img);
        nameText = (TextView)findViewById(R.id.name_text);
        levelText = (TextView)findViewById(R.id.level_text);
        currentHPText = (TextView)findViewById(R.id.currentHP_text);
        maxHPText = (TextView)findViewById(R.id.maxHP_text);
        for(int i = 0;i < PokemonInfo.numCurrentSkills;i++) {
            int skillTextId = mRes.getIdentifier(String.format("skill_%d_text",i + 1), "id", packageName);
            skillsText[i] = (TextView) findViewById(skillTextId);
        }
        HPProgressBar = (ProgressBar)findViewById(R.id.HP_progressBar);

        //bind with data
        mPicasso.load(mData.detailImgId).into(appearanceImg);
        nameText.setText(mData.name);
        levelText.setText(String.valueOf(mData.level));
        currentHPText.setText(String.valueOf(mData.currentHP));
        maxHPText.setText(String.valueOf(mData.maxHP));
        for(int i = 0;i < PokemonInfo.numCurrentSkills;i++) {
            if(mData.skill[i] != null) {
                skillsText[i].setText(mData.skill[i]);
            }
            else {
                skillsText[i].setText("");
            }
        }
        HPProgressBar.setProgress((int)(((float)mData.currentHP/mData.maxHP) * 100));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pokemon_detail_action_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if(itemId == R.id.action_save) {
            returnIntent.putExtra(PokemonInfo.nameKey, mData.name);
            setResult(removeFromList, returnIntent);
            finish();
            return true;
        }
        else if(itemId == R.id.action_level_up){
            //implement some logic here

            return true;
        }

        return false;
    }

}
