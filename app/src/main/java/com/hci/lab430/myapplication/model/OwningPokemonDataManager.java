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
    ArrayList<OwnedPokemonInfo> ownedPokemonInfos = null;
    ArrayList<String> pokemonNames = null;
    OwnedPokemonInfo[] initThreePokemons = new OwnedPokemonInfo[3];

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
            OwnedPokemonInfo.typeNames = reader.readLine().split(",");
            reader.close();
        }
        catch(Exception e) {
            Log.d("testCsv", e.getLocalizedMessage());
        }
    }

    public void loadListViewData() {
        ownedPokemonInfos = new ArrayList<>();

        BufferedReader reader;
        String line = null;
        String[] dataFields = null;
        try {

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
                ownedPokemonInfos.add(constructPokemonInfo(dataFields));
            }
            reader.close();

        }
        catch(Exception e) {
            Log.d("testCsv", e.getLocalizedMessage());
        }


    }

    static final int skill_startIndex = 8;

    private OwnedPokemonInfo constructPokemonInfo(String[] dataFields) {

        OwnedPokemonInfo ownedPokemonInfo = new OwnedPokemonInfo();
        ownedPokemonInfo.setPokeId(dataFields[0]);
        ownedPokemonInfo.setName(dataFields[2]);
        ownedPokemonInfo.setLevel(Integer.valueOf(dataFields[3]));
        ownedPokemonInfo.setCurrentHP(Integer.valueOf(dataFields[4]));
        ownedPokemonInfo.setMaxHP(Integer.valueOf(dataFields[5]));
        ownedPokemonInfo.setType_1(Integer.valueOf(dataFields[6]));
        ownedPokemonInfo.setType_2(Integer.valueOf(dataFields[7]));
        //if strings are not enough, rest of array index would point to null.
        String[] skills = new String[OwnedPokemonInfo.maxNumSkills];
        for(int i = skill_startIndex;i < dataFields.length;i++) {
            skills[i - skill_startIndex] = dataFields[i];
        }
        ownedPokemonInfo.setSkill(skills);

        return ownedPokemonInfo;
    }

    public ArrayList<String> getPokemonNames() {
        return pokemonNames;
    }

    public ArrayList<OwnedPokemonInfo> getOwnedPokemonInfos() {
        return ownedPokemonInfos;
    }

    public OwnedPokemonInfo[] getInitThreePokemonInfos() {
        return initThreePokemons;
    }

    public void releaseAll() {
        ownedPokemonInfos.clear();
        ownedPokemonInfos = null;
        pokemonNames.clear();
        pokemonNames = null;
        initThreePokemons = null;
    }

}
