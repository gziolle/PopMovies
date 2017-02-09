package com.example.gziolle.popmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.example.gziolle.popmovies.data.FavoritesContract;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity implements MovieListFragment.Callback {

    private static boolean mTwoPane;
    private DrawerLayout mDrawerLayout;
    private NavigationView mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        if (findViewById(R.id.movie_detail_container) != null) {
            mTwoPane = true;

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.movie_detail_container, new DetailFragment()).commit();
            }
        } else {
            mTwoPane = false;
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawer = (NavigationView) findViewById(R.id.navigation_drawer_list);

        setupDrawerContent();

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.add_favorite, R.string.add_favorite);
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        Drawable menuIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_menu, getTheme());
        mDrawerToggle.setHomeAsUpIndicator(menuIcon);
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDrawerLayout.isDrawerVisible(GravityCompat.START)) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String queryMode = prefs.getString(getString(R.string.query_mode_key), "");
        String titleString;

        if (queryMode.equals("")) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(getString(R.string.query_mode_key), getString(R.string.query_mode_favorites));
            editor.commit();
            titleString = getString(R.string.favorites);
        } else {
            if (queryMode.equals(getString(R.string.query_mode_popular))) {
                mDrawer.setCheckedItem(R.id.popular);
                titleString = getString(R.string.popular);
            } else if (queryMode.equals(getString(R.string.query_mode_top_rated))) {
                titleString = getString(R.string.top_rated);
                mDrawer.setCheckedItem(R.id.top_rated);
            } else {
                titleString = getString(R.string.favorites);
                mDrawer.setCheckedItem(R.id.favorites);
            }
        }

        getSupportActionBar().setTitle(titleString);

    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String queryMode = prefs.getString(getString(R.string.query_mode_key), "");

        if (queryMode.equals(getString(R.string.query_mode_popular))) {
            mDrawer.setCheckedItem(R.id.popular);
        } else if (queryMode.equals(getString(R.string.query_mode_top_rated))) {
            mDrawer.setCheckedItem(R.id.top_rated);
        } else {
            mDrawer.setCheckedItem(R.id.favorites);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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

    @Override
    public void onItemSelected(MovieItem item, View view) {
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
            ImageView imageView = (ImageView) view.findViewById(R.id.poster);
            Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this, new Pair<View, String>(imageView, getString(R.string.transition_poster))).toBundle();
            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            byte[] bitmapArray = outputStream.toByteArray();

            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID, item.get_id());
            intent.putExtra(FavoritesContract.FavoritesEntry.COLUMN_TITLE, item.getTitle());
            intent.putExtra(FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH, item.getPosterPath());
            intent.putExtra(FavoritesContract.FavoritesEntry.COLUMN_RELEASE_DATE, item.getReleaseDate());
            intent.putExtra(FavoritesContract.FavoritesEntry.COLUMN_OVERVIEW, item.getOverview());
            intent.putExtra(FavoritesContract.FavoritesEntry.COLUMN_AVERAGE, item.getAverage());
            intent.putExtra("bitmap", bitmapArray);

            startActivity(intent, bundle);
        }
    }

    private void setupDrawerContent() {
        mDrawer.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        selectMenuItem(item);
                        return true;
                    }
                }
        );
    }

    private void selectMenuItem(MenuItem item) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        String queryMode;
        String titleString;

        switch (item.getItemId()) {
            case R.id.popular:
                queryMode = getString(R.string.query_mode_popular);
                titleString = getString(R.string.popular);
                break;
            case R.id.top_rated:
                queryMode = getString(R.string.query_mode_top_rated);
                titleString = getString(R.string.top_rated);
                break;
            case R.id.settings:
                item.setChecked(false);
                mDrawerLayout.closeDrawers();
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, 1);
                return;
            case R.id.about:
                DialogFragment newFragment = new AboutDialogFragment();
                newFragment.show(getSupportFragmentManager(), "about");
                return;
            default:
                queryMode = getString(R.string.query_mode_favorites);
                titleString = getString(R.string.favorites);
                break;
        }

        editor.putString(getString(R.string.query_mode_key), queryMode);
        editor.commit();

        item.setChecked(true);
        mDrawerLayout.closeDrawers();
        getSupportActionBar().setTitle(titleString);

        FragmentManager fm = getSupportFragmentManager();
        MovieListFragment movieListFragment = (MovieListFragment) fm.findFragmentById(R.id.movie_list_fragment);
        movieListFragment.queryMovies();
    }

    private void setDrawerCheckedItem(String queryMode) {
        if (queryMode.equals(getString(R.string.query_mode_popular))) {
            mDrawer.setCheckedItem(R.id.popular);
        } else if (queryMode.equals(getString(R.string.query_mode_top_rated))) {
            mDrawer.setCheckedItem(R.id.top_rated);
        } else {
            mDrawer.setCheckedItem(R.id.favorites);
        }
    }
}
