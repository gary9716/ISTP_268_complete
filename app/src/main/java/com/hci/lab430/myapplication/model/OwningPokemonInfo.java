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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lab430 on 16/7/16.
 */
@ParseClassName("OwningPokemonInfo")
public class OwningPokemonInfo extends ParseObject implements Parcelable{

    public final static int maxNumSkills = 4;
    public static String[] typeNames;

    public final static String parcelKey = "parcel";
    public final static String nameKey = "name";
    public final static String listImgIdKey = "listImgId";
    public final static String listImgUrlKey = "listImgUrl";

    public final static String levelKey = "level";
    public final static String currentHPKey = "currentHP";
    public final static String maxHPKey = "maxHP";
    public final static String detailImgIdKey = "detailImgId";
    public final static String type1Key = "type1";
    public final static String type2Key = "type2";
    public final static String skillKey = "skill";

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

    public void setListImgId(int listImgId) {
        put(listImgIdKey, listImgId);
    }

    public void setListImgUrl(String url) {
        put(listImgUrlKey, url);
    }

    public String getListImgUrl() {
        return getString(listImgUrlKey);
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

    public static void initTable(final ArrayList<OwningPokemonInfo> owningPokemonInfos) {

        OwningPokemonInfo.getQuery().fromPin(localDBTableName).findInBackground(new FindCallback<OwningPokemonInfo>() {
            @Override
            public void done(List<OwningPokemonInfo> objects, ParseException e) {
                final ArrayList<OwningPokemonInfo> newOwningPokemonInfos = owningPokemonInfos;
                OwningPokemonInfo.unpinAllInBackground(localDBTableName);
                syncToDB(newOwningPokemonInfos);
            }
        });

    }

    public static void syncToDB(List<OwningPokemonInfo> owningPokemonInfos) {
        //save with new record
        OwningPokemonInfo.pinAllInBackground(OwningPokemonInfo.localDBTableName, owningPokemonInfos);
    }


}
