package com.example.gziolle.popmovies;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
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
    private RecyclerView.Adapter mTrailerAdapter;
    private ImageButton mImageButton;
    private Bundle mBundle;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        RecyclerView trailerRecyclerView = (RecyclerView) rootView.findViewById(R.id.trailer_list);
        trailerRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager trailerLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        trailerRecyclerView.setLayoutManager(trailerLayoutManager);

        mTrailerAdapter = new TrailerAdapter(getActivity(), mMovieTrailers, this);
        trailerRecyclerView.setAdapter(mTrailerAdapter);

        mReviewLayout = (LinearLayout) rootView.findViewById(R.id.review_list);

        mImageButton = (ImageButton) rootView.findViewById(R.id.favorite_button);

        //TODO
        // make a query to check id the movie is already a favorite one.
        // If so, set the ImageButton as selected.
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mImageButton.isSelected()) {
                    String posterFileName = mBundle.getString(FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH);
                    Bitmap poster = Utility.getImageFromUrl(getActivity(), Utility.POSTER_PATH_AUTHORITY + posterFileName);
                    if (poster != null) {
                        String filePath = Utility.savePosterIntoStorage(mBundle, getActivity(), poster);
                        mBundle.putString(FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH, filePath);
                        Log.d("Ziolle", "posterUrl = " + filePath);
                    }

                    //Try to add movie into the database.
                    //If it does not work, a toast is displayed to the user.
                    if (Utility.insertMovieIntoDB(mBundle, getActivity())) {
                        mImageButton.setSelected(true);
                    } else {
                        Toast.makeText(getActivity(), "Couldn't save this movie as a favorite", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    //delete movie from the database
                    if (Utility.deleteMovieFromDB(mBundle, getActivity())) {
                        mImageButton.setSelected(false);
                    } else {
                        Toast.makeText(getActivity(), "Couldn't delete this movie from the favorite's lisr", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
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

        TextView title = (TextView) getActivity().findViewById(R.id.title);
        title.setText(bundle.getString(FavoritesContract.FavoritesEntry.COLUMN_TITLE));

        ImageView moviePoster = (ImageView) getActivity().findViewById(R.id.movie_image);
        String moviePosterPath = bundle.getString(FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH);

        if (!moviePosterPath.startsWith("/data")) {
            moviePosterPath = Utility.POSTER_PATH_AUTHORITY + moviePosterPath;
            //Download the image using Picasso API
            Picasso.with(getActivity()).load(moviePosterPath)
                    .error(R.mipmap.ic_launcher).fit().into(moviePoster);
        } else {
            File posterFile = new File(moviePosterPath);
            Picasso.with(getActivity()).load(posterFile)
                    .error(R.mipmap.ic_launcher).fit().into(moviePoster);
        }

        TextView releaseDate = (TextView) getActivity().findViewById(R.id.release_date);
        releaseDate.setText(bundle.getString(FavoritesContract.FavoritesEntry.COLUMN_RELEASE_DATE));

        TextView voteAverage = (TextView) getActivity().findViewById(R.id.average);
        String average = String.format(getActivity().getResources().getString(R.string.average_note), bundle.getDouble(FavoritesContract.FavoritesEntry.COLUMN_AVERAGE));
        voteAverage.setText(average);

        TextView overview = (TextView) getActivity().findViewById(R.id.overview);
        overview.setText(bundle.getString(FavoritesContract.FavoritesEntry.COLUMN_OVERVIEW));

        ImageButton favoriteButton = (ImageButton) getActivity().findViewById(R.id.favorite_button);
        favoriteButton.setVisibility(View.VISIBLE);

        String[] projection = {FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID};
        String[] selectionArgs = {String.valueOf(bundle.getLong(FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID))};

        String selection = FavoritesContract.FavoritesEntry.TABLE_NAME + "." + FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID + " = ?";

        Cursor retCursor = getActivity().getContentResolver().query(FavoritesContract.FavoritesEntry.CONTENT_URI, projection, selection, selectionArgs, null);

        try {
            if (retCursor.moveToFirst()) {
                favoriteButton.setSelected(true);
            }
        } catch (NullPointerException ex) {
            Log.e("Ziolle", ex.getMessage());
        } finally {
            if (retCursor != null && !retCursor.isClosed()) {
                retCursor.close();
            }
        }

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
}

