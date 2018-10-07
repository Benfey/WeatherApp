package com.example.anton.firstapp;

import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Weather - Fredericton");
    }

    public void getTemperature(View view){
        Button button = findViewById(R.id.change_button);
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
                System.out.println("Sleeping...");
                TimeUnit.SECONDS.sleep(5);
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

        @Override
        protected String doInBackground(Void... voids) {
            StringBuilder response = new StringBuilder();

            try {

                URL obj = new URL("https://api.darksky.net/forecast/2be7c1d3f9b304be4a2a04ff1a84d576/45.959856,-66.763270");
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
            }

            return display;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            showCountTextView.setText(display);
        }
    }
}
