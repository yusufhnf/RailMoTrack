package com.sentraup.railmo.railmotrack;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback
        , GoogleMap.OnMyLocationButtonClickListener
        , GoogleMap.OnMyLocationClickListener, LocationListener {

    GoogleMap gMap;
    MarkerOptions markerOptions = new MarkerOptions();
    LatLng center, latLng;
    String title;
    TextView text;

    LocationManager locationManager;
    Location location;

    public static final String LAT = "lat";
    public static final String LNG = "lon";
    //public static final String INDEK = "indek";
    public static final String TITLE = "vmak";
    public static final String JARAK = "jarak";

    TextView kecepatan, maks, radius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        kecepatan = findViewById(R.id.realtime_kecepatan);
        maks = findViewById(R.id.realtime_maks);
        radius = findViewById(R.id.realtime_radius);

        //delay for
        Thread thread = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(5000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                radius.setText(getRadius() + " km");
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        thread.start();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                        , Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            } else {
                gMap.setMyLocationEnabled(true);
            }
        } else {
            gMap.setMyLocationEnabled(true);
        }

        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria,true);
        location = locationManager.getLastKnownLocation(bestProvider);
        locationManager.requestLocationUpdates(bestProvider,1,1, (LocationListener) this);
        if(location!=null){
            onLocationChanged(location);
        }

        gMap.setOnMyLocationButtonClickListener(this);
        gMap.setOnMyLocationClickListener(this);
        gMap.setMyLocationEnabled(true);
        gMap.animateCamera(CameraUpdateFactory.zoomTo(21));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    gMap.setMyLocationEnabled(true);
                }
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        //Toast.makeText(this, "My Location Button Clicked", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Lokasiku saat ini : " + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        gMap.addMarker(new MarkerOptions().position(latLng));
        //gMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        LatLng latLngToAppearMarker;

        latLngToAppearMarker = getMarkers(latitude,longitude);

        gMap.clear();
        if(latLngToAppearMarker!=null) {
            gMap.addMarker(new MarkerOptions().position(latLngToAppearMarker));
        }

        maks.setText(getMaks() + " km/jam");

        if(location.hasSpeed()){
            String speed = String.format(Locale.ENGLISH, "%.0f", location.getSpeed() * 3.6) + " km/jam";
            kecepatan.setText(speed);
            //radius.setText(getRadius() + " km");
        }
    }

    String getMaks(){
        return  kecmaks;
    }

    String getRadius(){
        return rad;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private void addMarker(LatLng latlng, final String title) {
        markerOptions.position(latlng);
        markerOptions.title(title);
        gMap.addMarker(markerOptions);

        gMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Toast.makeText(getApplicationContext(), marker.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    String kecmaks;
    String rad;
    // Fungsi get JSON marker
    private LatLng getMarkers(double lat, double lng) {

        RequestQueue queue = Volley.newRequestQueue(this);

        String requestaddress = "http://railmo.sentraup.com/parsing.php?lat="+lat+"&lon="+lng+"&rute=1";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, requestaddress, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("Response: ", response.toString());

                try {
                    //JSONObject jObj = new JSONObject((Map) response);
                    String getObject = response.getString("node");
                    JSONArray jsonArray = new JSONArray(getObject);
                    Log.d("Response JSON: ", getObject);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        title = "Kecepatan Maksimal :" + jsonObject.getString(TITLE) + " km/jam";
                        latLng = new LatLng(Double.parseDouble(jsonObject.getString(LAT)), Double.parseDouble(jsonObject.getString(LNG)));

                        // Menambah data marker untuk di tampilkan ke google map
                        addMarker(latLng, title);
                        kecmaks = jsonObject.getString(TITLE);
                        rad = jsonObject.getString(JARAK);
                    }

                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.e("Error: ", error.getMessage());
                //Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        queue.add(jsonObjectRequest);
        //AppController.getInstance().addToRequestQueue(strReq, tag_json_obj);
        return  latLng;
    }
}
