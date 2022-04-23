package com.example.android.newsapp;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class NewsAdapter extends ArrayAdapter<News> {
    private final Context mContext;
    private TextView mDate, mTime;

    public NewsAdapter(Activity context, ArrayList<News> news) {
        super(context, 0, news);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.news_list_item, parent, false);
        }

        ImageView newsImage = listItemView.findViewById(R.id.newsImage);
        TextView textViewTitle = listItemView.findViewById(R.id.newsTitle);
        mDate = listItemView.findViewById(R.id.newsDate);
        mTime = listItemView.findViewById(R.id.newsTime);

        News currentNews = getItem(position);
        Glide.with(mContext)
                .load(currentNews.getUrlToImage())
                .placeholder(R.drawable.backdrop_noimage)
                .into(newsImage);

        textViewTitle.setText(currentNews.getTitle());
        setDateAndTime(currentNews.getPublishedAt());

        return listItemView;
    }

    private void setDateAndTime(String publishedAt) {
        SimpleDateFormat readDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        readDate.setTimeZone(TimeZone.getTimeZone("GMT"));

        Date date = null;
        try {
            date = readDate.parse(publishedAt);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat writeDate = new SimpleDateFormat("dd MMM, yyyy 'T' hh:mm aa", Locale.US);
        writeDate.setTimeZone(TimeZone.getTimeZone("GMT+05:30"));

        publishedAt = writeDate.format(date);

        String actualDate = "";
        String actualTime = "";

        if (publishedAt.contains(" T ")) {
            String[] parts = publishedAt.split(" T ");
            actualDate = parts[0];
            actualTime = parts[1];
        }

        mDate.setText(actualDate);
        mTime.setText(actualTime);
    }
}
