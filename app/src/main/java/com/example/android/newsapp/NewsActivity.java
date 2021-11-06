package com.example.android.newsapp;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.List;

public class NewsActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<List<News>>,
        SharedPreferences.OnSharedPreferenceChangeListener,
        SwipeRefreshLayout.OnRefreshListener{

    private NewsAdapter mAdapter;
    private TextView mEmptyTextView;
    private static final String URL = "https://content.guardianapis.com/search?";  //https://content.guardianapis.com/search?q=science&api-key=test&show-fields=thumbnail&page-size=100&order-by=newest
    private static final int EARTHQUAKE_LOADER_ID = 1;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar mProgressBar;
    private ShimmerFrameLayout mShimmerFrameLayout;
    ListView newsListView;

    String mNewsType;
    String mOrderBy;
    String mNewsLimit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        newsListView = (ListView) findViewById(R.id.list);
        mShimmerFrameLayout = (ShimmerFrameLayout) findViewById(R.id.shimmer);
        mShimmerFrameLayout.startShimmer();


        //mProgressBar = (ProgressBar) findViewById(R.id.loadingIndicator);
        mAdapter = new NewsAdapter(this, new ArrayList<News>());
        newsListView.setAdapter(mAdapter);

        mEmptyTextView = findViewById(R.id.emptyView);
        newsListView.setEmptyView(mEmptyTextView);

        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                News currentNews = mAdapter.getItem(position);
                Uri newsUri = Uri.parse(currentNews.getUrl());
                Intent webIntent = new Intent(Intent.ACTION_VIEW, newsUri);
                startActivity(webIntent);
            }
        });

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo= connectivityManager.getActiveNetworkInfo();

        if(networkInfo != null && networkInfo.isConnected()){
            LoaderManager loaderManager = getLoaderManager();
            if(loaderManager == null) {
                loaderManager.initLoader(EARTHQUAKE_LOADER_ID, null, this);
            } else {
                loaderManager.restartLoader(EARTHQUAKE_LOADER_ID, null, this);
            }
        }
        else{
            //mProgressBar.setVisibility(View.GONE);
            mEmptyTextView.setText(R.string.no_internet);
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        setUpSharedPreferences();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    private void setUpSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mNewsType = sharedPreferences.getString(getString(R.string.title_news_type_preference_key), getString(R.string.default_news_type_value));
        mOrderBy = sharedPreferences.getString(getString(R.string.title_order_by_preference_key), getString(R.string.default_order_by_value));
        mNewsLimit = sharedPreferences.getString(getString(R.string.title_news_limit_preference_key), getString(R.string.default_news_limit_value));
    }

    @Override
    public Loader<List<News>> onCreateLoader(int id, Bundle args) {
        setUpSharedPreferences();
        //mProgressBar.setVisibility(View.VISIBLE);
        newsListView.setVisibility(View.GONE);
        mShimmerFrameLayout.setVisibility(View.VISIBLE);

        Uri baseUri = Uri.parse(URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter("q", mNewsType);
        uriBuilder.appendQueryParameter("api-key", "test");
        uriBuilder.appendQueryParameter("show-fields", "thumbnail");
        uriBuilder.appendQueryParameter("page-size", mNewsLimit);
        uriBuilder.appendQueryParameter("order-by", mOrderBy);

        return new NewsLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(@NonNull  Loader<List<News>> loader, List<News> news) {
        mSwipeRefreshLayout.setRefreshing(false);
        //mProgressBar.setVisibility(View.GONE);
        mShimmerFrameLayout.stopShimmer();
        mShimmerFrameLayout.setVisibility(View.GONE);
        newsListView.setVisibility(View.VISIBLE);


        mAdapter.clear();
        if(news != null && !news.isEmpty()){
            mAdapter.addAll(news);
        } else {
            mEmptyTextView.setText(R.string.no_news);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<News>> loader) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        setUpSharedPreferences();
    }

    @Override
    protected void onResume() {
        super.onResume();
        onRefresh();
    }

    @Override
    public void onRefresh() {
        Log.d("MAIN", "REFRESHING...");
        LoaderManager loaderManager = getLoaderManager();
        loaderManager.restartLoader(EARTHQUAKE_LOADER_ID, null, this);
    }
}
