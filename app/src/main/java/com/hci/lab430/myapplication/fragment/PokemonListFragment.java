package com.hci.lab430.myapplication.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
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
import com.hci.lab430.myapplication.PokemonListActivity;
import com.hci.lab430.myapplication.R;
import com.hci.lab430.myapplication.adapter.PokemonInfoListViewAdapter;
import com.hci.lab430.myapplication.model.OwningPokemonDataManager;
import com.hci.lab430.myapplication.model.PokemonInfo;
import com.hci.lab430.myapplication.model.Utils;

import java.util.ArrayList;

/**
 * Created by lab430 on 16/8/4.
 */
public class PokemonListFragment extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, DialogInterface.OnClickListener{

    PokemonInfoListViewAdapter adapter;
    OwningPokemonDataManager dataManager;
    MediaPlayer mediaPlayer = null;
    Handler handler;
    AlertDialog deleteActionDialog;
    Activity activity = getActivity();
    ArrayList<PokemonInfo> pokemonInfos;

    public static Fragment newInstance() {
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
        pokemonInfos = dataManager.getPokemonInfos();

        Intent srcIntent = activity.getIntent();
        int selectedInitPokemonIndex =
                srcIntent.getIntExtra(MainActivity.selectedPokemonIndexKey, 0);
        PokemonInfo[] initThreePokemonInfos = dataManager.getInitThreePokemonInfos();
        pokemonInfos.add(0, initThreePokemonInfos[selectedInitPokemonIndex]);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.pokemon_list, container, false);

        adapter = new PokemonInfoListViewAdapter(activity, R.layout.row_view_of_pokemon_list_view, pokemonInfos);
        ListView pokemonListView = (ListView)rootView.findViewById(R.id.pokemonListView);
        pokemonListView.setAdapter(adapter);
        pokemonListView.setOnItemClickListener(this);
        pokemonListView.setOnItemLongClickListener(this);

        deleteActionDialog = new AlertDialog.Builder(activity)
                .setMessage("你確定要丟棄神奇寶貝們嗎？")
                .setTitle("警告")
                .setNegativeButton("取消", this)
                .setPositiveButton("確認", this)
                .setCancelable(false)
                .create();

        setHasOptionsMenu(true);

        return rootView;

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
        super.onStop();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        if(dialogInterface.equals(deleteActionDialog)) {
            if (which == AlertDialog.BUTTON_POSITIVE) {
                for(PokemonInfo pokemonInfo : adapter.selectedPokemons) {
                    adapter.remove(pokemonInfo);
                }
//                invalidateOptionsMenu();
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
                String nameToRemove = data.getStringExtra(PokemonInfo.nameKey);
                if(nameToRemove != null) {
                    PokemonInfo pokemonInfo = adapter.getItemWithName(nameToRemove);
                    if(pokemonInfo != null) {
                        adapter.remove(pokemonInfo);
                        Toast.makeText(activity, String.format("%s已經被存入電腦中", pokemonInfo.getName()),Toast.LENGTH_LONG).show();
                    }
                }
            }
            else if(resultCode == PokemonDetailActivity.updateData) {
                //also some logic here.

            }

        }
    }

//    public void changeOptionsMenuVisibility(boolean show) {
//        setHasOptionsMenu(show);
//    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.pokemon_list_action_bar_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
