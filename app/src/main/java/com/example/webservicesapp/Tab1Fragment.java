package com.example.webservicesapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;

import static android.content.Context.MODE_PRIVATE;

public class Tab1Fragment extends Fragment {

    private RecyclerView rv;
    private List<News> newsList;
    private NewsAdapter adapter;
    private String url = "https://newsapi.org/v2/top-headlines?country=il&apiKey=bc1638f71db649d5913040c6784fc804";
    private ImageView refreshBtnNews;
    private Button notificationBtn;
    private TextView tv;
    private String categorie;
    private int time;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab1_fragment,container,false);

        rv = view.findViewById(R.id.recycler);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        newsList =new ArrayList<>();
        adapter = new NewsAdapter(getActivity(),newsList);
        rv.setAdapter(adapter);
        settings = getActivity().getSharedPreferences("PREFERENCES",MODE_PRIVATE);
        editor = settings.edit();
        getData();
        adapter.setListener(new NewsAdapter.MyNewsListener() {
            @Override
            public void onNewsClicked(int position, View view) {

                String url=  newsList.get(position).getUrl();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        tv = view.findViewById(R.id.timeTVnews);
        refreshBtnNews = view.findViewById(R.id.refreshNews);
        refreshBtnNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getData();
            }
        });

        notificationBtn = view.findViewById(R.id.notificationsBtn);

        if(readSetting("service").equals("Stop Notifications"))
        {
            notificationBtn.setText("Stop Notifications");
        }

        notificationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String s = (String) ((Button) v).getText();
                if (s.equals("Start Notifications")) {
                    editSetting("service", "Start Notifications");


                    ArrayList<String> items = new ArrayList<>();
                    final SpinnerDialog spinnerDialog;

                    items.add("Business");
                    items.add("Entertainment");
                    items.add("Health");
                    items.add("Science");
                    items.add("Sports");
                    items.add("Technology");


                    spinnerDialog = new SpinnerDialog(getActivity(), items, "Select an interest.", R.style.DialogAnimations_SmileWindow, "Close");// With 	Animation

                    spinnerDialog.setCancellable(false); // for cancellable
                    spinnerDialog.setShowKeyboard(false);// for open keyboard by default
                    spinnerDialog.showSpinerDialog();

                    spinnerDialog.bindOnSpinerListener(new OnSpinerItemClick() {
                        @Override
                        public void onClick(String item, int position) {
                            categorie = item;
                            timeChooser();
                        }
                    });
                }else if(s.equals("Stop Notifications")){

                    Intent intent = new Intent(getActivity(), NewsService.class);
                    getActivity().stopService(intent);
                    notificationBtn.setText("Start Notifications");
                    editSetting("service", "Start Notifications");
                    Toast.makeText(getActivity(), "Notifications are turned off.", Toast.LENGTH_LONG).show();
                }
            }
        });

        return view;
    }

    void timeChooser(){

        final AlertDialog.Builder d = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.number_picker_dialog, null);

        d.setTitle("Select times");
        d.setMessage("Please select a time (in minutes) where you'd to be notified about the last article.");
        d.setView(dialogView);

        final NumberPicker numberPicker = (NumberPicker) dialogView.findViewById(R.id.dialog_number_picker);
        numberPicker.setMaxValue(60);
        numberPicker.setMinValue(1);
        numberPicker.setWrapSelectorWheel(true);
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
            }
        });
        d.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                time= numberPicker.getValue();
                dialogInterface.dismiss();

                    Intent intent = new Intent(getActivity(), NewsService.class);
                    intent.putExtra("time",time);
                     intent.putExtra("categorie",categorie);

                if (Build.VERSION.SDK_INT >= 26) {
                        getActivity().startForegroundService(intent);
                    }else {
                        getActivity().startService(intent);
                        Log.d("notif",time+" "+categorie);
                    }

                    notificationBtn.setText("Stop Notifications");
                editSetting("service", "Stop Notifications");
                Toast.makeText(getActivity(), "You will now be notified on "+categorie+" articles every "+time+" minutes.", Toast.LENGTH_LONG).show();
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


    private void getData() {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("articles");
                            String author , urlToImage , description;
                            String nullable = "null";

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject articles = jsonArray.getJSONObject(i);

                                News news = new News();
                                news.setTitle(articles.getString("title"));
                                news.setUrl(articles.getString("url"));
                                urlToImage = (articles.getString("urlToImage"));
                                author = (articles.getString("author"));
                                description = (articles.getString("description"));

                                    if(!urlToImage.startsWith("http")){
                                        urlToImage = "https://i.imgur.com/CH5XD9H.jpg";
                                    }
                                    if(author.equals(nullable)){
                                        author = "לא קיים שם הכתב";
                                    }
                                    if(description.equals(nullable)){
                                        description = "לא קיים תיאור";
                                    }

                                    news.setUrlToImage(urlToImage);
                                    news.setAuthor(author);
                                    news.setDescription(description);

                                newsList.add(news);
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



        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        requestQueue.add(request);
    }


    String time(){
        String mydate;
        return  mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
    }
    public String readSetting(String key)
    {
        String value;
        value = settings.getString(key, "");
        return value;
    }

    public void editSetting(String key, String value)
    {
        editor.putString(key,value);
        editor.commit();
    }

}
