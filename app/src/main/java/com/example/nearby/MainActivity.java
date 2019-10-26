package com.example.nearby;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.nearby.listViewSearch.CustomListViewAdapter;
import com.example.nearby.listViewSearch.DataList;
import com.example.nearby.utils.Connectivity;
import com.example.nearby.utils.TypefaceUtil;
import com.example.nearby.utils.PrefManager;
import com.example.nearby.utils.SharedPreference;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    protected double latAfter, longAfter,latBefore,lonBefore;
    List<String> photo, name, id, address;
    DataList dataList;
    CustomListViewAdapter mAdapter;
    RecyclerView recyclerView;
    RelativeLayout layoutLoadig;
    TextView progressTV,realTimeState;
    ProgressBar progressBar;
    ImageView IvError;
    String basic_url = "https://api.foursquare.com/v2/venues/";
    String client_id = "CUYGFRFU0BHLA4HVACINGPXEUYJK4BR5PNHU1ECV2UMEI03Q";
    String client_secret = "IZ1AN01K0IMIEZPIOICJMAZBK0VHZTU0PA45Z5BZN0FQZT2C";
    String v = "20191024";
    String llAcc = "1000";
    int index = 0;
    LatLng myLocation;
    android.app.AlertDialog alert;
    private SwipeRefreshLayout swipeContainer;
    private FusedLocationProviderClient mFusedLocationClient;
    private SharedPreference sharedPreference;
    private PrefManager prefManager;
    String state="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TypefaceUtil.overrideFont(getApplicationContext(), "SERIF", "font/SourceSansPro-Light.ttf");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplication());
        layoutLoadig = findViewById(R.id.LayoutLoading);
        progressTV = findViewById(R.id.progressTV);
        progressBar = findViewById(R.id.progressBar);
        IvError = findViewById(R.id.imageError);
        recyclerView = findViewById(R.id.recyclerView);
        realTimeState = findViewById(R.id.realTimeState);
        sharedPreference = new SharedPreference(this);
        prefManager = new PrefManager(getApplication());
         state = sharedPreference.preferences.getString("state", "");
        if(state.isEmpty()|| state.equals("realTime")){
            realTimeState.setText("realTime");
            state="realTime";
        }else{
            realTimeState.setText("Single update");
            state="Single update";
        }
        realTimeState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (state.equals("realTime")){
                    realTimeState.setText("Single update");
                    state="Single update";
                }else {
                    realTimeState.setText("realTime");
                    state="realTime";
                }
                sharedPreference.prefEditor.putString("state", state).apply();
            }
        });
        photo = new ArrayList<>();
        name = new ArrayList<>();
        id = new ArrayList<>();
        address = new ArrayList<>();
        swipeContainer = findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                layoutLoadig.setVisibility(View.VISIBLE);
                //connectionGetPlaces(basic_url,latitude,longitude,1000);
                swipeContainer.setRefreshing(false);
                IvError.setVisibility(View.GONE);
                progressTV.setText(R.string.please_wait);
                progressBar.setVisibility(View.VISIBLE);
                getLocation();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        ///////////////////////////////////////////////////////////////////////////////////////////
        getLocation();
    }

    public void getLocation() {
        if (checkLocationPermission()) {
            checkGps();
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                latAfter = location.getLatitude();
                                longAfter = location.getLongitude();
                                if (Connectivity.isConnected(getApplicationContext())) {
                                    if(latBefore==0 && lonBefore==0 ){
                                        latBefore=latAfter;
                                        lonBefore=longAfter;
                                        connectionGetPlaces(latAfter, longAfter); //first call
                                    }else if (state.equals("realTime")){
                                        Location locBefore = new Location("");
                                        locBefore.setLatitude(latBefore);
                                        locBefore.setLongitude(lonBefore);
                                        Location locAfter = new Location("");
                                        locAfter.setLatitude(latAfter);
                                        locAfter.setLongitude(longAfter);
                                        float distanceInMeters = locBefore.distanceTo(locAfter);
                                        if (distanceInMeters>=500){
                                            connectionGetPlaces(latAfter, longAfter);
                                        }
                                    }

                                } else {
                                    progressBar.setVisibility(View.GONE);
                                    progressTV.setText(R.string.no_internet);
                                    IvError.setVisibility(View.VISIBLE);
                                    IvError.setImageDrawable(getDrawable(R.drawable.cloud_problem));
                                }
                                Log.d("location", location.getLatitude() + location.getLongitude() + "");
                            } else {
                                checkGps();
                            }
                        }
                    });
        }
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        99);

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        99);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 99: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                    getLocation();
                }
            }

        }
    }

    void checkGps() {
        LocationManager locationManager = (LocationManager) getApplication().getSystemService(LOCATION_SERVICE);
        assert locationManager != null;
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showGPSDisabledAlertToUser();
        }
    }

    private void showGPSDisabledAlertToUser() {
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Goto Settings Page To Enable GPS",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                                dialog.cancel();
                            }
                        });

        alert = alertDialogBuilder.create();
        alert.show();
    }

    public String getDataJason(String JsonStr) /////this is method we call from Background method
            throws JSONException {
        final String TAG_response = "response";
        final String TAG_groups = "groups";
        final String TAG_items = "items";
        final String TAG_venue = "venue";
        final String TAG_id = "id";
        final String TAG_location = "location";
        final String TAG_address = "formattedAddress";
        final String TAG_name = "name";
        JSONObject jsonObject = new JSONObject(JsonStr);
        JSONObject jsonObjectResponse = jsonObject.getJSONObject(TAG_response);
        JSONArray jsonOArrayGroups = jsonObjectResponse.getJSONArray(TAG_groups);
        for (int i = 0; i < jsonOArrayGroups.length(); i++) {
            JSONArray jsonOArrayItems = jsonOArrayGroups.getJSONObject(i).getJSONArray(TAG_items);
            for (int x = 0; x < jsonOArrayItems.length(); x++) {
                JSONObject jsonObjectVenue = jsonOArrayItems.getJSONObject(x).getJSONObject(TAG_venue);
                JSONObject jsonObjectLocation = jsonObjectVenue.getJSONObject(TAG_location);
                String strId = jsonObjectVenue.getString(TAG_id);
                id.add(strId);
                name.add(jsonObjectVenue.getString(TAG_name));
                JSONArray jsonArrayAddress=jsonObjectLocation.getJSONArray(TAG_address);
                String addressFormatter="";
                for(int y=0;y<jsonArrayAddress.length();y++){
                    addressFormatter=addressFormatter+"-"+jsonArrayAddress.getString(y);
                }
                address.add(addressFormatter);
            }
        }
        dataList = new DataList(id, photo, name, address);
        mAdapter = new CustomListViewAdapter(getApplication(), dataList);
        GridLayoutManager mLayoutBrowserManager = new GridLayoutManager(this, 1, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutBrowserManager);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplication());
        ((LinearLayoutManager) mLayoutManager).setOrientation(RecyclerView.HORIZONTAL);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        getPhoto(id.get(index));
        return "suc";
    }

    public void connectionGetPlaces(Double latitude, double longitude) {
        id.clear();
        name.clear();
        photo.clear();
        address.clear();
        RequestQueue requestQueue = Volley.newRequestQueue(getApplication());
        final String url = basic_url + "explore?client_id=" + client_id + "&client_secret=" + client_secret + "&v=" + v + "&ll=" + latitude + "," + longitude + "&llAcc=" + llAcc;
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        Log.d("url", url);
                        Log.d("Response", response.toString());
                        try {
                            getDataJason(response.toString());
                            layoutLoadig.setVisibility(View.GONE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            progressBar.setVisibility(View.GONE);
                            IvError.setVisibility(View.VISIBLE);
                            IvError.setImageDrawable(getDrawable(R.drawable.warning));
                            progressTV.setText(R.string.noData);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error + "");
                    }
                }
        );

        getRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
        queue.add(getRequest);


    }

    public void getPhotsDataJason(String JsonStr) /////this is method we call from Background method
            throws JSONException {
        final String TAG_response = "response";
        final String TAG_photos = "photos";
        final String TAG_items = "items";
        final String TAG_prefix = "prefix";
        final String TAG_suffix = "suffix";
        final String TAG_width = "width";
        final String TAG_height = "height";
        JSONObject jsonObject = new JSONObject(JsonStr);
        JSONObject jsonObjectResponse = jsonObject.getJSONObject(TAG_response);
        JSONObject jsonOpjectPhoto = jsonObjectResponse.getJSONObject(TAG_photos);
        JSONArray jsonOArrayItems = jsonOpjectPhoto.getJSONArray(TAG_items);
        String url;
        if(jsonOArrayItems.length()>0){
            JSONObject dataItems = jsonOArrayItems.getJSONObject(0);
             url = dataItems.getString(TAG_prefix) + dataItems.getString(TAG_width) + "x" + dataItems.getString(TAG_height) + dataItems.getString(TAG_suffix);
        }else {
            url="";
        }
        photo.add(url);
        index = index + 1;
        if (id.size() > index) {
            getPhoto(id.get(index));
        }
        mAdapter.notifyDataSetChanged();
    }

    public void getPhoto(String id) {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplication());
        String url = basic_url + id + "/photos?client_id=" + client_id + "&client_secret=" + client_secret + "&v=" + v;
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        Log.d("Response", response.toString());
                        try {
                            getPhotsDataJason(response.toString());
                            layoutLoadig.setVisibility(View.GONE);
                        } catch (JSONException e) {
                            Toast.makeText(getApplication(),"errorType quota_exceeded",Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplication(),"errorType quota_exceeded",Toast.LENGTH_LONG).show();
                        Log.d("Error.Response", error + "");
                    }
                }
        );
        getRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
        queue.add(getRequest);


    }
}
