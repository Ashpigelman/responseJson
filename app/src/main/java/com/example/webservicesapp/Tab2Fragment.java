package com.example.webservicesapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.support.v4.content.ContextCompat.checkSelfPermission;


public class Tab2Fragment extends Fragment{

    private RecyclerView rv2;
    private List<Weather> weatherList;
    private WeatherAdapter adapter;
    private double latitude;
    private double longitude;
    private String url ;
    private ProgressDialog progressDialog;
    private Button radiusBtn;
    private ImageView refreshBtn;
    private TextView tv;
    int radius = 5;

    final int LOCATION_PERMISSION_REQUEST = 1;
    FusedLocationProviderClient client;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == LOCATION_PERMISSION_REQUEST) {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "You must enable GPS permissions!", Toast.LENGTH_LONG).show();
            }
            else startLocation();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab2_fragment,container,false);

        rv2 = view.findViewById(R.id.recyclerWeather);
        rv2.setHasFixedSize(true);
        rv2.setLayoutManager(new LinearLayoutManager(getActivity()));
        weatherList =new ArrayList<>();
        adapter = new WeatherAdapter(getActivity(),weatherList);
        rv2.setAdapter(adapter);
        tv = view.findViewById(R.id.timeTVweather);


        checkGPS();

        if(Build.VERSION.SDK_INT >= 23) {
            int hasLocationPermission = checkSelfPermission(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION);
            if(hasLocationPermission!= PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_PERMISSION_REQUEST);
            }
            else startLocation();
        }
        else{ startLocation();
        }
        radiusBtn = view.findViewById(R.id.radiusBtn);
        radiusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final AlertDialog.Builder d = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.number_picker_dialog, null);

                TextView textView = new TextView(getActivity());
                textView.setText("Select a radius");
                textView.setPadding(20, 50, 20, 50);
                textView.setTextSize(20F);
                textView.setBackgroundColor(Color.parseColor("#b10cadb3"));
                textView.setTextColor(Color.WHITE);
                textView.setGravity(Gravity.CENTER);

                d.setCustomTitle(textView);
                d.setMessage("Please select a kilometer range where you'll see the weather in the cities around you.");
                d.setView(dialogView);
                final NumberPicker numberPicker = (NumberPicker) dialogView.findViewById(R.id.dialog_number_picker);
                numberPicker.setMaxValue(50);
                numberPicker.setMinValue(1);
                numberPicker.setWrapSelectorWheel(false);
                numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                    }
                });
                d.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        radius= numberPicker.getValue();
                        startLocation();
                        dialogInterface.dismiss();
                    }
                });
                d.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog alertDialog = d.create();
                alertDialog.show();
            }
        });
        refreshBtn = view.findViewById(R.id.refreshBTN);
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLocation();
            }
        });

        return view;
    }

    private void checkGPS() {
        final LocationManager manager = (LocationManager)getActivity().getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            final AlertDialog.Builder builder =
                    new AlertDialog.Builder(getActivity());
            final String message = "Please enable GPS"
                    + " to find current location.  Click OK to go to"
                    + " location settings.";


            builder.setMessage(message)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                    d.dismiss();
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            }).setCancelable(false);

            builder.create().show(); }
    }

    private void startLocation() {
        client = LocationServices.getFusedLocationProviderClient(getActivity());

        LocationCallback callback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Location lastLocation = locationResult.getLastLocation();

                latitude = lastLocation.getLatitude();
                longitude = lastLocation.getLongitude();

                url = "https://api.openweathermap.org/data/2.5/find?lat=" + latitude + "&lon=" + longitude + "&cnt="+radius+"&units=metric&appid=56462611c91d2d8da1dcf91a3e6dc44f";
                weatherList.clear();
                adapter.notifyDataSetChanged();

                try {
                    getData();
                }catch (Exception e){
                    e.getCause();
                }
            }


        };

        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(Build.VERSION.SDK_INT>=23 && checkSelfPermission(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            client.requestLocationUpdates(request,callback,null);
        else if(Build.VERSION.SDK_INT<=22)
            client.requestLocationUpdates(request,callback,null);


    }

    String time(){
        String mydate;
        return  mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
    }

    @Override
    public void onPause() {
        super.onPause();
        if ((progressDialog != null) && progressDialog.isShowing())
            progressDialog.dismiss();
        progressDialog = null;
    }

    private void getData() {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("list");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject wed = jsonArray.getJSONObject(i);

                                Weather weather = new Weather();
                                double temperature= Double.parseDouble(wed.getJSONObject("main").getString("temp"));
                                weather.setDegree(temperature);
                                weather.setCity(wed.getString("name"));
                                weather.setMinTemp(wed.getJSONObject("main").getString("temp_min"));
                                weather.setMaxTemp(wed.getJSONObject("main").getString("temp_max"));
                                weather.setWind(wed.getJSONObject("wind").getString("speed"));

                                weatherList.add(weather);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();
                        }
                        adapter.notifyDataSetChanged();
                        tv.setText(time());
                        progressDialog.dismiss();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });



        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(request);
    }


}

