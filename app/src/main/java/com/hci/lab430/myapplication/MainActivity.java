package com.hci.lab430.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,RadioGroup.OnCheckedChangeListener,EditText.OnEditorActionListener{
    TextView infoText;
    EditText nameEditText;
    RadioGroup optionGroup;
    Button confirmBtn;
    int selectedOptionIndex = 0;

    String[] pokemonNames = new String[]{
            "小火龍",
            "傑尼龜",
            "妙蛙種子"
    };

    Handler uiHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("testStage","onCreate");
        uiHandler = new Handler(getMainLooper());

        setContentView(R.layout.pokemon_welcome);

        infoText = (TextView)findViewById(R.id.info_text);

        nameEditText = (EditText)findViewById(R.id.name_editText);
        nameEditText.setOnEditorActionListener(this);

        confirmBtn = (Button)findViewById(R.id.confirm_btn);
        confirmBtn.setOnClickListener(this);

        optionGroup = (RadioGroup) findViewById(R.id.option_radioGrp);
        optionGroup.setOnCheckedChangeListener(this);


    }

    private final String optionSelectedKey = "optionSelectedKey";
    private final String nameEditTextKey = "nameTextKey";

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("testStage","onSaveInstance");
        outState.putInt(optionSelectedKey, selectedOptionIndex);
        String nameStr = nameEditText.getText().toString();
        if(!nameStr.isEmpty())
            outState.putString(nameEditTextKey, nameEditText.getText().toString());


    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d("testStage","onRestoreInstance");
        String nameStr = savedInstanceState.getString(nameEditTextKey, null);
        if(nameStr != null) {
            nameEditText.setText(nameStr);
        }

    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if(viewId == R.id.confirm_btn) {
            view.setClickable(false);
            int changeActivityInSecs = 3;
            infoText.setText(String.format("你好, 訓練家%s 歡迎來到神奇寶貝的世界 你的第一個夥伴是%s, 冒險將於%d秒後開始", nameEditText.getText(), pokemonNames[selectedOptionIndex], changeActivityInSecs));
            uiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, PokemonListActivity.class);
                    startActivity(intent);
                }
            }, changeActivityInSecs * 1000);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkId) {
        int radioGrpId = radioGroup.getId();
        if(radioGrpId == optionGroup.getId()) {
            switch(checkId) {
                case R.id.option1:
                    selectedOptionIndex = 0;
                    break;
                case R.id.option2:
                    selectedOptionIndex = 1;
                    break;
                case R.id.option3:
                    selectedOptionIndex = 2;
                    break;
            }
        }
    }


    @Override
    public boolean onEditorAction(TextView textView,
                                  int actionId,
                                  KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            //dismiss virtual keyboard
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);

            imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);

            //simulate button clicked
            confirmBtn.performClick();
            return true;
        }
        return false;

    }


}
