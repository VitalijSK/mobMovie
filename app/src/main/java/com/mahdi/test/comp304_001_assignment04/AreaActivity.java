package com.mahdi.test.comp304_001_assignment04;
/*
 * Author: Mahdi Moradi - 300951014
 * Final Project - CINEPLEX Ticket Service
 * Date: 18 April 2019
 *
 */

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class AreaActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
        {

    ProgressBar progressBar;
    GoogleMap map;
    Intent jSONDownload;
    Movie movie;
    private GoogleMap mMap;
    private LocationRequest request;
    private GoogleApiClient client;
    private LatLng latLngCurrent;
    private FusedLocationProviderClient fusedLocationClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area);

        //set Toolbar properties
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarArea);
        toolbar.setTitle("Find Locations");
        setSupportActionBar(toolbar);


        checkForInternetPermission();
        checkForLocationPermission();

        movie = (Movie) getIntent().getSerializableExtra("movie");

        //this progress bar is for experiment only, not working

        //register broadcast receiver for service broadcasts
        IntentFilter filter = new IntentFilter("progress_action");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFrag);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        //it gets the Data from Cineplex API jSON
        //and create a list of Theatres in Statics Class
        //this includes all information about cineplex Theatre locations
        //start the jsonDownload Service
        jSONDownload = new Intent(this, JsonDownloadService.class);
        this.startService(jSONDownload);

    }//end of onCreate Method

    //method to check and request permissions
    private void checkForInternetPermission() {
        boolean internetNotGranted = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED;

        if (internetNotGranted) {
            Log.d("AreaActivity", getString(R.string.read_permission_not_granted));
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET},
                    1);
        }else {
            Toast.makeText(this, "Have Permission", Toast.LENGTH_SHORT).show();
        }
    }

    //method to check and request permissions
    private void checkForLocationPermission() {
        boolean locationGranted = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED;
        boolean coarsGranted = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
        if (locationGranted || coarsGranted) {
            Log.d("MYPOR", getString(R.string.read_permission_not_granted));
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
        }else {
            Toast.makeText(this, "Have Permission", Toast.LENGTH_SHORT).show();
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            Log.d("MYPOR", "local change");
                            if (location == null) {
                                Toast.makeText(getApplicationContext(), "Location not found", Toast.LENGTH_SHORT).show();
                            } else {
                                latLngCurrent = new LatLng(location.getLatitude(), location.getLongitude());

                                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLngCurrent, 15);
                                mMap.animateCamera(update);

                                MarkerOptions options = new MarkerOptions();
                                options.position(latLngCurrent);
                                options.title("Current location");
                                mMap.addMarker(options);
                                findCinema();
                            }
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("MYPOR", "local change error" + e);
                        }
                    })
                    .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            Log.d("MYPOR", "local change complete");
                        }
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (permissions[0].equalsIgnoreCase(Manifest.permission.INTERNET)
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted.
                    Toast.makeText(this, "Granted", Toast.LENGTH_SHORT).show();
                } else {
                    // Permission denied.
                    Log.d("AreaActivity", getString(R.string.failure_permission));
                    Toast.makeText(this, getString(R.string.failure_permission),
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        mMap = map;
        client = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        client.connect();
        Log.d("MYPOR", "initt");

    }

    private void findCinema() {
        StringBuilder stringBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        stringBuilder.append("location="+latLngCurrent.latitude+"," + latLngCurrent.longitude);
        stringBuilder.append("&radius="+ 100000);
        stringBuilder.append("&language=" + "ru");
        stringBuilder.append("&keyword=" + "кинотеатры");
        stringBuilder.append("&key="+getResources().getString(R.string.google_places_key));
        String url = stringBuilder.toString();

        Object dataTransfer[] = new Object[2];
        dataTransfer[0] = mMap;
        dataTransfer[1] = url;
        GetNearlyByPlaces getNearlyByPlaces = new GetNearlyByPlaces(this);
        getNearlyByPlaces.execute(dataTransfer);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        request = new LocationRequest().create();
        Log.d("MYPOR", "connect");
        request.setInterval(1000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

}//end of activity
