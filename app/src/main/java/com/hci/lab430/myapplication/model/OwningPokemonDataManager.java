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
    ArrayList<PokemonInfo> pokemonInfos;
    ArrayList<String> pokemonNames;

    public OwningPokemonDataManager(Context context) {
        mContext = context;
        mRes = mContext.getResources();
        packageName = context.getPackageName();
        loadTestingData();

        pokemonNames = new ArrayList<>();
        for(PokemonInfo pokemonInfo : pokemonInfos) {
            pokemonNames.add(pokemonInfo.name);
        }
    }

    private void loadTestingData() {
        pokemonInfos = new ArrayList<>();
        BufferedReader reader;
        String line = null;
        try {
            reader = new BufferedReader(new InputStreamReader(mContext.getAssets().open("pokemon_types.csv")));
            PokemonInfo.typeNames = reader.readLine().split(",");
            reader.close();

            reader = new BufferedReader(new InputStreamReader(mContext.getAssets().open("pokemon_data.csv")));
            int skill_startIndex = 7;
            while ((line = reader.readLine()) != null) {
                String[] dataFields = line.split(",");
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
                pokemonInfos.add(pokemonInfo);
            }
            reader.close();

        }
        catch(Exception e) {
            Log.d("testCsv", e.getLocalizedMessage());
        }


    }


    public ArrayList<String> getPokemonNames() {
        return pokemonNames;
    }

    public ArrayList<PokemonInfo> getPokemonInfos() {
        return pokemonInfos;
    }


}
