package com.example.gziolle.popmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by gziolle on 10/18/2016.
 */

public class MovieAdapter extends ArrayAdapter<MovieItem> {

    private Context mContext;
    private ArrayList<MovieItem> movieList;


    public MovieAdapter(Context context, int resource, ArrayList<MovieItem> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.movieList = objects;
    }

    @Override
    public int getCount() {
        return movieList.size();
    }

    @Override
    public MovieItem getItem(int i) {
        return movieList.get(i);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        MovieItem movieItem = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.grid_view_item, viewGroup, false);

        }
        ImageView imageView = (ImageView) convertView.findViewById(R.id.poster);
        //Download the image using Picasso API

        Picasso.with(mContext).load(movieItem.posterPath)
                .error(R.mipmap.ic_launcher).fit().into(imageView);

        imageView.setAdjustViewBounds(true);

        return convertView;
    }
}
