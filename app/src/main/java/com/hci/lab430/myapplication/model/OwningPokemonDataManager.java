package com.hci.lab430.myapplication.model;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

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
    ArrayList<PokemonInfo> pokemonInfos = null;
    ArrayList<String> pokemonNames = null;
    PokemonInfo[] initThreePokemons = new PokemonInfo[3];

    public OwningPokemonDataManager(Context context) {
        mContext = context;
        mRes = mContext.getResources();
        packageName = context.getPackageName();
        loadListViewData();
    }

    public void loadListViewData() {
        pokemonInfos = new ArrayList<>();
        BufferedReader reader;
        String line = null;
        String[] dataFields = null;
        try {
            //load pokemon types
            reader = new BufferedReader(new InputStreamReader(mContext.getAssets().open("pokemon_types.csv")));
            PokemonInfo.typeNames = reader.readLine().split(",");
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
                pokemonInfos.add(constructPokemonInfo(dataFields));
            }
            reader.close();

        }
        catch(Exception e) {
            Log.d("testCsv", e.getLocalizedMessage());
        }


    }

    static final int skill_startIndex = 7;

    private PokemonInfo constructPokemonInfo(String[] dataFields) {
        PokemonInfo pokemonInfo = new PokemonInfo();
        pokemonInfo.detailImgId = mRes.getIdentifier("detail_" + dataFields[0],"drawable",packageName);
        pokemonInfo.listImgId = mRes.getIdentifier("list_" + dataFields[0],"drawable",packageName);
        pokemonInfo.name = dataFields[1];
        pokemonInfo.level = Integer.valueOf(dataFields[2]);
        pokemonInfo.currentHP = Integer.valueOf(dataFields[3]);
        pokemonInfo.maxHP = Integer.valueOf(dataFields[4]);
        pokemonInfo.type_1 = Integer.valueOf(dataFields[5]);
        pokemonInfo.type_2 = Integer.valueOf(dataFields[6]);
        //if strings are not enough, rest of array index would point to null.
        for(int i = skill_startIndex;i < dataFields.length;i++) {
            pokemonInfo.skill[i - skill_startIndex] = dataFields[i];
        }

        return pokemonInfo;
    }

    public ArrayList<String> getPokemonNames() {
        return pokemonNames;
    }

    public ArrayList<PokemonInfo> getPokemonInfos() {
        return pokemonInfos;
    }

    public PokemonInfo[] getInitThreePokemonInfos() {
        return initThreePokemons;
    }

}
