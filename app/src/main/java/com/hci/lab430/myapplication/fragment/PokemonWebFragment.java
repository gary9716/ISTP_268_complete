package com.hci.lab430.myapplication.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hci.lab430.myapplication.R;
import com.hci.lab430.myapplication.ReadJSONTask;
import com.hci.lab430.myapplication.adapter.AutoCompletePokemonNameAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by KTChou on 2016/8/28.
 */
public class PokemonWebFragment extends ItemFragment implements ReadJSONTask.OnJSONGotListener, View.OnClickListener, AdapterView.OnItemClickListener{

    public static PokemonWebFragment newInstance() {

        Bundle args = new Bundle();

        PokemonWebFragment fragment = new PokemonWebFragment();
        fragment.setArguments(args);
        return fragment;
    }

    View fragmentView;
    HashMap<String,String> nameUrlDict = new HashMap<>();
    String serverDomainName;
    AutoCompleteTextView completeTextView;
    AutoCompletePokemonNameAdapter arrayAdapterForAutoComplete;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        (new ReadJSONTask(getActivity(), this, "allPokeNamesAndUrl.json")).execute();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(fragmentView == null) {
            fragmentView = inflater.inflate(R.layout.activity_test_auto_complete, null);

            Button openWebBtn = (Button)fragmentView.findViewById(R.id.openWebPageButton);
            openWebBtn.setOnClickListener(this);

            completeTextView = (AutoCompleteTextView)fragmentView.findViewById(R.id.autoCompletePokemonNameText);
            setupCompleteTextView();
        }
        return fragmentView;
    }


    @Override
    public void onJSON(JSONObject jsonObject) {
        ArrayList<String> pokemonNames = new ArrayList<>();
        try {
            serverDomainName = jsonObject.getString("domain");
            JSONArray dataObjs = jsonObject.getJSONArray("data");
            for(int i = 0;i < dataObjs.length();i++) {
                JSONObject dataObj = dataObjs.getJSONObject(i);
                JSONObject chData = dataObj.getJSONObject("ch");
                String pokeName = chData.getString("name");
                pokemonNames.add(pokeName);

                nameUrlDict.put(pokeName, chData.getString("url"));
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            return;
        }

        arrayAdapterForAutoComplete = new AutoCompletePokemonNameAdapter(getActivity(),
                android.R.layout.simple_spinner_dropdown_item,
                pokemonNames);

        if(completeTextView != null)
            setupCompleteTextView();

    }

    void setupCompleteTextView() {
        if(arrayAdapterForAutoComplete != null)
            completeTextView.setAdapter(arrayAdapterForAutoComplete);
        completeTextView.setOnItemClickListener(this);
        completeTextView.setThreshold(1);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        Log.d("WTF", view.toString());
        TextView textView = (TextView)view;
        Log.d("WTF", "你選了:" + textView.getText().toString());

    }

    private void openUrlInBrowser(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }


    @Override
    public void onClick(View v) {
        String pokemonName = completeTextView.getText().toString();
        if(pokemonName != null && !pokemonName.isEmpty()) {
            String url = nameUrlDict.get(pokemonName);
            if(url != null) {
                openUrlInBrowser(serverDomainName + url);
            }
            else {
                Toast.makeText(getActivity(), "沒有此神奇寶貝,請重新選擇", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(getActivity(), "你還沒選任何的神奇寶貝", Toast.LENGTH_SHORT).show();
        }
    }
}
