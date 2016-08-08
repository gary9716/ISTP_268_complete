package com.hci.lab430.myapplication.model;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lab430 on 16/7/16.
 */
@ParseClassName("OwningPokemonInfo")
public class OwningPokemonInfo extends ParseObject implements Parcelable{

    public final static int maxNumSkills = 4;
    public static String[] typeNames;

    public final static String parcelKey = "OwningPokemonInfo.parcel";
    public final static String nameKey = "OwningPokemonInfo.name";
    public final static String listImgIdKey = "OwningPokemonInfo.listImgId";
    public final static String listImgKey = "OwningPokemonInfo.listImg";
    public final static String listImgUrlKey = "OwningPokemonInfo.listImgUrl";

    public final static String levelKey = "OwningPokemonInfo.level";
    public final static String currentHPKey = "OwningPokemonInfo.currentHP";
    public final static String maxHPKey = "OwningPokemonInfo.maxHP";
    public final static String detailImgIdKey = "OwningPokemonInfo.detailImgId";
    public final static String detailImgKey = "OwningPokemonInfo.detailImg";
    public final static String type1Key = "OwningPokemonInfo.type1";
    public final static String type2Key = "OwningPokemonInfo.type2";
    public final static String skillKey = "OwningPokemonInfo.skill";

    public boolean isSelected = false;
    public boolean isHealing = false;

//    private int listImgId;
//    private String name;
//    private int level;
//    private int currentHP;
//    private int maxHP;
//
//    //detail info
//    private int detailImgId;
//    private int type_1;
//    private int type_2;

    private String[] skill = new String[maxNumSkills];

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.getName());
        dest.writeInt(this.getLevel());
        dest.writeInt(this.getCurrentHP());
        dest.writeInt(this.getMaxHP());
        dest.writeInt(this.getDetailImgId());
        dest.writeInt(this.getType_1());
        dest.writeInt(this.getType_2());
        dest.writeStringArray(this.getSkill());
    }

    public OwningPokemonInfo() {
        super();
    }

    protected OwningPokemonInfo(Parcel in) {
        super();
        this.setName(in.readString());
        this.setLevel(in.readInt());
        this.setCurrentHP(in.readInt());
        this.setMaxHP(in.readInt());
        this.setDetailImgId(in.readInt());
        this.setType_1(in.readInt());
        this.setType_2(in.readInt());
        this.setSkill(in.createStringArray());
    }

    public static final Parcelable.Creator<OwningPokemonInfo> CREATOR = new Parcelable.Creator<OwningPokemonInfo>() {
        @Override
        public OwningPokemonInfo createFromParcel(Parcel source) {
            return new OwningPokemonInfo(source);
        }

        @Override
        public OwningPokemonInfo[] newArray(int size) {
            return new OwningPokemonInfo[size];
        }
    };

    public int getListImgId() {
        return getInt(listImgIdKey);
    }

    public ParseFile getListImgFile() {
        ParseFile parseFile = getParseFile(listImgKey);
        return parseFile;
    }

    public void setListImgId(int listImgId) {
        put(listImgIdKey, listImgId);
    }

    public void setListImgUrl(String url) {
        put(listImgUrlKey, url);
    }

    public String getListImgUrl() {
        return getString(listImgUrlKey);
    }

    public void setListImgFile(Drawable d) {
        final ParseFile imgFile = new ParseFile(Utils.drawableToBytes(d, Bitmap.CompressFormat.PNG));
        imgFile.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                put(listImgKey, imgFile);
                Log.d("image", "done");
            }
        });
    }

    public String getName() {
        return getString(nameKey);
    }

    public void setName(String name) {
        put(nameKey,name);
    }

    public int getLevel() {
        return getInt(levelKey);
    }

    public void setLevel(int level) {
        put(levelKey,level);
    }

    public int getCurrentHP() {
        return getInt(currentHPKey);
    }

    public void setCurrentHP(int currentHP) {
        put(currentHPKey,currentHP);
    }

    public int getMaxHP() {
        return getInt(maxHPKey);
    }

    public void setMaxHP(int maxHP) {
        put(maxHPKey, maxHP);
    }

    public int getDetailImgId() {
        return getInt(detailImgIdKey);
    }

    public void setDetailImgId(int detailImgId) {
        put(detailImgIdKey, detailImgId);
    }

    public int getType_1() {
        return getInt(type1Key);
    }

    public void setType_1(int type_1) {
        put(type1Key,type_1);
    }

    public int getType_2() {
        return getInt(type2Key);
    }

    public void setType_2(int type_2) {
        put(type2Key,type_2);
    }

    public String[] getSkill() {
        return this.skill;
    }

    public void setSkill(String[] skill) {
        ArrayList<String> skillList = new ArrayList<>(skill.length);
        for(String skillName : skill) {
            skillList.add(skillName);
        }
        put(skillKey, skillList);

        this.skill = skill;
    }

    public static ParseQuery<OwningPokemonInfo> getQuery() {
        return ParseQuery.getQuery(OwningPokemonInfo.class);
    }

    public static final String debug_tag = OwningPokemonInfo.class.getName();
    public static final String localDBTableName = OwningPokemonInfo.class.getName();

    public static void syncToParse() {
        ParseQuery<OwningPokemonInfo> query = OwningPokemonInfo.getQuery();
        query.fromPin(localDBTableName);
        query.findInBackground(new FindCallback<OwningPokemonInfo>() {
            @Override
            public void done(List<OwningPokemonInfo> objects, com.parse.ParseException e) {
                if (e == null) {
                    for (final OwningPokemonInfo pokemonInfo : objects) {
                        pokemonInfo.saveEventually();
                    }
                } else {
                    Log.d(debug_tag,
                            "syncToParse: Error finding pinned owningPokemonInfo: "
                                    + e.getMessage());
                }
            }

        });

    }

    public static void syncToLocalDB(List<OwningPokemonInfo> owningPokemonInfos) {
        //first delete previous record
        OwningPokemonInfo.unpinAllInBackground(OwningPokemonInfo.localDBTableName);
        //and then save with new record
        OwningPokemonInfo.pinAllInBackground(OwningPokemonInfo.localDBTableName, owningPokemonInfos);
    }


}
