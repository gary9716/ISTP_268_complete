package com.hci.lab430.myapplication.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
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
import java.util.List;

/**
 * Created by lab430 on 16/8/15.
 */
public class PokemonMapFragment extends ItemFragment implements OnMapReadyCallback, GeoCodingTask.GeoCodingResponse, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, PGMapDataManager.DataChangedListener, GoogleMap.OnMarkerClickListener, RoutingListener, DialogInterface.OnClickListener{

    public final static int ACCESS_FINE_LOCATION_REQUEST_CODE = 1;

    View fragmentView;
    MapFragment mapFragment;

    PGMapDataManager mapDataManager;
    final String[] gym_types = {"Uncontested", "Mystic", "Valor", "Instinct"};

    GoogleMap map;
    LatLng currentLocation = null;
    GoogleApiClient googleApiClient;
    LocationRequest locationRequest;
    BitmapDescriptor selectedBitmapDescriptor = null;

    private boolean markerSelectingMode = false;
    ArrayList<Marker> currentMarkers = new ArrayList<>();
    ArrayList<Marker> routingMarkers = new ArrayList<>();
    Polyline polyline;

    private AlertDialog routingDialog;
    Routing currentRoute = null;
    
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

        routingDialog = new AlertDialog.Builder(getActivity())
                .setMessage("按下確認後會開始規劃路線")
                .setNegativeButton("取消", this)
                .setPositiveButton("確認",this)
                .create();
    }

    //here I demo how to nest a fragment inside anther fragment
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(fragmentView == null) {
            fragmentView = inflater.inflate(R.layout.fragment_map, container, false);
            mapFragment = MapFragment.newInstance();
            mapFragment.getMapAsync(this);
            setHasOptionsMenu(true);
            setMenuVisibility(true);
        }
        return fragmentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getChildFragmentManager().beginTransaction().replace(R.id.childFragmentContainer, mapFragment).commit();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.map_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if(itemId == R.id.action_routing) {
            routingDialog.show();
            return true;
        }
        else if(itemId == R.id.action_selecting_mode_switch) {
            markerSelectingMode = !markerSelectingMode;
            if(markerSelectingMode) { //become selecting mode
                item.setTitle("一般模式");
            }
            else {
                //recover all routing markers
                for(Marker marker : routingMarkers) {
                    changeMarkerSelectedState(marker);
                }
                routingMarkers.clear();
                item.setTitle("選取模式");
            }
            return true;
        }
        else
            return super.onOptionsItemSelected(item);
    }

    @Override
    public void callbackWithGeoCodingResult(LatLng latLng) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("NTU").snippet("National Taiwan University");
        map.moveCamera(cameraUpdate);
        map.addMarker(markerOptions);

        createGoogleApiClient();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMarkerClickListener(this);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pokemon_selected_marker);
        bitmap = Bitmap.createScaledBitmap(bitmap, (int)(bitmap.getWidth() * 0.2f), (int)(bitmap.getHeight() * 0.2f), false);
        selectedBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);

        UiSettings mapUISettings = googleMap.getUiSettings();
        mapUISettings.setZoomControlsEnabled(true);

        (new GeoCodingTask(PokemonMapFragment.this)).execute("台北市羅斯福路四段一號");
    }

    private void createGoogleApiClient()
    {
        if(googleApiClient == null) {
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

    public void requestLocationPermission (int requestCode){
        //below this version we only need to specify it in AndroidManifest
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestCode);
        }
    }

    private void removePolylinePointsBaseOnLocation(Location location) {
        List<LatLng> points = polyline.getPoints();

        int index = -1;

        for(int i=0; i < points.size();i ++)
        {
            if(i < points.size() -1)
            {
                LatLng point1 = points.get(i);
                LatLng point2 =  points.get(i+1);
                double offset = 0.0001;

                Double maxLat = Math.max(point1.latitude, point2.latitude) + offset;
                Double maxLng = Math.max(point1.longitude, point2.longitude) + offset;
                Double minLat = Math.min(point1.latitude, point2.latitude) - offset;
                Double minLng = Math.min(point1.longitude, point2.longitude) - offset;
                if(location.getLatitude() >= minLat && location.getLatitude() <= maxLat && location.getLongitude() >= minLng && location.getLongitude() <= maxLng)
                {
                    index = i;
                    break;
                }
            }
        }

        if(index != -1)
        {
            for (int i = index - 1;i >= 0;i--) {
                points.remove(0);
            }
            points.set(0, new LatLng(location.getLatitude(), location.getLongitude()));
            polyline.setPoints(points);
        }
        else {
            //refresh polyline
            doRouting(currentLocation);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null)
            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

        if(polyline == null) {
            return;
        }

        removePolylinePointsBaseOnLocation(location);
    }


    public void requestLocationUpdateService() {
        if(locationRequest == null) {
            locationRequest = new LocationRequest();
            locationRequest.setInterval(5000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }

        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if(location != null)
            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
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
                routingMarkers.add(marker);
            }
            else {
                routingMarkers.remove(marker);
            }
            return true;
        }
        else
            return false;
    }

    private Marker tryToFindExistedMarker(String markerId) {
        for(Marker currentMarker : currentMarkers) {
            if(((MarkerExtraInfo)currentMarker.getTag()).markerId.equals(markerId)) {
                return currentMarker;
            }
        }

        return null;
    }

    private void removeOutdatedMarkers(HashSet<Marker> reservedMarkers) {
        boolean needToUpdateRoute = false;
        for(Marker marker : currentMarkers) {
            if(!reservedMarkers.contains(marker)) {
                marker.remove();
                if(routingMarkers.remove(marker)) {
                    needToUpdateRoute = true;
                }

            }
        }

        if(needToUpdateRoute) {
            doRouting(currentLocation);
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







    private void doRouting(LatLng startLoc) {

        ArrayList<LatLng> routingLocations = new ArrayList<>();
        for(Marker marker : routingMarkers) {
            routingLocations.add(marker.getPosition());
        }

        if(startLoc != null)
            routingLocations.add(0, startLoc);

        //delete previous route
        if(currentRoute != null) {
            currentRoute.cancel(true);
            currentRoute = null;
        }

        if(polyline != null) {
            polyline.remove();
            polyline = null;
        }

        currentRoute = new Routing.Builder()
                .travelMode(Routing.TravelMode.WALKING)
                .withListener(this)
                .waypoints(routingLocations)
                .build();

        currentRoute.execute();
    }

    @Override
    public void onRoutingFailure(RouteException e) {

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> arrayList, int i) {
        Route route = arrayList.get(i);

        List<LatLng> points = route.getPoints();

        PolylineOptions polylineOptions = new PolylineOptions();

        polylineOptions.addAll(points);

        polylineOptions.color(Color.GREEN);
        polylineOptions.width(10);

        polyline = map.addPolyline(polylineOptions);

    }

    @Override
    public void onRoutingCancelled() {

    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        if(which == AlertDialog.BUTTON_POSITIVE) {
            doRouting(currentLocation);
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
