package com.example.gziolle.popmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by gziolle on 10/18/2016.
 */

public class DetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();

        Bundle bundle = new Bundle();

        if (intent != null) {
            bundle.putLong(MovieListFragment.TMDB_ID, intent.getLongExtra(MovieListFragment.TMDB_ID, 0));
            bundle.putString(MovieListFragment.TMDB_TITLE, intent.getStringExtra(MovieListFragment.TMDB_TITLE));
            bundle.putString(MovieListFragment.TMDB_POSTER_PATH, intent.getStringExtra(MovieListFragment.TMDB_POSTER_PATH));
            bundle.putString(MovieListFragment.TMDB_RELEASE_DATE, intent.getStringExtra(MovieListFragment.TMDB_RELEASE_DATE));
            bundle.putString(MovieListFragment.TMDB_OVERVIEW, intent.getStringExtra(MovieListFragment.TMDB_OVERVIEW));
            bundle.putDouble(MovieListFragment.TMDB_VOTE_AVERAGE, intent.getDoubleExtra(MovieListFragment.TMDB_VOTE_AVERAGE, 0));
        }

        if (savedInstanceState == null) {
            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
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
}
