package com.mahdi.test.comp304_001_assignment04;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class GetNearlyByPlaces extends AsyncTask<Object, String, String> {
    GoogleMap mMap;
    String url;
    InputStream is;
    BufferedReader bufferedReader;
    StringBuilder lines;
    String data;

    public GetNearlyByPlaces(MapsActivity mapsActivity) {

    }

    public GetNearlyByPlaces(AreaActivity areaActivity) {
    }

    @Override
    protected String doInBackground(Object... params) {
        mMap = (GoogleMap)params[0];
        url = (String)params[1];
        try {
            URL myUrl = new URL(url);
            HttpURLConnection httpURLConnection = (HttpURLConnection)myUrl.openConnection();
            httpURLConnection.connect();
            is = httpURLConnection.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(is));

            String line="";
            lines = new StringBuilder();

            while((line = bufferedReader.readLine()) != null) {
                lines.append(line);
            }

            data = lines.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

    @Override
    protected void onPostExecute(String s) {
        Log.d("MYPOR", "onPostExecute");
        try {
            JSONObject parentObject = new JSONObject(s);
            JSONArray resultArray = parentObject.getJSONArray("results");
            Log.d("MYPOR", "parentObject");
            for(int i = 0, n = resultArray.length(); i < n; i++) {
                JSONObject jsonObject = resultArray.getJSONObject(i);
                JSONObject locationObj = jsonObject
                        .getJSONObject("geometry")
                        .getJSONObject("location");

                String latitude = locationObj.getString("lat");
                String longitude = locationObj.getString("lng");

                JSONObject nameObject = resultArray.getJSONObject(i);

                String name_cinema = nameObject.getString("name");
                Log.d("MYPOR", "name_cinema " + name_cinema);
                String vicinity = nameObject.getString("vicinity");

                String snippet = vicinity;

                LatLng latLng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));

                MarkerOptions options = new MarkerOptions();
                options.title(name_cinema);
                options.snippet(snippet);
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                options.position(latLng);

                mMap.addMarker(options);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
