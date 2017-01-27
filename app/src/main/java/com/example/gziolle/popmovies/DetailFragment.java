package com.example.gziolle.popmovies;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.gziolle.popmovies.data.FavoritesContract;
import com.example.gziolle.popmovies.utils.FetchReviewsTask;
import com.example.gziolle.popmovies.utils.FetchTrailersTask;
import com.example.gziolle.popmovies.utils.Utility;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

/**
 * Created by gziolle on 10/19/2016.
 */

public class DetailFragment extends Fragment implements TrailerAdapter.RecyclerViewClickListener, FetchReviewsTask.AsyncResponse, FetchTrailersTask.AsyncResponse {

    public static final String LOG_TAG = DetailFragment.class.getSimpleName();
    static final String DETAIL_FRAGMENT_TAG = "DFTAG";

    public ArrayList<TrailerItem> mMovieTrailers = new ArrayList<>();
    ViewGroup mReviewLayout;
    Toolbar mToolbar;
    private RecyclerView.Adapter mTrailerAdapter;
    private Bundle mBundle;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("Ziolle", "onCreateView");

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(mToolbar);

        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.d("Ziolle", "portrait");
            mToolbar.setPadding(0, getStatusBarHeight(), 0, 0);
        }

        activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView trailerRecyclerView = (RecyclerView) rootView.findViewById(R.id.trailer_list);
        RecyclerView.LayoutManager trailerLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        trailerRecyclerView.setLayoutManager(trailerLayoutManager);
        mTrailerAdapter = new TrailerAdapter(getActivity(), mMovieTrailers, this);
        trailerRecyclerView.setAdapter(mTrailerAdapter);

        mReviewLayout = (LinearLayout) rootView.findViewById(R.id.review_list);
        /*if(getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            mImageButton = (ImageButton) rootView.findViewById(R.id.favorite_button);
            //TODO
            // make a query to check id the movie is already a favorite one.
            // If so, set the ImageButton as selected.
            mImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!mImageButton.isSelected()) {
                        String posterUrl = mBundle.getString(FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH);
                        if (posterUrl != null) {
                            new DownloadImageTask().execute(posterUrl);
                        }
                    } else {
                        //delete movie from the database
                        if (Utility.deleteMovieFromDB(mBundle, getActivity())) {
                            mImageButton.setSelected(false);
                        } else {
                            Toast.makeText(getActivity(), "Couldn't delete this movie from the favorite's list", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }*/
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mBundle = bundle;
            bindView(mBundle);
        }
    }

    private void updateTrailerAndReviewList(Long id) {
        ConnectivityManager manager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        if (networkInfo.isAvailable() && networkInfo.isConnected()) {
            new FetchTrailersTask(getActivity(), this).execute(String.valueOf(id));
            new FetchReviewsTask(this).execute(String.valueOf(id));
        }
    }

    public void bindView(Bundle bundle) {

        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mToolbar.setTitle(bundle.getString(FavoritesContract.FavoritesEntry.COLUMN_TITLE));
        }

        ImageView moviePoster = (ImageView) getActivity().findViewById(R.id.movie_image);
        String moviePosterPath = bundle.getString(FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH);

        if (!moviePosterPath.startsWith("/data")) {
            moviePosterPath = Utility.POSTER_PATH_BIG_AUTHORITY + moviePosterPath;
            Log.d("Ziolle", "url = " + moviePosterPath);
            //Download the image using Picasso API
            Picasso.with(getActivity()).load(moviePosterPath)
                    .error(R.mipmap.ic_launcher).resize(500, 750).centerCrop().into(moviePoster);
        } else {
            File posterFile = new File(moviePosterPath);
            Picasso.with(getActivity()).load(posterFile)
                    .error(R.mipmap.ic_launcher).resize(500, 750).centerCrop().into(moviePoster);
        }

        TextView releaseDate = (TextView) getActivity().findViewById(R.id.release_date);
        releaseDate.setText(bundle.getString(FavoritesContract.FavoritesEntry.COLUMN_RELEASE_DATE));

        TextView voteAverage = (TextView) getActivity().findViewById(R.id.average);
        String average = String.format(getActivity().getResources().getString(R.string.average_note), bundle.getDouble(FavoritesContract.FavoritesEntry.COLUMN_AVERAGE));
        voteAverage.setText(average);

        TextView overview = (TextView) getActivity().findViewById(R.id.overview);
        overview.setText(bundle.getString(FavoritesContract.FavoritesEntry.COLUMN_OVERVIEW));

        try {
            updateTrailerAndReviewList(bundle.getLong(FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID));
        } catch (NullPointerException nex) {
            Log.e(LOG_TAG, "NullPointerException " + nex.getMessage());
        }
    }

    @Override
    public void recyclerViewListClicked(View v, int position) {
        TrailerItem trailer = mMovieTrailers.get(position);
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(trailer.appUrl));
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(trailer.browserUrl));

        try {
            startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            startActivity(browserIntent);
        }
    }

    @Override
    public void updateTrailers(ArrayList<TrailerItem> trailers) {
        if (trailers.size() != 0) {
            TextView trailerListTitle = (TextView) getActivity().findViewById(R.id.trailers_title);
            trailerListTitle.setVisibility(View.VISIBLE);
        }
        mMovieTrailers.addAll(trailers);
        mTrailerAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateReviews(ArrayList<ReviewItem> reviews) {

        if (reviews.size() != 0) {
            TextView reviewListTitle = (TextView) mReviewLayout.findViewById(R.id.review_title);
            reviewListTitle.setVisibility(View.VISIBLE);
        }

        for (ReviewItem item : reviews) {
            View layout = LayoutInflater.from(getActivity()).inflate(R.layout.review_item, mReviewLayout, false);

            TextView reviewContent = (TextView) layout.findViewById(R.id.review_content);
            reviewContent.setText(item.getContent());

            TextView reviewAuthor = (TextView) layout.findViewById(R.id.review_author);
            String author = String.format(getActivity().getResources().getString(R.string.by_author), item.getAuthor());
            reviewAuthor.setText(author);

            if (mReviewLayout != null) {
                mReviewLayout.addView(layout);
            }
        }
    }

    // A method to find height of the status bar
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        Log.d("Ziolle", "result = " + result);
        return result;
    }
}

