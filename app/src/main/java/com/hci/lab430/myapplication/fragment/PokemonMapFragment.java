package com.hci.lab430.myapplication.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.location.LocationListener;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hci.lab430.myapplication.GeoCodingTask;
import com.hci.lab430.myapplication.R;
import com.hci.lab430.myapplication.model.PGMapDataManager;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lab430 on 16/8/15.
 */
public class PokemonMapFragment extends ItemFragment implements OnMapReadyCallback, GeoCodingTask.GeoCodingResponse, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, PGMapDataManager.DataChangedListener{

    public final static int ACCESS_FINE_LOCATION_REQUEST_CODE = 1;

    GoogleMap map;
//    LatLng storeLocation;
    GoogleApiClient googleApiClient;
    LocationRequest locationRequest;

    View fragmentView;
    MapFragment mapFragment;

    PGMapDataManager mapDataManager;
    final String[] gym_types = {"Uncontested", "Mystic", "Valor", "Instinct"};

    public static PokemonMapFragment newInstance() {

        Bundle args = new Bundle();

        PokemonMapFragment fragment = new PokemonMapFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mapDataManager = new PGMapDataManager(getActivity(), 15);
        mapDataManager.dataChangedListener = this;
    }

    //here I demo how to nest a fragment inside anther fragment
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(fragmentView == null) {
            fragmentView = inflater.inflate(R.layout.fragment_map, container, false);
            mapFragment = MapFragment.newInstance();
            mapFragment.getMapAsync(this);
        }
        return fragmentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getChildFragmentManager().beginTransaction().replace(R.id.childFragmentContainer, mapFragment).commit();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        UiSettings mapUISettings = googleMap.getUiSettings();
        mapUISettings.setZoomControlsEnabled(true);
        mapUISettings.setZoomGesturesEnabled(true);

        (new GeoCodingTask(PokemonMapFragment.this)).execute("台北市羅斯福路四段一號");
    }

    @Override
    public void callbackWithGeoCodingResult(LatLng latLng) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("NTU").snippet("National Taiwan University");
        map.moveCamera(cameraUpdate);
        map.addMarker(markerOptions);
    }

    private void createGoogleApiClient()
    {
        if(googleApiClient == null)
        {
            googleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            googleApiClient.connect();

        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission(ACCESS_FINE_LOCATION_REQUEST_CODE);
        }
        else {
            requestLocationUpdateService();
            setMyLocationButtonEnabled();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == ACCESS_FINE_LOCATION_REQUEST_CODE)
        {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                requestLocationUpdateService();
                setMyLocationButtonEnabled();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    @Override
    public void onLocationChanged(Location location) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 17));
    }

    public void requestLocationPermission (int requestCode){
        //below this version we only need to specify it in AndroidManifest
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestCode);
        }
    }

    public void requestLocationUpdateService() {
        if(locationRequest == null) {
            locationRequest = new LocationRequest();
            locationRequest.setInterval(1000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }

        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
    }

    public void setMyLocationButtonEnabled() {
        // Enables/disables the my location button (this DOES NOT enable/disable the my location
        // dot/chevron on the map). The my location button will never appear if the my location
        // layer is not enabled.
        // First verify that the location permission has been granted.
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.getUiSettings().setMyLocationButtonEnabled(true);
            map.setMyLocationEnabled(true);
        } else {
            requestLocationPermission(ACCESS_FINE_LOCATION_REQUEST_CODE);
        }
    }

    LatLng getLocDataFromJsonObj(JSONObject jsonObject) {
        try {
            return new LatLng(jsonObject.getDouble("latitude"),jsonObject.getDouble("longitude"));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private class SetMapMarkerWithBitmapLoadedFromUrl implements ImageLoadingListener{

        private Marker markerToSet;
        private float mImgScale;

        public SetMapMarkerWithBitmapLoadedFromUrl(Marker marker, float imgScale) {
            markerToSet = marker;
            mImgScale = imgScale;
        }

        @Override
        public void onLoadingStarted(String imageUri, View view) {

        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(loadedImage, (int)(loadedImage.getWidth() * mImgScale), (int)(loadedImage.getHeight() * mImgScale), false);
            markerToSet.setIcon(BitmapDescriptorFactory.fromBitmap(scaledBitmap));
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {

        }
    }

    @Override
    public void onData(JSONObject jsonObject) {
        //parse jsonObj
        Log.d("onData", "test");
        if(map == null)
            return;

        map.clear();
        try {
            JSONArray gymObjs = jsonObject.getJSONArray("gyms");
            //"keys:"
            //"guard_pokemon_id"
            //"team_id"
            for(int i = 0;i < gymObjs.length();i++) {
                JSONObject gymObj = gymObjs.getJSONObject(i);
                int teamId = gymObj.getInt("team_id");
                Marker marker = map.addMarker(new MarkerOptions()
                                .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                                .position(getLocDataFromJsonObj(gymObj))
                                .title("gym")
                                .snippet("owned by Team " + gym_types[teamId])
                );
                //load image with url
                ImageLoader.getInstance().loadImage(PGMapDataManager.ImgServerAddr + "/forts/" + gym_types[teamId] + ".png",
                        new SetMapMarkerWithBitmapLoadedFromUrl(marker,2));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            JSONArray pokemonObjs = jsonObject.getJSONArray("pokemons");
            for(int i = 0;i < pokemonObjs.length();i++) {
                JSONObject pokemonObj = pokemonObjs.getJSONObject(i);
                //keys:
                //"pokemon_id"
                //"pokemon_name"
                Marker marker = map.addMarker(new MarkerOptions()
                                .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                                .position(getLocDataFromJsonObj(pokemonObj))
                                .title(pokemonObj.getString("pokemon_name"))
                );
                ImageLoader.getInstance().loadImage(PGMapDataManager.ImgServerAddr + "/icons/" + pokemonObj.getInt("pokemon_id") + ".png" ,
                        new SetMapMarkerWithBitmapLoadedFromUrl(marker,1));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //memory management

    @Override
    public void onDestroy() {
        fragmentView = null;
        mapFragment = null;
        mapDataManager.releaseAll();
        super.onDestroy();
    }
}
