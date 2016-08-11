package com.hci.lab430.myapplication;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
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

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONObject;

import fr.castorflex.android.circularprogressbar.CircularProgressDrawable;

public class MainActivity extends CustomizedActivity implements View.OnClickListener,RadioGroup.OnCheckedChangeListener,EditText.OnEditorActionListener{

    public final static String selectedPokemonIndexKey = "selectedPokemonIndex";

    public static final String optionSelectedKey = "optionSelectedKey";
    public static final String nameTextKey = "nameTextKey";
    public static final String profileImgUrlKey = "profileImgUrlKey";
    public static final String emailKey = "emailKey";

    TextView infoText;
//    EditText nameEditText;
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

    public enum UISetting {
        Initial,
        DataIsKnown
    }

    Handler uiHandler;
    SharedPreferences preferences;
    UISetting uiSetting;

    LoginButton loginBtn;

    CallbackManager callbackManager;
    AccessToken accessToken;

    public void setupFBLogin() {
        callbackManager = CallbackManager.Factory.create();

        loginBtn.setReadPermissions("public_profile", "email");
        loginBtn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                accessToken = loginResult.getAccessToken();
                sendGraphReq();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
    }

    public void sendGraphReq() {
        if(accessToken != null) {
            GraphRequest request = GraphRequest.newMeRequest(
                    accessToken,
                    new GraphRequest.GraphJSONObjectCallback() {

                        //當RESPONSE回來的時候

                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {

                            //讀出姓名 ID FB個人頁面連結
                            if (response != null) {
                                SharedPreferences preferences = getSharedPreferences(Application.class.getName(), MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                nameOfTheTrainer = object.optString("name");
                                editor.putString(nameTextKey, nameOfTheTrainer);
                                editor.putString(emailKey, object.optString("email"));

//                                Log.d("FB", object.optString("name"));
//                                Log.d("FB", object.optString("email"));
//                                Log.d("FB", object.optString("id"));

                                if (object.has("picture")) {
                                    try {
                                        String profilePicUrl = object.getJSONObject("picture").getJSONObject("data").getString("url");
                                        editor.putString(profileImgUrlKey, profilePicUrl);
                                    } catch (Exception e) {
                                        Log.d("FB", e.getLocalizedMessage());
                                    }
                                }

                                editor.commit();
                            }
                        }
                    });

            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,email,picture.type(large)");
            request.setParameters(parameters);
            request.executeAsync();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("testStage", "onCreate");
        uiHandler = new Handler(getMainLooper());

        setContentView(R.layout.pokemon_welcome_fb_login);

        infoText = (TextView)findViewById(R.id.info_text);

//        nameEditText = (EditText)findViewById(R.id.name_editText);
//        nameEditText.setOnEditorActionListener(this);
//        nameEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);

        confirmBtn = (Button)findViewById(R.id.confirm_btn);
        confirmBtn.setOnClickListener(this);

        AccessToken currentToken;
        currentToken = AccessToken.getCurrentAccessToken();
        if(currentToken != null) {
            Log.d("accessToken", currentToken.getToken());
            accessToken = currentToken;
        }
        else {
            Log.d("accessToken", "no token now");
            SharedPreferences preferences = getSharedPreferences(Application.class.getName(), MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(nameTextKey);
            editor.remove(profileImgUrlKey);
            editor.remove(emailKey);
            editor.commit();

            accessToken = null;
        }

        loginBtn = (LoginButton)findViewById(R.id.login_button);
        setupFBLogin();
        sendGraphReq();

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
        nameOfTheTrainer = preferences.getString(nameTextKey, nameOfTheTrainer);
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
            //hide these UI
//            nameEditText.setVisibility(View.INVISIBLE);
            confirmBtn.setVisibility(View.INVISIBLE);
            optionGroup.setVisibility(View.INVISIBLE);
            loginBtn.setVisibility(View.INVISIBLE);
            //show progress bar
            progressBar.setVisibility(View.VISIBLE);
            //although button is invisible, we can still simulate the button clicked.
            confirmBtn.performClick();
        }
        else {
            //show these UI
//            nameEditText.setVisibility(View.VISIBLE);
            confirmBtn.setVisibility(View.VISIBLE);
            optionGroup.setVisibility(View.VISIBLE);
            loginBtn.setVisibility(View.VISIBLE);
            //hide progress bar
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

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d("testStage","onRestoreInstance");
        selectedOptionIndex = savedInstanceState.getInt(optionSelectedKey, selectedOptionIndex);
        ((RadioButton)optionGroup.getChildAt(selectedOptionIndex)).setChecked(true);

    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if(viewId == R.id.confirm_btn) {
            view.setClickable(false);

            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(optionSelectedKey, selectedOptionIndex);
            editor.commit();

            setInfoTextWithFormat();

            uiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent();
                    intent.putExtra(selectedPokemonIndexKey, selectedOptionIndex);
                    intent.setClass(MainActivity.this, DrawerActivity.class);
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
    protected void onResume() {
        super.onResume();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
