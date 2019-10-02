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

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    private Context context;
    private List<News> list;
    private MyNewsListener listener;


    interface MyNewsListener {
        void onNewsClicked(int position, View view);
    }

    public void setListener(MyNewsListener listener) {
        this.listener = listener;
    }

    public NewsAdapter(Context context, List<News> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.news_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        News news = list.get(position);

        holder.textTitle.setText(news.getTitle());
        holder.textAuthor.setText(String.valueOf(news.getAuthor()));
        holder.textDescription.setText(String.valueOf(news.getDescription()));
        Picasso.with(context).load(news.getUrlToImage()).into(holder.image);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textTitle, textAuthor, textDescription;
        private ImageView image ;

        public ViewHolder(View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.newsImage);
            textTitle = itemView.findViewById(R.id.newsTitle);
            textDescription = itemView.findViewById(R.id.newsDescription);
            textAuthor = itemView.findViewById(R.id.newsAuthor);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(listener!=null)
                        listener.onNewsClicked(getAdapterPosition(),view);
                }
            });
        }
    }
}
