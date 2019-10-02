package com.example.webservicesapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class NewsService extends Service {

    private int time;
    private String categorie;
    Handler handler;
    Timer timer;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        time = intent.getIntExtra("time", 1);
        categorie = intent.getStringExtra(("categorie"));

        handler = new Handler();
        final Intent myIntent = intent;

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        performCall(myIntent);

                    }
                });
            }
        };


        timer = new Timer();
        long freq = time*60*1000;
        timer.schedule(timerTask, freq);

        return super.onStartCommand(intent, flags, startId);

    }


    public void performCall(final Intent intent){

        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        String REQUEST_URL = "https://newsapi.org/v2/top-headlines?country=il&category="+ categorie +"&apiKey=bc1638f71db649d5913040c6784fc804";
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(0,
                REQUEST_URL,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONArray articles =null;
                        try {
                            articles =response.getJSONArray("articles");
                            JSONObject jsonObject = articles.getJSONObject(0);

                            String title = jsonObject.getString("title");
                            String description = jsonObject.getString("description");

                            Intent intent1= new Intent(Intent.ACTION_VIEW, Uri.parse(jsonObject.getString("url")));
                            PendingIntent pi = PendingIntent.getActivity(NewsService.this, 1, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

                            NotificationCompat.Builder builder = new NotificationCompat.Builder(NewsService.this,App.CHANNEL_1_ID)
                                    .setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("Article-service")
                                    .setContentTitle(title).setContentIntent(pi).setContentText(description)
                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                                    .setShowWhen(true);


                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                startForeground(1, builder.build());
                            }else {
                                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                manager.notify(1, builder.build());
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        requestQueue.add(jsonObjectRequest);

    }

    @Override
    public void onDestroy() {
        timer.cancel();
        super.onDestroy();
    }

}




