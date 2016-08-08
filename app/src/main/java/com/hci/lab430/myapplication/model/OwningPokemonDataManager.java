package com.hci.lab430.myapplication.model;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.parse.CountCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by lab430 on 16/7/24.
 */
public class OwningPokemonDataManager {
    Context mContext;
    Resources mRes;
    String packageName;
    ArrayList<OwningPokemonInfo> owningPokemonInfos = null;
    ArrayList<String> pokemonNames = null;
    OwningPokemonInfo[] initThreePokemons = new OwningPokemonInfo[3];

    public OwningPokemonDataManager(Context context) {
        mContext = context;
        mRes = mContext.getResources();
        packageName = context.getPackageName();

    }

    public void loadPokemonTypes() {
        BufferedReader reader;
        try {
            //load pokemon types
            reader = new BufferedReader(new InputStreamReader(mContext.getAssets().open("pokemon_types.csv")));
            OwningPokemonInfo.typeNames = reader.readLine().split(",");
            reader.close();
        }
        catch(Exception e) {
            Log.d("testCsv", e.getLocalizedMessage());
        }
    }

    public void loadListViewData() {
        owningPokemonInfos = new ArrayList<>();

        BufferedReader reader;
        String line = null;
        String[] dataFields = null;
        try {
            //load pokemon types
            reader = new BufferedReader(new InputStreamReader(mContext.getAssets().open("pokemon_types.csv")));
            OwningPokemonInfo.typeNames = reader.readLine().split(",");
            reader.close();

            //load initial three pokemon data
            reader = new BufferedReader(new InputStreamReader(mContext.getAssets().open("init_pokemon_data.csv")));
            for(int i = 0;i < 3;i++) {
                dataFields = reader.readLine().split(",");
                initThreePokemons[i] = constructPokemonInfo(dataFields);
            }
            reader.close();

            //load rest of pokemon data
            reader = new BufferedReader(new InputStreamReader(mContext.getAssets().open("pokemon_data.csv")));
            while ((line = reader.readLine()) != null) {
                dataFields = line.split(",");
                owningPokemonInfos.add(constructPokemonInfo(dataFields));
            }
            reader.close();

        }
        catch(Exception e) {
            Log.d("testCsv", e.getLocalizedMessage());
        }


    }

    static final int skill_startIndex = 8;

    private OwningPokemonInfo constructPokemonInfo(String[] dataFields) {

        OwningPokemonInfo owningPokemonInfo = new OwningPokemonInfo();
        int pokeId = Integer.valueOf(dataFields[0]);
        String listImgUrl = String.format("http://pokeapi.co/media/sprites/pokemon/%d.png", pokeId);
        owningPokemonInfo.setListImgUrl(listImgUrl);
        owningPokemonInfo.setDetailImgId(mRes.getIdentifier("detail_" + dataFields[1], "drawable", packageName));
        int listImgId = mRes.getIdentifier("list_" + dataFields[1], "drawable", packageName);
        owningPokemonInfo.setListImgId(listImgId);
        owningPokemonInfo.setName(dataFields[2]);
        owningPokemonInfo.setLevel(Integer.valueOf(dataFields[3]));
        owningPokemonInfo.setCurrentHP(Integer.valueOf(dataFields[4]));
        owningPokemonInfo.setMaxHP(Integer.valueOf(dataFields[5]));
        owningPokemonInfo.setType_1(Integer.valueOf(dataFields[6]));
        owningPokemonInfo.setType_2(Integer.valueOf(dataFields[7]));
        //if strings are not enough, rest of array index would point to null.
        for(int i = skill_startIndex;i < dataFields.length;i++) {
            owningPokemonInfo.getSkill()[i - skill_startIndex] = dataFields[i];
        }

        return owningPokemonInfo;
    }

    public ArrayList<String> getPokemonNames() {
        return pokemonNames;
    }

    public ArrayList<OwningPokemonInfo> getOwningPokemonInfos() {
        return owningPokemonInfos;
    }

    public OwningPokemonInfo[] getInitThreePokemonInfos() {
        return initThreePokemons;
    }

}
