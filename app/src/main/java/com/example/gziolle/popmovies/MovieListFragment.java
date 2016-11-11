package com.example.gziolle.popmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


/**
 * Created by gziolle on 10/18/2016.
 */

public class MovieListFragment extends Fragment {

    public static final String LOG_TAG = MovieListFragment.class.getSimpleName();

    public static String TMDB_RESULTS = "results";
    public static String TMDB_ID = "id";
    public static String TMDB_TITLE = "title";
    public static String TMDB_POSTER_PATH = "poster_path";
    public static String TMDB_OVERVIEW = "overview";
    public static String TMDB_VOTE_AVERAGE = "vote_average";
    public static String TMDB_RELEASE_DATE = "release_date";

    public GridView mGridView;
    public MovieAdapter mMovieAdapter;
    public ArrayList<MovieItem> mMovieItems;
    private int currentPage = 0;
    private String lastQueryMode = "";
    private boolean mIsFetching = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_movie_list, container, false);
        mGridView = (GridView) rootView.findViewById(R.id.gridview);

        mMovieItems = new ArrayList<>();
        mMovieAdapter = new MovieAdapter(getActivity(), R.layout.grid_view_item, mMovieItems);

        mGridView.setAdapter(mMovieAdapter);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                MovieItem item = mMovieAdapter.getItem(position);

                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(TMDB_ID, item.get_id());
                intent.putExtra(TMDB_TITLE, item.getTitle());
                intent.putExtra(TMDB_POSTER_PATH, item.getPosterPath());
                intent.putExtra(TMDB_RELEASE_DATE, item.getReleaseDate());
                intent.putExtra(TMDB_OVERVIEW, item.getOverview());
                intent.putExtra(TMDB_VOTE_AVERAGE, item.getAverage());
                startActivity(intent);
            }
        });

        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScroll(AbsListView absListView,int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int currentItem = firstVisibleItem + visibleItemCount;
                if(currentItem == totalItemCount && !mIsFetching){
                    updateMovieList();
                }
            }
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }
        });
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovieList();

    }

    public void updateMovieList() {
        ConnectivityManager manager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected()) {
            //Get the sharedPreference and start the AsyncTask

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

            String queryMode = prefs.getString(getString(R.string.query_mode_key), getString(R.string.query_mode_default));
            if (lastQueryMode.equals("")) {
                lastQueryMode = queryMode;
            } else if (!lastQueryMode.equals(queryMode)) {
                mMovieItems.clear();
                currentPage = 1;
                lastQueryMode = queryMode;
            } else {
                currentPage++;
            }

            new FetchMoviesTask().execute(queryMode, String.valueOf(currentPage));

        } else {
            Toast.makeText(getActivity(), "connectivity error", Toast.LENGTH_SHORT).show();
        }
    }

    class FetchMoviesTask extends AsyncTask<String, Void, ArrayList<MovieItem>> {

        private String TMDB_AUTHORITY = "api.themoviedb.org";
        private String TMDB_API_VERSION = "3";
        private String TMDB_MOVIE_DIR = "movie";
        private String TMDB_API_KEY = "api_key";
        private String TMDB_LANGUAGE = "language";
        private String TMDB_PAGE = "page";

        @Override
        protected ArrayList<MovieItem> doInBackground(String... params) {
            mIsFetching = true;
            //https://api.themoviedb.org/3/movie/popular?api_key=35b914867b56190b284ba099bce2cb35&language=en-US

            HttpURLConnection conn = null;
            InputStream is;
            BufferedReader reader = null;
            String moviesJSONString;
            ArrayList<MovieItem> movieItems = null;

            if (params[0] == null) {
                return null;
            }

            String queryMode = params[0];
            String currentPage = params[1];

            try {
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http");
                builder.authority(TMDB_AUTHORITY);
                builder.appendPath(TMDB_API_VERSION).appendPath(TMDB_MOVIE_DIR).appendPath(queryMode);
                builder.appendQueryParameter(TMDB_API_KEY, BuildConfig.THE_MOVIE_DB_KEY);
                builder.appendQueryParameter(TMDB_LANGUAGE, "en-us");
                builder.appendQueryParameter(TMDB_PAGE, currentPage);

                URL queryUrl = new URL(builder.build().toString());

                conn = (HttpURLConnection) queryUrl.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                is = conn.getInputStream();

                //Error handling
                if (is == null) {
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
                    return null;
                }

                moviesJSONString = buffer.toString();

                movieItems = getDataFromJSON(moviesJSONString);

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
            return movieItems;
        }

        @Override
        protected void onPostExecute(ArrayList<MovieItem> result) {
            mIsFetching = false;
            if (result != null) {
                mMovieItems.addAll(result);
                mMovieAdapter.notifyDataSetChanged();
            }
            super.onPostExecute(result);
        }

        ArrayList<MovieItem> getDataFromJSON(String JSONString) throws JSONException {
            ArrayList<MovieItem> movieItems = new ArrayList<>();

            JSONObject mainObject = new JSONObject(JSONString);

            JSONArray moviesArray = mainObject.getJSONArray(TMDB_RESULTS);

            for (int i = 0; i < moviesArray.length(); i++) {
                JSONObject movie = moviesArray.getJSONObject(i);
                //long _id, String original_title, String posterPath, String overview, double vote_average, String releaseDate
                MovieItem item = new MovieItem(movie.getLong(TMDB_ID), movie.getString(TMDB_TITLE),
                        movie.getString(TMDB_POSTER_PATH), movie.getString(TMDB_OVERVIEW),
                        movie.getDouble(TMDB_VOTE_AVERAGE), movie.getString(TMDB_RELEASE_DATE));
                movieItems.add(item);
            }
            return movieItems;
        }
    }
}
