package com.hci.lab430.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.hci.lab430.myapplication.adapter.PokemonInfoListViewAdapter;
import com.hci.lab430.myapplication.model.OwnedPokemonDataManager;
import com.hci.lab430.myapplication.model.OwnedPokemonInfo;
import com.hci.lab430.myapplication.model.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by lab430 on 16/7/22.
 */
public class PokemonListActivity extends CustomizedActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, DialogInterface.OnClickListener, PokemonInfoListViewAdapter.OnPokemonInfoStateChangeListener {
    PokemonInfoListViewAdapter adapter;
    OwnedPokemonDataManager dataManager;
    MediaPlayer mediaPlayer = null;
    Handler handler;
    AlertDialog deleteActionDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pokemon_list);

        handler = new Handler(getMainLooper());
        mediaPlayer = Utils.loadSongFromAssets(this,"healing_sound2.mp3");
        dataManager = new OwnedPokemonDataManager(this);
        dataManager.loadPokemonTypes();
        dataManager.loadListViewData();

        ArrayList<OwnedPokemonInfo> ownedPokemonInfos = new ArrayList<>();
        ownedPokemonInfos.addAll(dataManager.getOwnedPokemonInfos());

        Intent srcIntent = getIntent();
        int selectedInitPokemonIndex =
                srcIntent.getIntExtra(MainActivity.selectedPokemonIndexKey, 0);
        OwnedPokemonInfo[] initThreePokemonInfos = dataManager.getInitThreePokemonInfos();
        ownedPokemonInfos.add(0, initThreePokemonInfos[selectedInitPokemonIndex]);

        adapter = new PokemonInfoListViewAdapter(this, R.layout.row_view_of_pokemon_list_view, ownedPokemonInfos);
        adapter.stateChangeListener = new WeakReference<PokemonInfoListViewAdapter.OnPokemonInfoStateChangeListener>(this);

        ListView pokemonListView = (ListView)findViewById(R.id.pokemonListView);
        pokemonListView.setAdapter(adapter);
        pokemonListView.setOnItemClickListener(this);
        pokemonListView.setOnItemLongClickListener(this);

        deleteActionDialog = new AlertDialog.Builder(this)
                .setMessage("你確定要丟棄神奇寶貝們嗎？")
                .setTitle("警告")
                .setNegativeButton("取消", this)
                .setPositiveButton("確認", this)
                .setCancelable(false)
                .create();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(adapter.selectedPokemons.size() > 0) {
            getMenuInflater().inflate(R.menu.selected_show_pokemon_list_action_bar_menu, menu);
            return true;
        }
        else {
            return false;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if(itemId == R.id.action_delete) {
            deleteActionDialog.show();
            return true;
        }
        else if(itemId == R.id.action_heal) {
            boolean shouldHeal = false;
            // once the healing button has been pressed,
            // we need to deselect the selected items in list view.
            for(OwnedPokemonInfo ownedPokemonInfo : adapter.selectedPokemons) {
                ownedPokemonInfo.isSelected = false;
                //check whether we need to show the effect
                if(ownedPokemonInfo.getCurrentHP() < ownedPokemonInfo.getMaxHP()) {
                    shouldHeal = true;
                }
            }

            adapter.notifyDataSetChanged();

            if(shouldHeal) {
                mediaPlayer.setVolume(1.0f, 1.0f);
                mediaPlayer.start();

                handler.postDelayed(startHealingTask, 1000);
            }
            else {
                adapter.selectedPokemons.clear();
                invalidateOptionsMenu();
            }

            return true;
        }
        else if(itemId == R.id.action_level_up) {
            return true;
        }

        return false;
    }

    private Runnable startHealingTask = new Runnable() {
        @Override
        public void run() {
            for (OwnedPokemonInfo ownedPokemonInfo : adapter.selectedPokemons) {
                ownedPokemonInfo.isHealing = true;
            }
            adapter.notifyDataSetChanged();
            adapter.selectedPokemons.clear();
            invalidateOptionsMenu();
        }
    };

    private final static int detailActRequestCode = 1;

    private boolean itemIsClickable = true;

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        if(itemIsClickable) { //avoid repeating trigger
            itemIsClickable = false;
            OwnedPokemonInfo ownedPokemonInfo = adapter.getItem(position);
            Intent detailActIntent = new Intent();
            detailActIntent.putExtra(OwnedPokemonInfo.parcelKey, ownedPokemonInfo);
            detailActIntent.setClass(this, PokemonDetailActivity.class);
            startActivityForResult(detailActIntent, detailActRequestCode);
        }

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
        adapter.selectRow(view);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == detailActRequestCode) {
            if(resultCode == PokemonDetailActivity.removeFromList) {
                String nameToRemove = data.getStringExtra(OwnedPokemonInfo.nameKey);
                if(nameToRemove != null && adapter != null) {
                    OwnedPokemonInfo ownedPokemonInfo = adapter.getItemWithName(nameToRemove);
                    if(ownedPokemonInfo != null) {
                        adapter.remove(ownedPokemonInfo);
                        adapter.selectedPokemons.remove(ownedPokemonInfo);
                        Toast.makeText(PokemonListActivity.this, String.format("%s已經被存入電腦中", ownedPokemonInfo.getName()),Toast.LENGTH_LONG).show();
                    }
                }
            }
            else if(resultCode == PokemonDetailActivity.updateData) {
                //also some logic here.

            }

        }


    }

    @Override
    protected void onStop() {
        itemIsClickable = true;
        super.onStop();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        if(dialogInterface.equals(deleteActionDialog)) {
            if (which == AlertDialog.BUTTON_POSITIVE) {
                for(OwnedPokemonInfo ownedPokemonInfo : adapter.selectedPokemons) {
                    adapter.remove(ownedPokemonInfo);
                }
                adapter.selectedPokemons.clear();
                invalidateOptionsMenu();
            } else if (which == AlertDialog.BUTTON_NEGATIVE) {
                Toast.makeText(this, "取消丟棄", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onPokemonInfoSelectedChange(PokemonInfoListViewAdapter adapter) {
        invalidateOptionsMenu();
    }


    @Override
    protected void onDestroy() {
        adapter.releaseAll();
        adapter = null;
        dataManager.releaseAll();
        dataManager = null;
        mediaPlayer = null;
        startHealingTask = null;
        handler = null;
        deleteActionDialog = null;

        super.onDestroy();
    }
}
