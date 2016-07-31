package com.hci.lab430.myapplication;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import fr.castorflex.android.circularprogressbar.CircularProgressDrawable;

public class MainActivity extends CustomizedActivity implements View.OnClickListener,RadioGroup.OnCheckedChangeListener,EditText.OnEditorActionListener{

    public final static String selectedPokemonIndexKey = "selectedPokemonIndex";

    TextView infoText;
    EditText nameEditText;
    RadioGroup optionGroup;
    Button confirmBtn;
    int selectedOptionIndex = 0;
    String nameOfTheTrainer = null;
    int changeActivityInSecs = 5;

    ProgressBar progressBar;

    String[] pokemonNames = new String[]{
            "小火龍",
            "傑尼龜",
            "妙蛙種子"
    };

    Handler uiHandler;
    SharedPreferences preferences;
    public final String optionSelectedKey = "optionSelectedKey";
    public final String nameEditTextKey = "nameTextKey";

    public enum UISetting {
        Initial,
        DataIsKnown
    }

    UISetting uiSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("testStage", "onCreate");
        uiHandler = new Handler(getMainLooper());

        setContentView(R.layout.pokemon_welcome);

        infoText = (TextView)findViewById(R.id.info_text);

        nameEditText = (EditText)findViewById(R.id.name_editText);
        nameEditText.setOnEditorActionListener(this);
        nameEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);

        confirmBtn = (Button)findViewById(R.id.confirm_btn);
        confirmBtn.setOnClickListener(this);

        optionGroup = (RadioGroup) findViewById(R.id.option_radioGrp);
        optionGroup.setOnCheckedChangeListener(this);

        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        progressBar.setIndeterminateDrawable(new CircularProgressDrawable
                .Builder(this)
                .colors(getResources().getIntArray(R.array.gplus_colors))
                .sweepSpeed(1f)
                .strokeWidth(8f)
                .build());

        //we save the preference data with Application name as the key
        preferences = getSharedPreferences(Application.class.getName(), MODE_PRIVATE);

        selectedOptionIndex = preferences.getInt(optionSelectedKey, selectedOptionIndex);
        //initial value of nameOfTheTrainer is null
        nameOfTheTrainer = preferences.getString(nameEditTextKey, nameOfTheTrainer);
        if(nameOfTheTrainer == null) {
            uiSetting = UISetting.Initial;
        }
        else {
            Log.d("testPreference","data is known");
            uiSetting = UISetting.DataIsKnown;
        }

        changeUIAccordingToRecord();
    }


    private void changeUIAccordingToRecord() {
        if(uiSetting == UISetting.DataIsKnown) {
            nameEditText.setVisibility(View.INVISIBLE);
            confirmBtn.setVisibility(View.INVISIBLE);
            optionGroup.setVisibility(View.INVISIBLE);

            //although button is invisible, we can still simulate the button clicked.
            confirmBtn.performClick();
        }
        else {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void setInfoTextWithFormat() {
        if(uiSetting == UISetting.Initial) { //first time
            infoText.setText(String.format("你好, 訓練家%s 歡迎來到神奇寶貝的世界 你的第一個夥伴是%s, 冒險將於%d秒後開始", nameOfTheTrainer, pokemonNames[selectedOptionIndex], changeActivityInSecs));
        }
        else {
            infoText.setText(String.format("你好, 訓練家%s 歡迎回到神奇寶貝的世界 你的第一個夥伴是%s, 冒險將於%d秒後繼續", nameOfTheTrainer, pokemonNames[selectedOptionIndex], changeActivityInSecs));
        }

    }

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
        selectedOptionIndex = savedInstanceState.getInt(optionSelectedKey, selectedOptionIndex);
        ((RadioButton)optionGroup.getChildAt(selectedOptionIndex)).setChecked(true);
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
            if(uiSetting == UISetting.Initial) {
                //get the name from editText and save the data into SharePreference
                nameOfTheTrainer = nameEditText.getText().toString();
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(nameEditTextKey, nameOfTheTrainer);
                editor.putInt(optionSelectedKey, selectedOptionIndex);
                editor.commit();
            }

            setInfoTextWithFormat();
            uiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent();
                    intent.putExtra(selectedPokemonIndexKey, selectedOptionIndex);
                    intent.setClass(MainActivity.this, PokemonListActivity.class);
                    startActivity(intent);
                    MainActivity.this.finish();
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

    @Override
    protected void onStop() {
        confirmBtn.setClickable(true);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("testStage", "onDestroy");
    }
}
