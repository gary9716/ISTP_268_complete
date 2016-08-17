package com.hci.lab430.myapplication.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.hci.lab430.myapplication.MainActivity;
import com.hci.lab430.myapplication.PokemonDetailActivity;
import com.hci.lab430.myapplication.R;
import com.hci.lab430.myapplication.adapter.PokemonInfoListViewAdapter;
import com.hci.lab430.myapplication.model.OwningPokemonDataManager;
import com.hci.lab430.myapplication.model.OwningPokemonInfo;
import com.hci.lab430.myapplication.model.Utils;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lab430 on 16/8/4.
 */
public class PokemonListFragment extends ItemFragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, DialogInterface.OnClickListener, PokemonInfoListViewAdapter.OnPokemonInfoStateChangeListener, FindCallback<OwningPokemonInfo> {

    AlertDialog deleteActionDialog;
    View fragmentView;
    Activity activity;

    public PokemonInfoListViewAdapter adapter;
    OwningPokemonDataManager dataManager;
    MediaPlayer mediaPlayer = null;
    Handler handler;

    ArrayList<OwningPokemonInfo> owningPokemonInfos;

    public static PokemonListFragment newInstance() {
        PokemonListFragment fragment = new PokemonListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        handler = new Handler(activity.getMainLooper());
        mediaPlayer = Utils.loadSongFromAssets(activity, "healing_sound2.mp3");
        dataManager = new OwningPokemonDataManager(activity);
        dataManager.loadPokemonTypes();

        owningPokemonInfos = new ArrayList<>();
        prepareListViewData();

        adapter = new PokemonInfoListViewAdapter(activity, R.layout.row_view_of_pokemon_list_view, owningPokemonInfos);
        adapter.stateChangeListener = new WeakReference<PokemonInfoListViewAdapter.OnPokemonInfoStateChangeListener>(this);

        deleteActionDialog = new AlertDialog.Builder(activity)
                .setMessage("你確定要丟棄神奇寶貝們嗎？")
                .setTitle("警告")
                .setNegativeButton("取消", this)
                .setPositiveButton("確認", this)
                .setCancelable(false)
                .create();

    }

    private final static String recordIsInDBKey = "recordIsInDB";

    public void prepareListViewData() {
        SharedPreferences preferences = activity.getSharedPreferences(Application.class.getName(), activity.MODE_PRIVATE);
        boolean recordIsInDB = preferences.getBoolean(recordIsInDBKey, false);

        if(!recordIsInDB) {
            loadFromCSV();
            OwningPokemonInfo.initTable(owningPokemonInfos);
            preferences.edit().putBoolean(recordIsInDBKey, true).commit();
        }
        else {
            ParseQuery<OwningPokemonInfo> query = OwningPokemonInfo.getQuery();
            query.fromPin(OwningPokemonInfo.localDBTableName).findInBackground(this); //query from local
        }

    }

    private void loadFromCSV() {
        dataManager.loadListViewData();
        ArrayList<OwningPokemonInfo> dataArray = dataManager.getOwningPokemonInfos();
        for(OwningPokemonInfo owningPokemonInfo : dataArray) {
            owningPokemonInfos.add(owningPokemonInfo);
        }

        Intent srcIntent = activity.getIntent();
        int selectedInitPokemonIndex =
                srcIntent.getIntExtra(MainActivity.selectedPokemonIndexKey, 0);
        OwningPokemonInfo[] initThreePokemonInfos = dataManager.getInitThreePokemonInfos();
        owningPokemonInfos.add(0, initThreePokemonInfos[selectedInitPokemonIndex]);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);

        if(fragmentView == null) {
            fragmentView = inflater.inflate(R.layout.pokemon_list, container, false);

            ListView pokemonListView = (ListView) fragmentView.findViewById(R.id.pokemonListView);
            pokemonListView.setAdapter(adapter);
            pokemonListView.setOnItemClickListener(this);
            pokemonListView.setOnItemLongClickListener(this);

            setHasOptionsMenu(true);
            setMenuVisibility(true);
        }

