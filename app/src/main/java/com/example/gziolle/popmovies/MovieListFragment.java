package com.example.gziolle.popmovies;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.example.gziolle.popmovies.utils.FetchMoviesTask;
import com.example.gziolle.popmovies.utils.Utility;
import java.util.ArrayList;


/**
 * Created by gziolle on 10/18/2016.
 */

public class MovieListFragment extends Fragment implements FetchMoviesTask.AsyncResponse {

    public static final String LOG_TAG = MovieListFragment.class.getSimpleName();
    private static boolean mIsFetching = false;
    public GridView mGridView;
    public MovieAdapter mMovieAdapter;
    public ArrayList<MovieItem> mMovieItems;
    private int mCurrentPage = 0;
    private String lastQueryMode = "";

    public static void setIsFetching(boolean isFetching) {
        mIsFetching = isFetching;
    }

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
                        mCurrentPage++;
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
        if (!mIsFetching) {
            updateMovieList();
        }
    }

    public void updateMovieList() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String queryMode = prefs.getString(getString(R.string.query_mode_key), getString(R.string.query_mode_default));

        //Get the favorites and add them to the list. No need for a web query.

        if (queryMode.equals(getActivity().getString(R.string.query_mode_favorites))) {
            mMovieItems.clear();
            mCurrentPage = 1;
            lastQueryMode = getActivity().getString(R.string.query_mode_favorites);
            new FetchMoviesTask(getActivity(), this).execute(queryMode, String.valueOf(mCurrentPage));

        } else {
            if (Utility.isConnected(getActivity())) {
                //Get the preference's value and start the AsyncTask
                if (lastQueryMode.equals("") || !lastQueryMode.equals(queryMode)) {
                    lastQueryMode = queryMode;
                    mCurrentPage = 1;
                }
                new FetchMoviesTask(getActivity(), this).execute(queryMode, String.valueOf(mCurrentPage));
            } else {
                //Since there is no connection, we'll clear the data source and notify that it has changed.
                mMovieItems.clear();
                mMovieAdapter.notifyDataSetChanged();

                TextView mEmptyView = (TextView) getActivity().findViewById(R.id.empty_grid_view);
                mEmptyView.setText(R.string.grid_view_error_no_connection);
            }
        }
    }

    @Override
    public void updateMovieList(ArrayList<MovieItem> movieList) {
        TextView emptyTextView = (TextView) getActivity().findViewById(R.id.empty_grid_view);
        if (movieList != null) {
            mMovieItems.addAll(movieList);
            mMovieAdapter.notifyDataSetChanged();
        } else {
            if (emptyTextView != null) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                int status = prefs.getInt(getActivity().getString(R.string.pref_status), Utility.STATUS_SERVER_DOWN);

                switch (status) {
                    case Utility.STATUS_SERVER_NEEDS_API_KEY:
                        emptyTextView.setText(getString(R.string.grid_view_error_needs_api_key));
                        break;
                    case Utility.STATUS_RESOURCE_NOT_FOUND:
                        emptyTextView.setText(getString(R.string.grid_view_error_needs_api_key));
                        break;
                }
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
}
