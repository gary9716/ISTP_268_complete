package com.hci.lab430.myapplication;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.hci.lab430.myapplication.adapter.PokemonInfoListViewAdapter;
import com.hci.lab430.myapplication.model.OwningPokemonDataManager;
import com.hci.lab430.myapplication.model.PokemonInfo;

import java.util.ArrayList;

/**
 * Created by lab430 on 16/7/22.
 */
public class PokemonListActivity extends CustomizedActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener{
    PokemonInfoListViewAdapter adapter;
    OwningPokemonDataManager dataManager;
    MediaPlayer mediaPlayer = null;
    Handler handler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pokemon_list);
        handler = new Handler(getMainLooper());
        loadSongFromAssets("healing_sound2.mp3");

        dataManager = new OwningPokemonDataManager(this);
        ArrayList<PokemonInfo> pokemonInfos = dataManager.getPokemonInfos();

        int selectedInitPokemonIndex = getIntent().getIntExtra(MainActivity.selectedPokemonIndexKey, 0);
        PokemonInfo[] initThreePokemonInfos = dataManager.getInitThreePokemonInfos();
        pokemonInfos.add(0, initThreePokemonInfos[selectedInitPokemonIndex]);

        adapter = new PokemonInfoListViewAdapter(this, R.layout.row_view_of_pokemon_list_view, pokemonInfos);

        ListView pokemonListView = (ListView)findViewById(R.id.pokemonListView);
        pokemonListView.setAdapter(adapter);
        pokemonListView.setOnItemClickListener(this);
        pokemonListView.setOnItemLongClickListener(this);

    }

    private void loadSongFromAssets(String fileName) {
        try {
            AssetFileDescriptor descriptor;
            descriptor = getAssets().openFd(fileName);
            if(descriptor != null) {
                mediaPlayer = new MediaPlayer();

                long start = descriptor.getStartOffset();
                long end = descriptor.getLength();

                mediaPlayer.setDataSource(descriptor.getFileDescriptor(), start, end);
                mediaPlayer.prepareAsync();
            }

        }
        catch(Exception e) {
            mediaPlayer = null;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(adapter.selectedPokemons.size() > 0) {
            getMenuInflater().inflate(R.menu.pokemon_list_action_bar_menu, menu);
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

            for(PokemonInfo pokemonInfo : adapter.selectedPokemons) {
                adapter.remove(pokemonInfo);
            }
            adapter.selectedPokemons.clear();
            invalidateOptionsMenu();

            return true;
        }
        else if(itemId == R.id.action_heal) {
            boolean shouldHeal = false;
            // once the healing button has been pressed,
            // we need to deselect the selected items in list view.
            for(PokemonInfo pokemonInfo : adapter.selectedPokemons) {
                pokemonInfo.isSelected = false;
                //check whether we need to show the effect
                if(pokemonInfo.currentHP < pokemonInfo.maxHP) {
                    shouldHeal = true;
                }

            }
            adapter.notifyDataSetChanged();

            if(shouldHeal) {
                mediaPlayer.setVolume(1.0f, 1.0f);
                mediaPlayer.start();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        for (PokemonInfo pokemonInfo : adapter.selectedPokemons) {
                            pokemonInfo.isHealing = true;
                        }
                        adapter.notifyDataSetChanged();
                        adapter.selectedPokemons.clear();
                        invalidateOptionsMenu();
                    }
                }, 1000);
            }
            return true;
        }
        else if(itemId == R.id.action_settings) {

            return true;
        }

        return false;
    }

    private final static int detailActRequestCode = 1;

    private boolean itemIsClickable = true;

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        if(itemIsClickable) { //avoid repeating trigger
            itemIsClickable = false;
            PokemonInfo pokemonInfo = adapter.getItem(position);
            Intent detailActIntent = new Intent();
            detailActIntent.putExtra(PokemonInfo.parcelKey, pokemonInfo);
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
                String nameToRemove = data.getStringExtra(PokemonInfo.nameKey);
                if(nameToRemove != null) {
                    PokemonInfo pokemonInfo = adapter.getItemWithName(nameToRemove);
                    if(pokemonInfo != null) {
                        adapter.remove(pokemonInfo);
                        Toast.makeText(PokemonListActivity.this, String.format("%s已經被存入電腦中",pokemonInfo.name),Toast.LENGTH_LONG).show();
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


}
