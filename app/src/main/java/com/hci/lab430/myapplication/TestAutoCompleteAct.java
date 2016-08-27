package com.hci.lab430.myapplication;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.TextView;
import android.widget.Toast;

import com.hci.lab430.myapplication.adapter.AutoCompletePokemonNameAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TestAutoCompleteAct extends AppCompatActivity implements ReadJSONTask.OnJSONGotListener, AdapterView.OnItemClickListener, View.OnClickListener{

    HashMap<String,String> nameUrlDict = new HashMap<>();
    String serverDomainName;
    AutoCompleteTextView completeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_auto_complete);
        Button openWebBtn = (Button)findViewById(R.id.openWebPageButton);
        openWebBtn.setOnClickListener(this);
        (new ReadJSONTask(this, this, "allPokeNamesAndUrl.json")).execute();

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

        AutoCompletePokemonNameAdapter arrayAdapterForAutoComplete = new AutoCompletePokemonNameAdapter(this,
                android.R.layout.simple_spinner_dropdown_item,
                pokemonNames);

//        ArrayAdapter<String> arrayAdapterForAutoComplete = new ArrayAdapter<>(this,
//                android.R.layout.simple_spinner_dropdown_item,
//                pokemonNames);

        completeTextView = (AutoCompleteTextView)findViewById(R.id.autoCompletePokemonNameText);
        completeTextView.setThreshold(1);

        completeTextView.setAdapter(arrayAdapterForAutoComplete);
        completeTextView.setOnItemClickListener(this);
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
                Toast.makeText(this, "沒有此神奇寶貝,請重新選擇", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(this, "你還沒選任何的神奇寶貝", Toast.LENGTH_SHORT).show();
        }
    }

}
