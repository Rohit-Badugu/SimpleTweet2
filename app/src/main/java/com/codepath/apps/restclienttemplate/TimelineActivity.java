package com.codepath.apps.restclienttemplate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 200 ;
    TwitterClient client;
    public static final String TAG="TimelineActivity";
    RecyclerView rvTweets;
    List<Tweet> tweets;
    TweetsAdapter adapter;
    SwipeRefreshLayout swipeContainer;
    EndlessRecyclerViewScrollListener scrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        client=TwitterApplication.getRestClient(this);

        swipeContainer =findViewById(R.id.swipeContainer);
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(TAG,"onRefresh");
                populateHomeTimeline();
            }
        });
        rvTweets=findViewById(R.id.rvTweets);
        tweets=new ArrayList<>();
        adapter=new TweetsAdapter(this,tweets);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        rvTweets.setLayoutManager(layoutManager);
        rvTweets.setAdapter(adapter);

        scrollListener=new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {

                loadMoreData();
            }
        };
        rvTweets.addOnScrollListener(scrollListener);
        populateHomeTimeline();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.compose){
            //Toast.makeText(this,"Compose",Toast.LENGTH_SHORT).show();
            Intent intent=new Intent(this,ComposeActivity.class);
            startActivityForResult(intent,REQUEST_CODE);
            return  true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==REQUEST_CODE && resultCode==RESULT_OK){

            Tweet tweet=Parcels.unwrap(data.getParcelableExtra("tweet"));
            tweets.add(0,tweet);
            adapter.notifyItemChanged(0);
            rvTweets.smoothScrollToPosition(0);

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void loadMoreData() {

        client.getNextPageofTweets(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {

                Log.i(TAG,"onSuccess for loadMoreData");
                JSONArray jsonArray= json.jsonArray;
                try {
                    List<Tweet>tweets=Tweet.fromJsonArray(jsonArray);
                    adapter.addAll(tweets);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {

            }
        },tweets.get(tweets.size()-1).id);

    }

    private void populateHomeTimeline() {
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG,"onSuccess");

                JSONArray jsonArray=json.jsonArray;
                System.out.println("Length" + jsonArray.length());
                try{
                    adapter.clear();
                    adapter.addAll(Tweet.fromJsonArray(jsonArray));
                    swipeContainer.setRefreshing(false);
                } catch (JSONException e) {
                    Log.e(TAG," Json Exception",e);
                    e.printStackTrace();

                }


            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG,"onFailure",throwable);
            }
        });
    }
}