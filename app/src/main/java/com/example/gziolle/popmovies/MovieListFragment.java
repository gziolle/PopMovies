package com.example.gziolle.popmovies;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
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

import com.example.gziolle.popmovies.data.FavoritesContract;
import com.example.gziolle.popmovies.utils.Utility;

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

    public static int ROW_ID = 0;
    public static int MOVIE_ID = 1;
    public static int MOVIE_TITLE = 2;
    public static int MOVIE_POSTER_PATH = 3;
    public static int MOVIE_OVERVIEW = 4;
    public static int MOVIE_AVERAGE = 5;
    public static int MOVIE_RELEASE_DATE = 6;


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
        mGridView = (GridView) rootView.findViewById(R.id.grid_view);

        mMovieItems = new ArrayList<>();
        mMovieAdapter = new MovieAdapter(getActivity(), R.layout.grid_view_item, mMovieItems);

        View emptyView = rootView.findViewById(R.id.empty_grid_view);
        mGridView.setEmptyView(emptyView);

        mGridView.setAdapter(mMovieAdapter);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                MovieItem item = mMovieAdapter.getItem(position);
                ((Callback) getActivity()).onItemSelected(item);
            }
        });

        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (!lastQueryMode.equals(getActivity().getString(R.string.query_mode_favorites))) {
                    int currentItem = firstVisibleItem + visibleItemCount;
                    if (Utility.isConnected(getActivity()) && currentItem == totalItemCount && !mIsFetching) {
                        updateMovieList();
                    }
                }
            }
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
            }
        });
        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Add the default query mode to lastQueryMode.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        lastQueryMode = prefs.getString(getString(R.string.query_mode_key), getString(R.string.query_mode_default));
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovieList();

    }

    public void updateMovieList() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String queryMode = prefs.getString(getString(R.string.query_mode_key), getString(R.string.query_mode_default));

        //Get the favorites and add them to the list. No need for a web query.

        if (queryMode.equals(getActivity().getString(R.string.query_mode_favorites))) {

            mMovieItems.clear();
            currentPage = 1;
            lastQueryMode = getActivity().getString(R.string.query_mode_favorites);

            new FetchMoviesTask().execute(queryMode, String.valueOf(currentPage));

        } else {
            if (Utility.isConnected(getActivity())) {
                //Get the preference's value and start the AsyncTask
                if (lastQueryMode.equals("")) {
                    lastQueryMode = queryMode;
                    currentPage = 1;
                } else if (!lastQueryMode.equals(queryMode)) {
                    mMovieItems.clear();
                    currentPage = 1;
                    lastQueryMode = queryMode;
                } else {
                    currentPage++;
                }
                new FetchMoviesTask().execute(queryMode, String.valueOf(currentPage));
            } else {
                //Since there is no connection, we'll clear the data source and notify that it has changed.
                mMovieItems.clear();
                mMovieAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        void onItemSelected(MovieItem item);
    }

    class FetchMoviesTask extends AsyncTask<String, Void, ArrayList<MovieItem>> {

        private static final String RESULTS = "results";
        ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage(getActivity().getString(R.string.progress_message));
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(true);
            mProgressDialog.show();
        }

        @Override
        protected ArrayList<MovieItem> doInBackground(String... params) {
            mIsFetching = true;
            //https://api.themoviedb.org/3/movie/popular?api_key=35b914867b56190b284ba099bce2cb35&language=en-US

            HttpURLConnection conn = null;
            InputStream is;
            BufferedReader reader = null;
            String moviesJSONString;
            ArrayList<MovieItem> movieList = new ArrayList<>();

            if (params[0] == null) {
                return null;
            }

            String queryMode = params[0];
            String currentPage = params[1];

            if (getActivity().getString(R.string.query_mode_favorites).equals(queryMode)) {
                movieList = getDataFromDatabase();
            }
            //Make a web query to retrieve date for the other options.
            else {
                try {
                    Uri.Builder builder = new Uri.Builder();
                    builder.scheme("http");
                    builder.authority(Utility.TMDB_AUTHORITY);
                    builder.appendPath(Utility.TMDB_API_VERSION).appendPath(Utility.TMDB_MOVIE_DIR).appendPath(queryMode);
                    builder.appendQueryParameter(Utility.TMDB_API_KEY, BuildConfig.THE_MOVIE_DB_KEY);
                    builder.appendQueryParameter(Utility.TMDB_LANGUAGE, "en-us");
                    builder.appendQueryParameter(Utility.TMDB_PAGE, currentPage);

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

                    movieList = getDataFromJSON(moviesJSONString);

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
            }
            return movieList;
        }

        @Override
        protected void onPostExecute(ArrayList<MovieItem> result) {
            super.onPostExecute(result);
            mProgressDialog.dismiss();
            if (result != null) {
                mMovieItems.addAll(result);
                mMovieAdapter.notifyDataSetChanged();
            }
            mIsFetching = false;
        }

        private ArrayList<MovieItem> getDataFromJSON(String JSONString) throws JSONException {
            ArrayList<MovieItem> movieItems = new ArrayList<>();
            JSONObject mainObject = new JSONObject(JSONString);

            JSONArray moviesArray = mainObject.getJSONArray(RESULTS);

            for (int i = 0; i < moviesArray.length(); i++) {
                JSONObject movie = moviesArray.getJSONObject(i);
                //long _id, String original_title, String posterPath, String overview, double vote_average, String releaseDate
                MovieItem item = new MovieItem(movie.getLong(Utility.TMDB_ID), movie.getString(FavoritesContract.FavoritesEntry.COLUMN_TITLE),
                        movie.getString(FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH), movie.getString(FavoritesContract.FavoritesEntry.COLUMN_OVERVIEW),
                        movie.getDouble(Utility.VOTE_AVERAGE), movie.getString(FavoritesContract.FavoritesEntry.COLUMN_RELEASE_DATE));
                movieItems.add(item);
            }
            return movieItems;
        }

        private ArrayList<MovieItem> getDataFromDatabase() {

            ArrayList<MovieItem> movieList = new ArrayList<>();
            String[] projection = {FavoritesContract.FavoritesEntry._ID,
                    FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID,
                    FavoritesContract.FavoritesEntry.COLUMN_TITLE,
                    FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH,
                    FavoritesContract.FavoritesEntry.COLUMN_OVERVIEW,
                    FavoritesContract.FavoritesEntry.COLUMN_AVERAGE,
                    FavoritesContract.FavoritesEntry.COLUMN_RELEASE_DATE};


            Cursor favoriteMovies = getActivity().getContentResolver().query(FavoritesContract.FavoritesEntry.CONTENT_URI, projection, null, null, null);

            if (favoriteMovies != null) {
                while (favoriteMovies.moveToNext()) {
                    MovieItem item = new MovieItem(favoriteMovies.getInt(MOVIE_ID),
                            favoriteMovies.getString(MOVIE_TITLE),
                            favoriteMovies.getString(MOVIE_POSTER_PATH),
                            favoriteMovies.getString(MOVIE_OVERVIEW),
                            favoriteMovies.getDouble(MOVIE_AVERAGE),
                            favoriteMovies.getString(MOVIE_RELEASE_DATE));
                    movieList.add(item);
                }
            }

            if (favoriteMovies != null && !favoriteMovies.isClosed()) {
                favoriteMovies.close();
            }
            return movieList;
        }
    }
}
