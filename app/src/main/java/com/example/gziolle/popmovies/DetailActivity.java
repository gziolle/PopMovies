package com.example.gziolle.popmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.gziolle.popmovies.data.FavoritesContract;

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
            bundle.putLong(FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID, intent.getLongExtra(FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID, 0));
            bundle.putString(FavoritesContract.FavoritesEntry.COLUMN_TITLE, intent.getStringExtra(FavoritesContract.FavoritesEntry.COLUMN_TITLE));
            bundle.putString(FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH, intent.getStringExtra(FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH));
            bundle.putString(FavoritesContract.FavoritesEntry.COLUMN_RELEASE_DATE, intent.getStringExtra(FavoritesContract.FavoritesEntry.COLUMN_RELEASE_DATE));
            bundle.putString(FavoritesContract.FavoritesEntry.COLUMN_OVERVIEW, intent.getStringExtra(FavoritesContract.FavoritesEntry.COLUMN_OVERVIEW));
            bundle.putDouble(FavoritesContract.FavoritesEntry.COLUMN_AVERAGE, intent.getDoubleExtra(FavoritesContract.FavoritesEntry.COLUMN_AVERAGE, 0));
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
                startActivityForResult(intent, 1);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
