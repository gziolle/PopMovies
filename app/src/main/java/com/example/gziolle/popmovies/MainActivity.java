package com.example.gziolle.popmovies;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.gziolle.popmovies.data.FavoritesContract;

public class MainActivity extends AppCompatActivity implements MovieListFragment.Callback {

    private static boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (findViewById(R.id.movie_detail_container) != null) {
            mTwoPane = true;

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.movie_detail_container, new DetailFragment()).commit();
            }
        } else {
            mTwoPane = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(MovieItem item) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putLong(FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID, item.get_id());
            args.putString(FavoritesContract.FavoritesEntry.COLUMN_TITLE, item.getTitle());
            args.putString(FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH, item.getPosterPath());
            args.putString(FavoritesContract.FavoritesEntry.COLUMN_RELEASE_DATE, item.getReleaseDate());
            args.putString(FavoritesContract.FavoritesEntry.COLUMN_OVERVIEW, item.getOverview());
            args.putDouble(FavoritesContract.FavoritesEntry.COLUMN_AVERAGE, item.getAverage());

            DetailFragment df = new DetailFragment();
            df.setArguments(args);

            getSupportFragmentManager().beginTransaction().replace(R.id.movie_detail_container, df, DetailFragment.DETAIL_FRAGMENT_TAG).commit();

        } else {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID, item.get_id());
            intent.putExtra(FavoritesContract.FavoritesEntry.COLUMN_TITLE, item.getTitle());
            intent.putExtra(FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH, item.getPosterPath());
            intent.putExtra(FavoritesContract.FavoritesEntry.COLUMN_RELEASE_DATE, item.getReleaseDate());
            intent.putExtra(FavoritesContract.FavoritesEntry.COLUMN_OVERVIEW, item.getOverview());
            intent.putExtra(FavoritesContract.FavoritesEntry.COLUMN_AVERAGE, item.getAverage());
            startActivity(intent);
        }
    }

}
