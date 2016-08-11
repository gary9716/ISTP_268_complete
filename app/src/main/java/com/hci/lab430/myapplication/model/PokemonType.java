package com.hci.lab430.myapplication.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;

/**
 * Created by lab430 on 16/8/11.
 */

@ParseClassName("PokemonType")
public class PokemonType extends ParseObject {
    public final static String typeArrayKey = "all";

    public ArrayList<String> getTypeArray() {
        return (ArrayList<String>)get(typeArrayKey);
    }

    public static ParseQuery<PokemonType> getQuery() {
        return ParseQuery.getQuery(PokemonType.class);
    }

}
