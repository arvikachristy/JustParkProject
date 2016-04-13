package com.example.user.justparknew20;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

public class MainActivity extends AppCompatActivity {

    private TextView mymapData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //button to move page
        Button moveit = (Button)findViewById(R.id.mapPage);
        moveit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MapsActivity.class));
            }
        });

        //button to display json
        Button btnHit = (Button)findViewById(R.id.btnHit);
        mymapData = (TextView)findViewById(R.id.tvJsonItem);

        //to set up POST request


        /*try {
            URL url = new URL("https://api.justpark.com/apiv3/search/region");
            HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();

            String urlParameters ="";


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/


        assert btnHit != null;
        btnHit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new JSONTask().execute("https://api.justpark.com/apiv3/search/region");//new
            }
        });
    }

    public class JSONTask extends AsyncTask<String, String, String>{

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

                JSONObject finalobject = parentArray.getJSONObject(0);

                String latWembley = finalobject.getString("address_lat");//get the wembley lat
                String langWembley = finalobject.getString("address_lng");

                return latWembley + "and lang is" + langWembley;//display wembley lat lang

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
            super.onPostExecute(s);
            mymapData.setText(s);
        }
    }

    private void buttonClick(int numbers){
        switch(numbers) {
            case 1:
                startActivity(new Intent("com.example.user.justparknew20.MapsActivity"));
                break;
        }
    }

    public void onClick(View pass){
        switch(pass.getId())
        {
            case R.id.mapPage:
                buttonClick(1);
                break;
        }
    }



}
