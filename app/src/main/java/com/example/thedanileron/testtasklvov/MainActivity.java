package com.example.thedanileron.testtasklvov;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient mFusedLocationClient;
    private AppCompatButton getCurLocationBtn, selectLocationBtn;
    private CardView cardSunInfo;
    private TextView yourSunInfoTV;
    private Activity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.context = this;

        cardSunInfo = findViewById(R.id.cardSunInfo);
        yourSunInfoTV = findViewById(R.id.sunString);
        getCurLocationBtn = findViewById(R.id.btnCurLocation);
        selectLocationBtn = findViewById(R.id.btnSelectLocation);
        getCurLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
                getCurrentLocationData();
            }
        });

        selectLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int PLACE_PICKER_REQUEST = 1;
                PlaceAutocomplete.IntentBuilder builder = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY);

                try {
                    startActivityForResult(builder.build(context), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            Place place = PlacePicker.getPlace(this, data);
            String toastMsg = String.format("Place: %s", place.getName());

            LatLng latLng = place.getLatLng();
            double latitude = latLng.latitude;
            double longitude = latLng.longitude;

            displaySunInformation(latitude, longitude);
            Toast.makeText(this, toastMsg + " lat: " + latitude + " long: " + longitude, Toast.LENGTH_LONG).show();
        }

    }

    private void getCurrentLocationData() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        CurrentLocationSuccessListener currentLocationSuccessListener = new CurrentLocationSuccessListener();
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, currentLocationSuccessListener);
    }

    private void displaySunInformation(double latitude, double longitude) {
        RequestQueue mRequestQueue;

        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

        Network network = new BasicNetwork(new HurlStack());

        mRequestQueue = new RequestQueue(cache, network);

        mRequestQueue.start();

        String url = "https://api.sunrise-sunset.org/json?lat=" + latitude + "&lng=" + longitude;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String strSunrise = jsonObject.getJSONObject("results").getString("sunrise");
                            String strSunset = jsonObject.getJSONObject("results").getString("sunset");

                            cardSunInfo.setVisibility(View.VISIBLE);
                            yourSunInfoTV.setText("Sun rises at " + strSunrise + "\nSun sets at " + strSunset);
                        } catch (JSONException e) {
                            Log.wtf("JSON CONVERT FAILED", "FOR SOME REASON WE GOT JSONException " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error
                    }
                });

        mRequestQueue.add(stringRequest);
    }

    private class CurrentLocationSuccessListener implements OnSuccessListener<Location> {

        @Override
        public void onSuccess(Location location) {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                displaySunInformation(latitude, longitude);
            }
        }
    }
}
