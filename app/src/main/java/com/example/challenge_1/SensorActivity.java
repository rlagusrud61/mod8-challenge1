package com.example.challenge_1;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


public class SensorActivity extends FragmentActivity implements SensorEventListener, OnMapReadyCallback, LocationListener {

    // sending log output
    private static final String TAG = "MyActivity";

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private LocationManager locationManager;
    // Handle location changes
    private LocationListener locationListener;

    TextView xValue, yValue, zValue;

    // If app is running or if its on pause
    Boolean running = true;

    private MapView mapView;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    double lat = 52.213453; // 52.213453
    double longi = 6.879420; // 6.879420
    LatLng latLng = new LatLng (lat, longi);
    LatLng[] coordinates_total;

    ArrayList<Float> accel_x = new ArrayList<Float>();
    ArrayList<Float> accel_y = new ArrayList<Float>();
    ArrayList<Float> accel_z = new ArrayList<Float>();

    private float average(ArrayList<Float> input) {
        float temp = 0;
        for (int i=0; i<input.size();i++) {
            temp += input.get(i);
        }
    return temp/input.size();
    }

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        xValue = (TextView) findViewById(R.id.xValue);
        yValue = (TextView) findViewById(R.id.yValue);
        zValue = (TextView) findViewById(R.id.zValue);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        if (ActivityCompat.checkSelfPermission((Activity)this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity)this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            }, 10);
        }

        // THE GPS location provider requires ACCESS_FINE_LOCATION permission.

        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);


        if (location != null) {
            lat = location.getLatitude();
            longi = location.getLongitude();

        } else {
            setMyLastLocation();
        }

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);



        // Pause accelerometer if button pressed
        final Button button = findViewById(R.id.pauseButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (running == true){
                    running = false;
                    button.setText("Continue");
                }
                else if (running == false){
                    running = true;
                    button.setText("Pause");
                }
            }
        });

        // Send a DEBUG log message.
        Log.d(TAG, "OnCreate: Initializing the sensor services");
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(SensorActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        Log.d(TAG, "OnCreate: Registered accelerometer listener");
    }

    private void setMyLastLocation() {
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        float x;
        float y;
        float z;

        accel_x.add(event.values[0]);
        accel_y.add(event.values[1]);
        accel_z.add(event.values[2]);

        if(accel_x.size() > 5) {
            accel_x.remove(0);
            accel_y.remove(0);
            accel_z.remove(0);
        }

        if (running == true) {
            x = average(accel_x);
            y = average(accel_y);
            z = average(accel_z);
            Log.d(TAG, "OnSensorChanged: X" + x + " Y:" + y + " Z:" + z);

            xValue.setText("xValue:" + x);
            yValue.setText("yValue:" + y);
            zValue.setText("zValue:" + z);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // STORE THE DATA IN A PARTICULAR FILE IN OUR APP
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        // Editor String == key, Integer Value = Coordinate of the pothhole
        editor.putString("Coordinates", (new LatLng(1,2).toString()));
        // All the data will not get copied back to the original file until apply is called
        editor.apply();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // STORE THE DATA IN A PARTICULAR FILE IN OUR APP
        SharedPreferences sharedPreferences = getSharedPreferences("PothHoles", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        // Editor String == key, Integer Value = Coordinate of the pothhole
        editor.putString("Coordinates", (new LatLng(1,2).toString()));
        // All the data will not get copied back to the original file until apply is called
        editor.apply();
        sensorManager.unregisterListener(this);
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        // ONCE YOU HAVE THE COORDINATES, YOU CAN ADD MANY MARKERS
        float zoomLevel = 15.0f;
        googleMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoomLevel));
    }

    @Override
    public void onLocationChanged(final Location location) {
        //your code here
    }

};

