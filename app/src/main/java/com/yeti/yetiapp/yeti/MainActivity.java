package com.yeti.yetiapp.yeti;

import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    int LOCATION_REFRESH_TIME = 1000;
    int LOCATION_REFRESH_DISTANCE = 5;
    LocationManager mLocationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return  ;
        }
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, mLocationListener);
        Location location = mLocationManager
                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            ((TextView)findViewById(R.id.txtLatitude)).setText(Double.toString(location.getLatitude()));
            ((TextView)findViewById(R.id.txtLongitude)).setText(Double.toString(location.getLongitude()));
            ((TextView)findViewById(R.id.txtAltitude)).setText(Double.toString(location.getAltitude()));
        }

        ((Button)findViewById(R.id.button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String YetiNodeIP = ((EditText)findViewById(R.id.txtYetiNodeIP)).getText().toString();
                new YetiNodeTask().execute(YetiNodeIP);
            }
        });
    }
    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            if (location != null) {
                ((TextView)findViewById(R.id.txtLatitude)).setText(Double.toString(location.getLatitude()));
                ((TextView)findViewById(R.id.txtLongitude)).setText(Double.toString(location.getLongitude()));
                ((TextView)findViewById(R.id.txtAltitude)).setText(Double.toString(location.getAltitude()));
            }
            Log.d("NEW location", Double.toString(location.getLatitude()) + " - " + Double.toString(location.getLongitude()));
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        @Override
        public void onProviderEnabled(String provider) {}
        @Override
        public void onProviderDisabled(String provider) {}
    };



    private class YetiNodeTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                Request YetiNodeRequest = new Request.Builder()
                        .url("http://" + params[0])
                        .build();
                OkHttpClient YetiNodeClient = new OkHttpClient();

                Response response = null;
                response = YetiNodeClient.newCall(YetiNodeRequest).execute();
                String json = response.body().string();
                Log.d("YetiNodeTask", json);
                return json;

            } catch (Exception e) {
                return null;
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jObject = new JSONObject(result);

                String soil_h = jObject.getString("soli_h");
                String t = jObject.getString("t");
                String d = jObject.getString("d");

                ((TextView)findViewById(R.id.txtDistance)).setText(d);
                ((TextView)findViewById(R.id.txtTemperature)).setText(t);
                ((TextView)findViewById(R.id.txtSoilHumidity)).setText(soil_h);

                DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                dateFormatter.setLenient(false);
                Date today = new Date();
                String time = dateFormatter.format(today);

                String YetiServer = ((EditText)findViewById(R.id.txtYetiServer)).getText().toString();
                YetiServer += "?depth=" + d + "&"
                + "temperature=" + t + "&"
                + "humidity=" + t + "&"//TODO
                + "time=" + time + "&"
                + "humidity_soil=" + soil_h  + "&"
                + "latitude=" + ((TextView)findViewById(R.id.txtLatitude)).getText() + "&"
                + "longitude=" + ((TextView)findViewById(R.id.txtLongitude)).getText() + "&"
                + "altitude=" + ((TextView)findViewById(R.id.txtAltitude)).getText();

                Log.d("YetiServer", YetiServer);

                new YetiServerTask().execute(YetiServer);
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }

    private class YetiServerTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                Request YetiNodeRequest = new Request.Builder()
                        .url("http://" + params[0])
                        .build();
                OkHttpClient YetiNodeClient = new OkHttpClient();

                Response response = null;
                try {
                    response = YetiNodeClient.newCall(YetiNodeRequest).execute();
                    Log.d("YetiServerTask", response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            } catch (Exception e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

        }
    }




}
