package com.example.user.justparknew20;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private TextView mymapData;
    private GoogleMap mMap;
    private static final int GPS_ERRORDIALOG_REQUEST = 9001; //any value you want
    MapView mMapView;
    private static final float DEFAULTZOOM=15;

    @SuppressWarnings("unused")
    private static final String LOGTAG = "Maps";

    //getting all requests
    public class JSONTask extends AsyncTask<String, String, String> {
        HashMap<String,ArrayList<Double>> pairingTitle = new HashMap<>();

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection connection=null;
            BufferedReader reader=null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();

                //Create JSONObject here
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("to", "2016-04-27 07:50:00");
                jsonParam.put("from", "2016-04-27 19:40:00");
                JSONObject nested = new JSONObject();
                nested.put("lat", "51.4666895");
                nested.put("lng", "-0.0052762");
                jsonParam.put("near",nested);

                String message = jsonParam.toString();

                //add new one
                connection.setReadTimeout( 10000 /*milliseconds*/ );
                connection.setConnectTimeout( 15000 /* milliseconds */ );
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setFixedLengthStreamingMode(message.getBytes().length);
                connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");

                //setup send
                OutputStream os = new BufferedOutputStream(connection.getOutputStream());
                os.write(message.getBytes());
                //clean up
                os.flush();
                connection.connect();

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line ="";
                while ((line = reader.readLine()) != null){
                    buffer.append(line);
                    Log.d("buffer",buffer.toString());
                }

                String finalJson = buffer.toString();

                JSONObject parentObject = new JSONObject(finalJson);
                JSONArray parentArray = parentObject.getJSONArray("data");

                //go through all the data from JSON
                for(int i = 0; i<parentArray.length(); i++) {
                    JSONObject finalobject = parentArray.getJSONObject(i);
                    ArrayList<Double> locations = new ArrayList<>();
                    locations.add(Double.parseDouble(finalobject.getString("address_lat")));
                    locations.add (Double.parseDouble(finalobject.getString("address_lng")));//get the position
                    pairingTitle.put(finalobject.getString("title"),locations); //pair the title with addres longlat
                }

            } catch (MalformedURLException e) {
                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {

                if (connection != null){
                    connection.disconnect();}
                try {
                    if(reader!=null){
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            //get where the location is
            super.onPostExecute(s);
            for(Map.Entry<String, ArrayList<Double>> pair : pairingTitle.entrySet()) {
                gotoLocation(pair.getKey(),pair.getValue().get(0), pair.getValue().get(1), DEFAULTZOOM);

            }
        }
    }

    //getting request end

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        JSONTask task = new JSONTask();
        task.execute("https://api.justpark.com/apiv3/search/region");

        if (servicesOK()){
            setContentView(R.layout.activity_maps);

            if (initMap()) {
                Toast.makeText(this, "Ready to map!", Toast.LENGTH_SHORT).show();
                //mMap.setMyLocationEnabled(true); //add mylocation to the map, to show current location
            }
            else{
                Toast.makeText(this, "Map isn't Available", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            setContentView(R.layout.activity_main);
        }

        //Button moveit = (Button)findViewById(R.id.mapPage);

        //button to display json
        //Button btnHit = (Button)findViewById(R.id.gomap);
        //mymapData = (TextView)findViewById(R.id.displayText);

    }

    private void gotoLocation(String title, double lat, double lng, float zoom) {
        LatLng ll = new LatLng(lat,lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
        mMap.moveCamera(update);
        mMap.addMarker(new MarkerOptions()
                .position(ll)
                .title("This is a possible parking spot!")
                .snippet(title)
        ); //pin the location details
    }

    public boolean servicesOK(){
        //Check whether the device is GooglePlay Compatible!!
        int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if(isAvailable == ConnectionResult.SUCCESS){
            return true;
        }
        else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(isAvailable, this, GPS_ERRORDIALOG_REQUEST);
            dialog.show(); //google will determine, not up to u
        }
        else{
            Toast.makeText(this, "Cannot Connect!", Toast.LENGTH_SHORT).show();
        }
        return false;

    }

    private boolean initMap(){
        if (mMap == null){
            SupportMapFragment mapFrag =
                    (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mMap = mapFrag.getMap();
        }
        return (mMap != null);
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
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
