package com.example.gziolle.popmovies;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by gziolle on 10/19/2016.
 */

public class DetailFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            bindView(bundle);
        }

    }

    public void bindView(Bundle bundle) {

        TextView title = (TextView) getActivity().findViewById(R.id.title);
        title.setText(bundle.getString(MovieListFragment.TMDB_TITLE));

        ImageView moviePoster = (ImageView) getActivity().findViewById(R.id.movie_image);
        String posterUrl = bundle.getString(MovieListFragment.TMDB_POSTER_PATH);
        Picasso.with(getActivity()).load(posterUrl)
                .error(R.mipmap.ic_launcher).fit().into(moviePoster);

        TextView releaseDate = (TextView) getActivity().findViewById(R.id.release_date);
        releaseDate.setText(bundle.getString(MovieListFragment.TMDB_RELEASE_DATE));

        TextView voteAverage = (TextView) getActivity().findViewById(R.id.average);
        voteAverage.setText(bundle.getDouble(MovieListFragment.TMDB_VOTE_AVERAGE) + "/10");

        TextView overview = (TextView) getActivity().findViewById(R.id.overview);
        overview.setText(bundle.getString(MovieListFragment.TMDB_OVERVIEW));

    }
}
