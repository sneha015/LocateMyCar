package com.example.murtazachunia.project220v3;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/*
This class is responsible for connecting to google map's and drawing the floormap
of the area on the google map. This activity is started when myCar button is pressed
in the mainactivity. This activity shows the floor and zone information in a text marker
 where the user has actually parked the car.
 */

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap;

    LocationManager locationManager;
    String provider;
    String floormap;
    JSONObject parentObject;

    String x1;
    String y1;
    String x2;
    String y2;
    String x3;
    String y3;
    String x4;
    String y4;
    String lat;
    String lng;
    String zone;
    String inZone;
    String onFloor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        Intent intent = getIntent();
        floormap = intent.getStringExtra("MAP");
        inZone = intent.getStringExtra("ZONE");
        onFloor = intent.getStringExtra("FLOOR");
        try {
            parentObject = new JSONObject(floormap);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map)).getMap();

            if (mMap != null) {
                try {
                    setUpMap();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        //mapFragment.getMapAsync(this);

       /* locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);
        if ( location != null)
        {
            onLocationChanged(location);
        }*/
    }


    private void setUpMap() throws JSONException {
        mMap.clear();
     //   Toast.makeText(getApplicationContext(),"IN MAPS SETUP",Toast.LENGTH_SHORT).show();
     //   Toast.makeText(getApplicationContext(),floormap,Toast.LENGTH_SHORT).show();
        JSONArray parentArray = parentObject.getJSONArray("floormap");
        int length = parentArray.length();
        JSONObject finalObject;
        for(int i = 0; i < length; i++){
            finalObject = parentArray.getJSONObject(i);
            x1 = finalObject.getString("leftTop_x");
            y1 = finalObject.getString("leftTop_y");
            x2 = finalObject.getString("rightTop_x");
            y2 = finalObject.getString("rightTop_y");
            x3 = finalObject.getString("rightBottom_x");
            y3 = finalObject.getString("rightBottom_y");
            x4 = finalObject.getString("leftBottom_x");
            y4 = finalObject.getString("leftBottom_y");
            zone = finalObject.getString("zone");
            if(inZone.equals(zone)){
                Polygon polygon=  mMap.addPolygon(new PolygonOptions()
                        .add(new LatLng(Double.parseDouble(x1), Double.parseDouble(y1)), new LatLng(Double.parseDouble(x2), Double.parseDouble(y2)), new LatLng(Double.parseDouble(x3), Double.parseDouble(y3)), new LatLng(Double.parseDouble(x4), Double.parseDouble(y4)), new LatLng(Double.parseDouble(x1), Double.parseDouble(y1)))
                        .strokeColor(Color.GREEN)
                        .strokeWidth(2)
                        .fillColor(Color.RED));

                Marker mark = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(Double.parseDouble(x1), Double.parseDouble(y1)))
                        .title(onFloor +" : "+inZone)
                        .alpha(0.8f));
                mark.showInfoWindow();
                mark.setInfoWindowAnchor(.5f, 1.0f);
            }
            else {
                Polygon polygon = mMap.addPolygon(new PolygonOptions()
                        .add(new LatLng(Double.parseDouble(x1), Double.parseDouble(y1)), new LatLng(Double.parseDouble(x2), Double.parseDouble(y2)), new LatLng(Double.parseDouble(x3), Double.parseDouble(y3)), new LatLng(Double.parseDouble(x4), Double.parseDouble(y4)), new LatLng(Double.parseDouble(x1), Double.parseDouble(y1)))
                        .strokeColor(Color.GREEN)
                        .strokeWidth(2)
                        .fillColor(Color.BLUE));
            }

        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(x1), Double.parseDouble(y1)), 18));

    }


    @Override
    protected void onPause() {
        super.onPause();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }


}