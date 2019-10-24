package com.example.nearby;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import com.example.nearby.utils.TypefaceUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<String> photo, name, id, address;
    DataList dataList;
    CustomListViewAdapter mAdapter;
    RecyclerView recyclerView;
    RelativeLayout layoutLoadig;
    TextView progressTV;
    String basic_url = "https://api.foursquare.com/v2/venues/";
    String client_id = "CUYGFRFU0BHLA4HVACINGPXEUYJK4BR5PNHU1ECV2UMEI03Q";
    String client_secret = "IZ1AN01K0IMIEZPIOICJMAZBK0VHZTU0PA45Z5BZN0FQZT2C";
    String v = "20191024";
    String llAcc = "1000";
    int index = 0;
    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TypefaceUtil.overrideFont(getApplicationContext(), "SERIF", "font/ArbFONTS-GE_SS_TWO_MEDIUM.otf");
        layoutLoadig = findViewById(R.id.LayoutLoading);
        progressTV = findViewById(R.id.progressTV);
        progressTV = findViewById(R.id.progressTV);
        recyclerView = findViewById(R.id.recyclerView);
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

            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        ///////////////////////////////////////////////////////////////////////////////////////////
        connectionGetPlaces(30.1083957, 31.3681733);

    }

    public String getDataJason(String JsonStr) /////this is method we call from Background method
            throws JSONException {
        final String TAG_response = "response";
        final String TAG_groups = "groups";
        final String TAG_items = "items";
        final String TAG_venue = "venue";
        final String TAG_id = "id";
        final String TAG_location = "location";
        final String TAG_address = "address";
        final String TAG_city = "city";
        final String TAG_state = "state";
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
                address.add(jsonObjectLocation.getString(TAG_address));

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
        String url = basic_url + "explore?client_id=" + client_id + "&client_secret=" + client_secret + "&v=" + v + "&ll=" + latitude + "," + longitude + "&llAcc=" + llAcc;
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        Log.d("Response", response.toString());
                        try {
                            getDataJason(response.toString());
                            layoutLoadig.setVisibility(View.GONE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            layoutLoadig.setVisibility(View.VISIBLE);
                            progressTV.setText("جارى التحميل لاعادة... التحميل اسحب لاسفل");
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

    public String getPhotsDataJason(String JsonStr) /////this is method we call from Background method
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
        JSONObject dataItems = jsonOArrayItems.getJSONObject(0);
        String url = dataItems.getString(TAG_prefix) + dataItems.getString(TAG_width) + "x" + dataItems.getString(TAG_height) + dataItems.getString(TAG_suffix);
        Log.d("azsx",url);
        photo.add(url);
        index = index + 1;
        if (id.size() > index) {
            getPhoto(id.get(index));
        }
        return url;
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
                            e.printStackTrace();
                            layoutLoadig.setVisibility(View.VISIBLE);
                            progressTV.setText("جارى التحميل لاعادة... التحميل اسحب لاسفل");
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
}
