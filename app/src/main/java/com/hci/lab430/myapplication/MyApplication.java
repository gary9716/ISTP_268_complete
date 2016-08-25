package com.hci.lab430.myapplication;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.hci.lab430.myapplication.model.OwnedPokemonInfo;
import com.hci.lab430.myapplication.model.PokemonType;
import com.hci.lab430.myapplication.model.SearchPokemonInfo;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.parse.Parse;
import com.parse.ParseObject;

/**
 * Created by lab430 on 16/8/4.
 */
public class MyApplication extends Application {

    //Once a application is started, onCreate would be called.
    @Override
    public void onCreate() {
        super.onCreate();
        ParseObject.registerSubclass(OwnedPokemonInfo.class);

        ParseObject.registerSubclass(SearchPokemonInfo.class);
        ParseObject.registerSubclass(PokemonType.class);
//        Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
//                .enableLocalDataStore()
//                .applicationId("aBriKu0h4EZgnb8Sft9Uv4HyDZHOj01WZQp3jPs1")
//                .clientKey("YJy27NUjuLfJaicKAFReic3gpCFxdemFsPrsQj05")
//                .server("https://parseapi.back4app.com/")
//                .build());

        Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                .enableLocalDataStore()
                .applicationId("d41d8cd98f00b204e9800998ecf8427e")
                .server("http://140.112.30.43:1337/parse")
                .build());


        //init library
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileCount(100)
                .build();

        ImageLoader.getInstance().init(config);

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

//        ParseFacebookUtils.initialize(this);
    }

}
