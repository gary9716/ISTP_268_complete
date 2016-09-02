package com.hci.lab430.myapplication.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
//import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hci.lab430.myapplication.GeoCodingTask;
import com.hci.lab430.myapplication.R;
import com.hci.lab430.myapplication.model.MarkerExtraInfo;
import com.hci.lab430.myapplication.model.PGMapDataManager;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by lab430 on 16/8/15.
 */
public class PokemonMapFragment extends ItemFragment implements OnMapReadyCallback, GeoCodingTask.GeoCodingResponse, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, PGMapDataManager.DataChangedListener, GoogleMap.OnMarkerClickListener {

    public final static int ACCESS_FINE_LOCATION_REQUEST_CODE = 1;

    View fragmentView;
    SupportMapFragment mapFragment;

    PGMapDataManager mapDataManager;
    final String[] gym_types = {"Uncontested", "Mystic", "Valor", "Instinct"};

    GoogleMap map;
    LatLng currentLocation = null;
    GoogleApiClient googleApiClient;
    LocationRequest locationRequest;
    BitmapDescriptor selectedBitmapDescriptor = null;

    private boolean markerSelectingMode = false;
    ArrayList<Marker> currentMarkers = new ArrayList<>();
    ArrayList<Marker> selectedMarkers = new ArrayList<>();

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
        if (fragmentView == null) {
            fragmentView = inflater.inflate(R.layout.fragment_map, container, false);
            mapFragment = SupportMapFragment.newInstance();
            mapFragment.getMapAsync(this);
            setHasOptionsMenu(true);
            setMenuVisibility(true);
        }

