package com.example.webservicesapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.ViewHolder> {

    private Context context;
    private List<Weather> list;
    private MyWeatherListener listener;


    interface MyWeatherListener {
        void onWeatherClicked(int position, View view);
    }

    public void setListener(MyWeatherListener listener) {
        this.listener = listener;
    }

    public WeatherAdapter(Context context, List<Weather> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.weather_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Weather weather = list.get(position);

        holder.textCity.setText(weather.getCity());
        holder.textDegree.setText(String.valueOf(weather.getDegree())+" °C");
        holder.textWind.setText(String.valueOf(weather.getWind())+" km/h");
        holder.textMinTemp.setText(String.valueOf(weather.getMinTemp())+" °C");
        holder.textMaxTemp.setText(String.valueOf(weather.getMaxTemp())+" °C");

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textCity, textDegree, textWind ,textMinTemp, textMaxTemp ;

        public ViewHolder(View itemView) {
            super(itemView);

            textCity = itemView.findViewById(R.id.weatherCity);
            textWind = itemView.findViewById(R.id.weatherWind);
            textDegree = itemView.findViewById(R.id.weatherDegree);
            textMinTemp = itemView.findViewById(R.id.weatherMin);
            textMaxTemp = itemView.findViewById(R.id.weatherMax);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(listener!=null)
                        listener.onWeatherClicked(getAdapterPosition(),view);
                }
            });
        }
    }
}
