package com.example.anton.firstapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    protected Location mLastLocation;

    Double latitude;
    Double longitude;
    FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Weather");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

     private void getLastLocation() {
        mFusedLocationClient.getLastLocation().addOnCompleteListener(this, new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    mLastLocation = task.getResult();
                    latitude = mLastLocation.getLatitude();
                    longitude = mLastLocation.getLongitude();
                }
            }
        });
     }

    public void getTemperature(View view){
        Button button = findViewById(R.id.change_button);

        if (!checkPermissions()) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 34);
            return;
        }

        new LongRunningTask().execute();
        button.setEnabled(false);
        button.setAlpha(0.3f);
        button.setText("...");
        new LongRunningTask2().execute();
    }

    private class LongRunningTask2 extends AsyncTask<Void, Void, Void> {

        Button button = findViewById(R.id.change_button);

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            button.setEnabled(true);
            button.setAlpha(1.0f);
            button.setText("FIND OUT");
        }
    }

    private class LongRunningTask extends AsyncTask<Void, Void, String> {

        // Get the text view.
        TextView showCountTextView = findViewById(R.id.textView);
        String display = "";
        String inputLine;
        Boolean error = false;

        @Override
        protected String doInBackground(Void... voids) {

            StringBuilder response = new StringBuilder();

            if (latitude == null || longitude == null) {
                getLastLocation();
                while (latitude == null || longitude == null) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }

            String url = "https://api.darksky.net/forecast/2be7c1d3f9b304be4a2a04ff1a84d576/" + latitude + "," + longitude;
            System.out.println(url);

            try {

                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                con.setRequestMethod("GET");
                con.setRequestProperty("Content-Type", "application/json");

                if (con.getResponseCode() == 200) {
                    // get response stream
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    // feed response into the StringBuilder
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                }

                JSONObject fullResponse = new JSONObject(response.toString());
                JSONObject currentWeather = new JSONObject(fullResponse.getString("currently"));
                double tempF = currentWeather.getDouble("apparentTemperature");
                int tempC = Math.round(((Math.round(tempF) - 32)*5)/9);
                display = ("Feels like: " + String.valueOf(tempC) + "°");

            } catch (IOException | JSONException e) {

                e.printStackTrace();
                error = true;

            }

            return display;

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            showCountTextView.setText(display);
            if (error) {
                Toast myToast = Toast.makeText(getApplicationContext(), "Error fetching weather info. Is device connected to internet?", Toast.LENGTH_SHORT);
                myToast.show();
                error = false;
            }
        }
    }
}