        return fragmentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //if we want to nest a fragment inside a fragment, we should use ChildFragmentManager instead of FragmentManager
        getChildFragmentManager().beginTransaction()
                .replace(R.id.childFragmentContainer, mapFragment)
                .commit();


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.map_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_selecting_mode_switch) {
            markerSelectingMode = !markerSelectingMode;
            if (markerSelectingMode) { //become selecting mode
                item.setTitle("一般模式");
            } else {
                //recover all routing markers
                for (Marker marker : selectedMarkers) {
                    changeMarkerSelectedState(marker);
                }
                selectedMarkers.clear();
                item.setTitle("選取模式");
            }
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

    @Override
    public void callbackWithGeoCodingResult(LatLng latLng) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title("NTU")
                .snippet("National Taiwan University");
        map.moveCamera(cameraUpdate);
        map.addMarker(markerOptions);

        createGoogleApiClient();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMarkerClickListener(this);

        //switch the image of a marker to this image if user click a marker
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pokemon_selected_marker);
        bitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * 0.2f), (int) (bitmap.getHeight() * 0.2f), false);
        selectedBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);

        UiSettings mapUISettings = googleMap.getUiSettings();
        mapUISettings.setZoomControlsEnabled(true);

        (new GeoCodingTask(PokemonMapFragment.this)).execute("台北市羅斯福路四段一號");
    }

    private void createGoogleApiClient() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            googleApiClient.connect();
        }
    }

    private void doAfterPermissionGranted() {
        requestLocationUpdateService();
        setMyLocationButtonEnabled();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission(ACCESS_FINE_LOCATION_REQUEST_CODE);
        } else {
            doAfterPermissionGranted();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACCESS_FINE_LOCATION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                doAfterPermissionGranted();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void requestLocationPermission(int requestCode) {
        //below this version we only need to specify it in AndroidManifest
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestCode);
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        if (location != null)
            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

    }


    public void requestLocationUpdateService() {
        if (locationRequest == null) {
            locationRequest = new LocationRequest();
            locationRequest.setInterval(5000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    public void setMyLocationButtonEnabled() {
        // Enables/disables the my location button (this DOES NOT enable/disable the my location
        // dot/chevron on the map). The my location button will never appear if the my location
        // layer is not enabled.
        // First verify that the location permission has been granted.
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //these two operations need to be wrapped by this conditional statement
            //otherwise there would be a exception warning.
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

    private void changeMarkerSelectedState(Marker marker) {
        MarkerExtraInfo extraInfo = (MarkerExtraInfo)marker.getTag();
        extraInfo.isSelected = !extraInfo.isSelected;
        if (extraInfo.isSelected) { //become selected
            marker.setIcon(selectedBitmapDescriptor);
        } else {
            marker.setIcon(extraInfo.originalIconDescriptor);
        }
    }

    private boolean isMarkerSelected(Marker marker) {
        return ((MarkerExtraInfo)marker.getTag()).isSelected;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(markerSelectingMode) {
            changeMarkerSelectedState(marker);
            if(isMarkerSelected(marker)) {
                selectedMarkers.add(marker);
            }
            else {
                selectedMarkers.remove(marker);
            }

            return true; //disable default behaviour
        }
        else
            return false;
    }

    //we would use certain data like id to find and update old marker.
    private Marker tryToFindExistedMarker(String markerId) {
        for(Marker currentMarker : currentMarkers) {
            if(((MarkerExtraInfo)currentMarker.getTag()).markerId.equals(markerId)) {
                return currentMarker;
            }
        }

        return null;
    }

    private void removeOutdatedMarkers(HashSet<Marker> reservedMarkers) {
        for(Marker marker : currentMarkers) {
            if(!reservedMarkers.contains(marker)) {
                marker.remove();

            }
        }

        currentMarkers = new ArrayList<>(reservedMarkers);
    }

    @Override
    public void onData(JSONObject jsonObject) {
        if(map == null) {
            return;
        }

        HashSet<Marker> reservedMarkers = new HashSet<>();

        try {
            JSONArray gymObjs = jsonObject.getJSONArray("gyms");
            for(int i = 0;i < gymObjs.length();i++) {
                JSONObject gymObj = gymObjs.getJSONObject(i);

                int teamId = gymObj.getInt("team_id");
                String markerId = gymObj.getString("gym_id");

                Marker marker = tryToFindExistedMarker(markerId);

                if(marker != null) {
                    //update info
                    marker.setPosition(getLocDataFromJsonObj(gymObj));
                    marker.setSnippet("owned by Team " + gym_types[teamId]);
                }
                else {
                    MarkerExtraInfo extraInfo = new MarkerExtraInfo();
                    extraInfo.isSelected = false;
                    extraInfo.markerId = markerId;
                    marker = map.addMarker(new MarkerOptions()
                                    .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                                    .position(getLocDataFromJsonObj(gymObj))
                                    .title("gym")
                                    .snippet("owned by Team " + gym_types[teamId])
                    );
                    marker.setTag(extraInfo);
                }
                reservedMarkers.add(marker);

                //load image with url
                ImageLoader.getInstance().loadImage(
                        PGMapDataManager.ImgServerAddr + "/forts/" + gym_types[teamId] + ".png",
                        new SetMapMarkerWithBitmapLoadedFromUrl(marker,2)
                );
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            JSONArray pokemonObjs = jsonObject.getJSONArray("pokemons");
            for(int i = 0;i < pokemonObjs.length();i++) {
                JSONObject pokemonObj = pokemonObjs.getJSONObject(i);
                String markerId = pokemonObj.getString("encounter_id");

                Marker marker = tryToFindExistedMarker(markerId);
                if(marker != null) {
                    //update info
                    marker.setPosition(getLocDataFromJsonObj(pokemonObj));
                }
                else {
                    MarkerExtraInfo extraInfo = new MarkerExtraInfo();
                    extraInfo.isSelected = false;
                    extraInfo.markerId = markerId;
                    marker = map.addMarker(new MarkerOptions()
                                    .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                                    .position(getLocDataFromJsonObj(pokemonObj))
                                    .title(pokemonObj.getString("pokemon_name"))
                    );
                    marker.setTag(extraInfo);
                }
                reservedMarkers.add(marker);

                ImageLoader.getInstance().loadImage(
                        PGMapDataManager.ImgServerAddr + "/icons/" + pokemonObj.getInt("pokemon_id") + ".png" ,
                        new SetMapMarkerWithBitmapLoadedFromUrl(marker,1)
                );
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        removeOutdatedMarkers(reservedMarkers);
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
            BitmapDescriptor descriptor = BitmapDescriptorFactory.fromBitmap(scaledBitmap);
            MarkerExtraInfo extraInfo = (MarkerExtraInfo)markerToSet.getTag();
            extraInfo.originalIconDescriptor = descriptor;
            if(!extraInfo.isSelected) //not in selected state
                markerToSet.setIcon(extraInfo.originalIconDescriptor);
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {

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
