package com.hci.lab430.myapplication.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lab430 on 16/7/16.
 */
@ParseClassName("PokemonInfo")
public class OwnedPokemonInfo extends ParseObject implements Parcelable{

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

    public OwnedPokemonInfo() {
        super();
    }

    protected OwnedPokemonInfo(Parcel in) {
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

    public static final Parcelable.Creator<OwnedPokemonInfo> CREATOR = new Parcelable.Creator<OwnedPokemonInfo>() {
        @Override
        public OwnedPokemonInfo createFromParcel(Parcel source) {
            return new OwnedPokemonInfo(source);
        }

        @Override
        public OwnedPokemonInfo[] newArray(int size) {
            return new OwnedPokemonInfo[size];
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
        if(!skillHaveBeenInited) {
            skillHaveBeenInited = true;
            this.skill = readSkillFromParseStorage();
        }
        else if(skillHaveBeenModified) {
            skillHaveBeenModified = false;
            this.skill = readSkillFromParseStorage();
        }
        return this.skill;
    }

    private String[] readSkillFromParseStorage() {
        ArrayList<String> skillList = (ArrayList)get(skillKey);
        String[] skillArray = new String[maxNumSkills];
        if(skillList != null) {
            for (int i = 0; i < skillList.size(); i++) {
                skillArray[i] = skillList.get(i);
            }
        }
        return skillArray;
    }

    private boolean skillHaveBeenInited = false;
    private boolean skillHaveBeenModified = false;

    public void setSkill(String[] skill) {
        ArrayList<String> skillList = new ArrayList<>(skill.length);
        for(String skillName : skill) {
            if(skillName != null)
                skillList.add(skillName);
        }
        put(skillKey, skillList);

        this.skill = skill;
        skillHaveBeenModified = true;
    }

    public static ParseQuery<OwnedPokemonInfo> getQuery() {
        return ParseQuery.getQuery(OwnedPokemonInfo.class);
    }

    public static final String debug_tag = OwnedPokemonInfo.class.getName();
    public static final String localDBTableName = OwnedPokemonInfo.class.getName();

    public static void initTable(final ArrayList<OwnedPokemonInfo> ownedPokemonInfos) {

        OwnedPokemonInfo.getQuery().fromPin(localDBTableName).findInBackground(new FindCallback<OwnedPokemonInfo>() {
            @Override
            public void done(List<OwnedPokemonInfo> objects, ParseException e) {
                final ArrayList<OwnedPokemonInfo> newOwnedPokemonInfos = ownedPokemonInfos;
                OwnedPokemonInfo.unpinAllInBackground(localDBTableName);
                syncToDB(newOwnedPokemonInfos);
            }
        });

    }

    public static void syncToDB(List<OwnedPokemonInfo> ownedPokemonInfos) {
        //save with new record
        OwnedPokemonInfo.pinAllInBackground(OwnedPokemonInfo.localDBTableName, ownedPokemonInfos);
    }


}
