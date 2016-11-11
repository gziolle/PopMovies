package com.example.gziolle.popmovies;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.TextView;

import com.example.gziolle.popmovies.data.FavoritesContract;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import android.support.v7.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by gziolle on 10/19/2016.
 */

public class DetailFragment extends Fragment implements TrailerAdapter.RecyclerViewClickListener {

    public static final String LOG_TAG = DetailFragment.class.getSimpleName();

    public ArrayList<TrailerItem> mMovieTrailers = new ArrayList<>();
    //public ArrayList<String> mMovieReviews = new ArrayList<>();

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ImageButton mImageButton;
    private Bundle mBundle;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.trailer_list);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new TrailerAdapter(getActivity(), mMovieTrailers, this);
        mRecyclerView.setAdapter(mAdapter);

        mImageButton = (ImageButton) rootView.findViewById(R.id.favorite_button);

        //TODO
        // make a query to check id the movie is already a favorite one.
        // If so, set the imagebutton as selected.
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mImageButton.isSelected()) {
                    mImageButton.setSelected(true);
                    //add movie to the database
                    ContentValues values = new ContentValues();
                    values.put(FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID, String.valueOf(mBundle.getLong(MovieListFragment.TMDB_ID)));
                    values.put(FavoritesContract.FavoritesEntry.COLUMN_TITLE, mBundle.getString(MovieListFragment.TMDB_TITLE));
                    values.put(FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH, mBundle.getString(MovieListFragment.TMDB_POSTER_PATH));
                    values.put(FavoritesContract.FavoritesEntry.COLUMN_OVERVIEW, mBundle.getString(MovieListFragment.TMDB_OVERVIEW));
                    values.put(FavoritesContract.FavoritesEntry.COLUMN_AVERAGE, String.valueOf(mBundle.getDouble(MovieListFragment.TMDB_VOTE_AVERAGE)));
                    values.put(FavoritesContract.FavoritesEntry.COLUMN_RELEASE_DATE, mBundle.getString(MovieListFragment.TMDB_RELEASE_DATE));

                    Uri rowUri = getActivity().getContentResolver().insert(FavoritesContract.FavoritesEntry.CONTENT_URI, values);
                } else {
                    mImageButton.setSelected(false);
                    //delete movie from the database

                    String selection = FavoritesContract.FavoritesEntry.TABLE_NAME + "." + FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID + " = ?";
                    String[] selectionArgs = {String.valueOf(mBundle.getLong(MovieListFragment.TMDB_ID))};

                    int rowsCount = getActivity().getContentResolver().delete(FavoritesContract.FavoritesEntry.CONTENT_URI, selection, selectionArgs);
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

    private void updateTrailerList(Long id) {
        ConnectivityManager manager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        if (networkInfo.isAvailable() && networkInfo.isConnected()) {
            new FetchTrailersTask().execute(String.valueOf(id));
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

        try {
            updateTrailerList(bundle.getLong(MovieListFragment.TMDB_ID));
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

    class FetchTrailersTask extends AsyncTask<String, Void, ArrayList<TrailerItem>> {

        private static final String RESULTS = "results";
        private static final String KEY = "key";
        private static final String NAME = "name";
        private final String LOG_TAG = FetchTrailersTask.class.getSimpleName();
        private String TMDB_AUTHORITY = "api.themoviedb.org";
        private String TMDB_API_VERSION = "3";
        private String TMDB_MOVIE_DIR = "movie";
        private String TMDB_MOVIE_VIDEOS = "videos";
        private String TMDB_API_KEY = "api_key";
        private String TMDB_LANGUAGE = "language";
        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage(getActivity().getString(R.string.progress_message));
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }


        @Override
        protected ArrayList<TrailerItem> doInBackground(String... params) {

            HttpURLConnection conn = null;
            InputStream is;
            BufferedReader reader = null;
            String jResult;
            ArrayList<TrailerItem> trailerList = new ArrayList<>();

            if (params[0] == null) {
                Log.e(LOG_TAG, "params[0] == null");
                return null;
            }

            try {
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http");
                builder.authority(TMDB_AUTHORITY);
                builder.appendPath(TMDB_API_VERSION).appendPath(TMDB_MOVIE_DIR).appendPath(params[0]).appendPath(TMDB_MOVIE_VIDEOS);
                builder.appendQueryParameter(TMDB_API_KEY, BuildConfig.THE_MOVIE_DB_KEY);
                builder.appendQueryParameter(TMDB_LANGUAGE, "en-us");

                URL queryUrl = new URL(builder.build().toString());

                conn = (HttpURLConnection) queryUrl.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                is = conn.getInputStream();

                //Error handling
                if (is == null) {
                    Log.e(LOG_TAG, "is == null");
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(is));

                String line;
                StringBuffer buffer = new StringBuffer();

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                //Error handling
                if (buffer.length() == 0) {
                    Log.e(LOG_TAG, "buffer.length() == 0");
                    return null;
                }

                jResult = buffer.toString();

                trailerList = getDataFromJSON(jResult);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());

            } finally {
                if (conn != null) {
                    conn.disconnect();
                }

                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e) {
                        Log.e(LOG_TAG, e.getMessage());
                    }
                }
            }
            return trailerList;
        }

        @Override
        protected void onPostExecute(ArrayList<TrailerItem> trailerList) {
            super.onPostExecute(trailerList);
            mProgressDialog.dismiss();

            if (trailerList != null) {
                mMovieTrailers.addAll(trailerList);
                mAdapter.notifyDataSetChanged();
            }
        }

        private ArrayList<TrailerItem> getDataFromJSON(String jString) throws JSONException {
            ArrayList<TrailerItem> trailerList = new ArrayList<>();

            JSONObject mainObject = new JSONObject(jString);
            JSONArray trailerArray = mainObject.getJSONArray(RESULTS);

            for (int i = 0; i < trailerArray.length(); i++) {
                JSONObject trailer = trailerArray.getJSONObject(i);
                TrailerItem item = new TrailerItem(trailer.getString(KEY), trailer.getString(NAME));
                trailerList.add(item);
            }

            return trailerList;
        }
    }
}
