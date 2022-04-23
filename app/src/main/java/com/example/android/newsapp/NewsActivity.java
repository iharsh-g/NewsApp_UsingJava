package com.example.android.newsapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.android.newsapp.databinding.ActivityNewsBinding;
import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NewsActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener,
        SwipeRefreshLayout.OnRefreshListener{

    private NewsAdapter mAdapter;
    private ArrayList<News> mNewsList;
    private static final String URL = "https://content.guardianapis.com/search?";  //https://content.guardianapis.com/search?q=science&api-key=test&show-fields=thumbnail&page-size=100&order-by=newest

    //Shared Preferences values
    private String mNewsType;
    private String mOrderBy;
    private String mNewsLimit;

    //Volley
    private RequestQueue mQueue;
    private ActivityNewsBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_news);

        mBinding.shimmer.startShimmer();

        mQueue = Volley.newRequestQueue(this);
        mNewsList = new ArrayList<>();
        mAdapter = new NewsAdapter(this, mNewsList);
        mBinding.list.setAdapter(mAdapter);

        mBinding.list.setEmptyView(mBinding.emptyView);
        mBinding.list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
            jsonParse();
        }
        else{
            mBinding.emptyView.setText(R.string.no_internet);
        }

        mBinding.swipeLayout.setOnRefreshListener(this);
        getSharedPreferences();
    }

    private void jsonParse() {
        String url = URL + "q=" + mNewsType + "&api-key=test&show-fields=thumbnail&page-size=" + mNewsLimit + "&order-by=" + mOrderBy;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                    getSharedPreferences();
                    mBinding.list.setVisibility(View.GONE);
                    mBinding.shimmer.startShimmer();
                    mBinding.shimmer.setVisibility(View.VISIBLE);
                        try {
                            JSONObject  res = response.getJSONObject("response");
                            JSONArray newsArray = res.getJSONArray("results");

                            for (int i = 0; i < newsArray.length(); i++) {

                                JSONObject currentNews = newsArray.getJSONObject(i);

                                String sectionName = currentNews.getString("sectionName");

                                String newsTitle = currentNews.getString("webTitle");
                                String newsPublish = currentNews.getString("webPublicationDate");
                                String newsURL;
                                if(currentNews.has("webUrl")){
                                    newsURL = currentNews.getString("webUrl");
                                }
                                else{
                                    newsURL = null;
                                }

                                String thumbnail;
                                if(currentNews.has("fields")){
                                    JSONObject fields = currentNews.getJSONObject("fields");
                                    thumbnail = fields.getString("thumbnail");
                                }
                                else{
                                    thumbnail = null;
                                }


                                mBinding.swipeLayout.setRefreshing(false);
                                mBinding.shimmer.stopShimmer();
                                mBinding.shimmer.setVisibility(View.GONE);
                                mBinding.list.setVisibility(View.VISIBLE);

                                News news = new News("newsName", sectionName, newsTitle, newsURL, thumbnail, newsPublish);
                                mNewsList.add(news);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            mBinding.emptyView.setVisibility(View.VISIBLE);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                mBinding.emptyView.setVisibility(View.VISIBLE);
            }
        });
        mQueue.add(request);
    }

    private void getSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mNewsType = sharedPreferences.getString(getString(R.string.title_news_type_preference_key), getString(R.string.default_news_type_value));
        mOrderBy = sharedPreferences.getString(getString(R.string.title_order_by_preference_key), getString(R.string.default_order_by_value));
        mNewsLimit = sharedPreferences.getString(getString(R.string.title_news_limit_preference_key), getString(R.string.default_news_limit_value));
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
        getSharedPreferences();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("NewsActivity", "OnResume");
        mNewsList.clear();
        Log.e("NewsActivity", "" + mNewsList.size());
        mAdapter.notifyDataSetChanged();
        jsonParse();
    }

    @Override
    public void onRefresh() {
        Log.d("MAIN", "REFRESHING...");
        mNewsList.clear();
        mAdapter.notifyDataSetChanged();
        jsonParse();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
