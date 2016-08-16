package com.hci.lab430.myapplication.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;

/**
 * Created by lab430 on 16/8/11.
 */
@ParseClassName("Pokemon")
public class SearchPokemonInfo extends ParseObject {

    // If you have uploaded data before and
    // prepare to query from parse cloud server
    // these keys need to match with those column names
    public final static String nameKey =  "name";
    public final static String hpKey = "hp";
    public final static String spAtkKey = "sp_atk";
    public final static String spDefKey = "sp_def";
    public final static String resIdKey = "resId";
    public final static String typesKey = "types";
    public final static String movesKey = "moves";


    public static ParseQuery<SearchPokemonInfo> getQuery() {
        return ParseQuery.getQuery(SearchPokemonInfo.class);
    }

    public ArrayList<Integer> getTypeIndices() {
        return (ArrayList)get(typesKey);
    }

    public String getName() {
        return getString(nameKey);
    }

    public int getHP() {
        return getInt(hpKey);
    }

    public String getPokedex() {
        return getString(resIdKey);
    }

}
