package life.haq.it.android.instalikefeed;

/**
 * Created by Abdul Haq (it.haq.life) on 19-12-2018.
 */

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Cache.Entry;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import life.haq.it.android.instalikefeed.R;
import life.haq.it.android.instalikefeed.adapter.FeedListAdapter;
import life.haq.it.android.instalikefeed.data.FeedItem;
import life.haq.it.android.instalikefeed.volley.AppController;

public class Feed extends Activity implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = Feed.class.getSimpleName();
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listView;
    private FeedListAdapter listAdapter;
    private List<FeedItem> feedItems;
    //it will tell us weather to load more items or not
    boolean loadingMore = false;
    private ActionBar actionBar;

    private String URL_FEED ;
    private String nextUrl;

    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        //setBackButton();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);

        listView = (ListView) findViewById(android.R.id.list);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.SwipeToRefreshColor);

        feedItems = new ArrayList<FeedItem>();

        listAdapter = new FeedListAdapter(this, feedItems);
        listView.setAdapter(listAdapter);

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        swipeRefreshLayout.setRefreshing(true);

                                        fetchFeed();
                                    }
                                }
        );
    }

    public void nameClick(View v) {
    };

    @Override
    public void onRefresh() {
        fetchFeed();
    }

    private void fetchFeed() {
        URL_FEED = "http://it.haq.life/res/other/InstaLikeFeed.json";

        // showing refresh animation before making http call
        swipeRefreshLayout.setRefreshing(true);

        // We first check for cached request
        Cache cache = AppController.getInstance().getRequestQueue().getCache();
        Entry entry = cache.get(URL_FEED);
        if (entry != null) {
            // fetch the data from cache
            try {
                String data = new String(entry.data, "UTF-8");
                try {
                    parseJsonFeed(new JSONObject(data));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        // making fresh volley request and getting json
        JsonObjectRequest jsonReq = new JsonObjectRequest(Method.GET,
                URL_FEED, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                VolleyLog.d(TAG, "Response: " + response.toString());
                if (response != null) {
                    feedItems.clear();
                    parseJsonFeed(response);
                }
                // stopping swipe refresh
                swipeRefreshLayout.setRefreshing(false);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                // stopping swipe refresh
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getApplicationContext(), "Could not connect", Toast.LENGTH_SHORT).show();
            }
        });

        // Adding request to volley request queue
        AppController.getInstance().addToRequestQueue(jsonReq);

        //setting  listener on scroll event of the list
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                //what is the bottom item that is visible
                int lastInScreen = firstVisibleItem + visibleItemCount;

                //is the bottom item visible & not loading more already? Load more!
                if ((lastInScreen == totalItemCount) && !(loadingMore)) {

                    //perform action
                    //Set flag so we cant load new items 2 at the same time
                    loadingMore = true;
                    Log.d("LoadMore Command", "Load More please!");
                    //Toast.makeText(getApplicationContext(), "Load more please!", Toast.LENGTH_SHORT).show();
                    LoadMoreFeed();
                }
            }
        });
    }

    /* Parsing json reponse and passing the data to feed view list adapter*/
    private void parseJsonFeed(JSONObject response) {
        try {
            if (response.has("error")) {
                listView.setEmptyView(findViewById(android.R.id.empty));

            } else {
            JSONObject feedArray1 = response.getJSONObject("feed");
            JSONArray feedArray = feedArray1.getJSONArray("data");

            for (int i = 0; i < feedArray.length(); i++) {
                JSONObject feedObj = (JSONObject) feedArray.get(i);

                FeedItem item = new FeedItem();
                if (feedObj.has("type")) {
                    item.setStatus(feedObj.getString("type"));

                    item.setId(feedObj.getInt("place_id"));
                    item.setName(feedObj.getString("name"));
                    item.setImge(feedObj.getString("photo_url"));
                    item.setTimeStamp(feedObj.getString("time"));
                    item.setNewSlides(feedObj.getString("new_slides"));

                } else {
                    item.setId(feedObj.getInt("id"));
                    item.setName(feedObj.getString("name"));

                    // Image might be null sometimes
                    String image = feedObj.isNull("photo_url") ? null : feedObj
                            .getString("photo_url");

                    item.setImge(image);
                    item.setProfilePic(feedObj.getString("avatar"));
                    item.setTimeStamp(feedObj.getString("time"));
                    item.setStatus("h");
                }

                feedItems.add(item);
            }

                /*if (feedArray1.getJSONObject("paging") != null) {
                    JSONObject paging = feedArray1.getJSONObject("paging");
                    nextUrl = paging.optString("next");
                }*/

            // notify data changes to list
            listAdapter.notifyDataSetChanged();
            //listAdapter.invalidate();
            loadingMore = false;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //load more request code below
    private void LoadMoreFeed() {

        // showing refresh animation before making http call
       //swipeRefreshLayout.setRefreshing(true);

        if (nextUrl != null) {
        // making fresh volley request and getting json
        JsonObjectRequest jsonReq = new JsonObjectRequest(Method.GET,
                nextUrl, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                VolleyLog.d(TAG, "Response: " + response.toString());
                if (response != null) {
                    parseJsonFeed(response);
                }
                // stopping swipe refresh
                //swipeRefreshLayout.setRefreshing(false);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                // stopping swipe refresh
                //swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getApplicationContext(), "Could not connect", Toast.LENGTH_LONG).show();
            }
        });

        // Adding request to volley request queue
        AppController.getInstance().addToRequestQueue(jsonReq);

    } else {
            Log.d("LoadMore Cmd Feed", "Next url is null.");
        }
    }

}