        return fragmentView;
    }

    private final static int detailActRequestCode = 1;
    private boolean itemIsClickable = true;

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        if(itemIsClickable) { //avoid repeating trigger
            itemIsClickable = false;
            OwningPokemonInfo owningPokemonInfo = adapter.getItem(position);
            Intent detailActIntent = new Intent();
            detailActIntent.putExtra(OwningPokemonInfo.parcelKey, owningPokemonInfo);
            detailActIntent.setClass(activity, PokemonDetailActivity.class);
            startActivityForResult(detailActIntent, detailActRequestCode);
        }

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
        adapter.selectRow(view);
        return true;
    }

    @Override
    public void onStop() {
        itemIsClickable = true;
        OwningPokemonInfo.syncToDB(owningPokemonInfos);
        super.onStop();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        if(dialogInterface.equals(deleteActionDialog)) {
            if (which == AlertDialog.BUTTON_POSITIVE) {
                for(OwningPokemonInfo owningPokemonInfo : adapter.selectedPokemons) {
                    removePokemonInfo(owningPokemonInfo);
                }
                clearSelectedPokemons();
            } else if (which == AlertDialog.BUTTON_NEGATIVE) {
                Toast.makeText(activity, "取消丟棄", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == detailActRequestCode) {
            if(resultCode == PokemonDetailActivity.removeFromList) {
                String nameToRemove = data.getStringExtra(OwningPokemonInfo.nameKey);
                if(nameToRemove != null) {
                    OwningPokemonInfo owningPokemonInfo = adapter.getItemWithName(nameToRemove);
                    if(owningPokemonInfo != null) {
                        removePokemonInfo(owningPokemonInfo);
                        adapter.selectedPokemons.remove(owningPokemonInfo); //in case it was selected
                        Toast.makeText(activity, String.format("%s已經被存入電腦中", owningPokemonInfo.getName()),Toast.LENGTH_LONG).show();
                    }
                }
            }
            else if(resultCode == PokemonDetailActivity.updateData) {
                //also some logic here.

            }

        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(adapter.selectedPokemons.size() != 0) {
            inflater.inflate(R.menu.selected_show_pokemon_list_action_bar_menu, menu);
        }
        else {
            inflater.inflate(R.menu.default_pokemon_list_action_bar_menu, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if(itemId == R.id.action_delete) {
            if(adapter.selectedPokemons.size() != 0) {
                deleteActionDialog.show();
            }
            return true;
        }
        else if(itemId == R.id.action_heal) {
            boolean shouldHeal = false;
            // once the healing button has been pressed,
            // we need to deselect the selected items in list view.
            for(OwningPokemonInfo owningPokemonInfo : adapter.selectedPokemons) {
                owningPokemonInfo.isSelected = false;
                //check whether we need to show the effect
                if(owningPokemonInfo.getCurrentHP() < owningPokemonInfo.getMaxHP()) {
                    shouldHeal = true;
                }

            }
            adapter.notifyDataSetChanged();

            if(shouldHeal) {
                mediaPlayer.setVolume(1.0f, 1.0f);
                mediaPlayer.start();

                handler.postDelayed(startHealingEffect, 1000);
            }
            else {
                clearSelectedPokemons();
            }
            return true;
        }
        else if(itemId == R.id.action_level_up) {
            for(OwningPokemonInfo owningPokemonInfo : adapter.selectedPokemons) {
                owningPokemonInfo.setLevel(owningPokemonInfo.getLevel() + 1);
                owningPokemonInfo.isSelected = false;
            }
            adapter.notifyDataSetChanged();
            clearSelectedPokemons();

            return true;
        }
        else if(itemId == R.id.action_sync) {
            OwningPokemonInfo.syncToDB(owningPokemonInfos);
        }
        return false;
    }

    Runnable startHealingEffect = new Runnable() {
        @Override
        public void run() {
            for (OwningPokemonInfo owningPokemonInfo : adapter.selectedPokemons) {
                owningPokemonInfo.isHealing = true;
            }
            adapter.notifyDataSetChanged();
            clearSelectedPokemons();
        }
    };

    @Override
    public void onPokemonInfoSelectedChange(PokemonInfoListViewAdapter adapter) {
        getActivity().invalidateOptionsMenu();
    }


    private void clearSelectedPokemons() {
        adapter.selectedPokemons.clear();
        getActivity().invalidateOptionsMenu();
    }

    public void removePokemonInfo(OwningPokemonInfo pokemonInfo) {
        if(adapter != null)
            adapter.remove(pokemonInfo);

        //and remove from database
        pokemonInfo.unpinInBackground(OwningPokemonInfo.localDBTableName); //remove from local

    }

    @Override
    public void done(List<OwningPokemonInfo> objects, ParseException e) {
        if (e == null) {
            owningPokemonInfos.clear();
            for (OwningPokemonInfo owningPokemonInfo : objects) {
                owningPokemonInfos.add(owningPokemonInfo);
            }
            Log.d(OwningPokemonInfo.debug_tag, "finishing loading from local DB");
        } else {
            Log.d(OwningPokemonInfo.debug_tag, "fail to load from local DB");
        }

        if (adapter != null)
            adapter.notifyDataSetChanged();
    }

    //memory management
    @Override
    public void onDestroy() {
        super.onDestroy();
        deleteActionDialog = null;
        fragmentView = null;
        activity = null;

        adapter.releaseAll();
        adapter = null;
        startHealingEffect = null;
        handler = null;
        mediaPlayer = null;
        dataManager.releaseAll();
        dataManager = null;

        owningPokemonInfos.clear();
        owningPokemonInfos = null;
    }

}
