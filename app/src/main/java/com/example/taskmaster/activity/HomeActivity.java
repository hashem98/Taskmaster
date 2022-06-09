package com.example.taskmaster.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amplifyframework.analytics.AnalyticsEvent;
import com.amplifyframework.analytics.UserProfile;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.auth.AuthUser;
import com.amplifyframework.auth.AuthUserAttribute;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.Task;
import com.example.taskmaster.R;
import com.example.taskmaster.adapter.TaskListRecycleReviewAdapter;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
@RequiresApi(api = Build.VERSION_CODES.N)
public class HomeActivity extends AppCompatActivity {
    private InterstitialAd mInterstitialAd = null;
    private RewardedAd mRewardedAd = null;

    public static final String TASK_TITLE_TAG = "TASK";
    public static final String TASK_BODY_TAG = "BODY";
    public static final String TASK_STATE_TAG = "STATE";
    public final String TAG = "MESSAGE";
    public static final String TASK_ID_TAG = "Task ID Tag";

    SharedPreferences preferences;
    TaskListRecycleReviewAdapter adapter;
    List<Task> tasks = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        tasks = new ArrayList<>();
        UserProfile.Location location = UserProfile.Location.builder()     .build();
        Log.i(TAG, "onCreate: GetGeoLocationRequest "+location.getLatitude());
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull com.google.android.gms.ads.initialization.InitializationStatus initializationStatus) {

            }  });
        advertisement();
        init();
        setUpAddTaskButton();
        setUpUserSettingsButton();
        setUpTaskListRecycleView();



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.logout:
                logout();
                break;
            case R.id.reset:
                // TODO: 5/25/22 Implement reset password
                break;
            default:
        }
        return true;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override

    protected void onResume(){
        super.onResume();

        String userTeamName = preferences.getString(UserSettingsActivity.USER_TEAM_NAME_TAG, "No Team");
        ((TextView) findViewById(R.id.textHomeTeamNameView)).setText(getString(R.string.team_name_with_input, userTeamName));


        AuthUser authUser = Amplify.Auth.getCurrentUser();
        String username = "";
        if (authUser == null)
        {
            String userUsername = preferences.getString(UserSettingsActivity.USER_USERNAME_TAG, "No Username");
            ((TextView) findViewById(R.id.textHomeUsernameView)).setVisibility(View.INVISIBLE);

        }
        else  // authUser is not null
        {
            Log.i(TAG, "Username is: " + username);


            // Not strictly required for your lab, but useful for your project
            Amplify.Auth.fetchUserAttributes(
                    success ->
                    {
                        Log.i(TAG, "Fetch user attributes succeeded for username: " + username);

                        for (AuthUserAttribute userAttribute : success)
                        {
                            if (userAttribute.getKey().getKeyString().equals("nickname"))
                            {
                                String userNickname = userAttribute.getValue();
                                runOnUiThread(() ->
                                        {
                                            ((TextView) findViewById(R.id.textHomeUsernameView)).setText(userNickname);
                                            ((TextView) findViewById(R.id.textHomeUsernameView)).setVisibility(View.VISIBLE);
                                        }
                                );
                            }
                        }
                    },
                    failure ->
                    {
                        Log.i(TAG, "Fetch user attributes failed: " + failure.toString());
                    }
            );
        }

        Amplify.API.query(
                ModelQuery.list(Task.class),
                success -> {
                    Log.i(TAG, "Updated Tasks Successfully!");
                    tasks.clear();
                    for(Task databaseTask : success.getData()){
                        if (userTeamName.equals("No Team")){
                            tasks.add(databaseTask);
                        }
                        else if (databaseTask.getTeamName().getName().equals(userTeamName)) {
                            tasks.add(databaseTask);
                        }
                    }
                    runOnUiThread(() -> {
                        adapter.notifyDataSetChanged();
                    });
                },

                failure -> Log.i(TAG, "failed with this response: ")
        );
    }

    private void setUpUserSettingsButton(){
        ImageView userSettingsImageView = (ImageView) findViewById(R.id.userSettingsImage);

        userSettingsImageView.setOnClickListener(view -> {
            Intent goToUserSettingsIntent = new Intent(HomeActivity.this, UserSettingsActivity.class);
            startActivity(goToUserSettingsIntent);
        });
    }








    private void setUpTaskListRecycleView() {

        RecyclerView taskListRecycleReview = (RecyclerView) findViewById(R.id.homeTaskRecycleView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        taskListRecycleReview.setLayoutManager(layoutManager);

        adapter = new TaskListRecycleReviewAdapter(tasks, this);
        taskListRecycleReview.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void setUpAddTaskButton(){
        FloatingActionButton buttonAddTask = findViewById(R.id.buttonAddTask);
        buttonAddTask.setOnClickListener(view -> {
            Intent goToAddTaskActivity = new Intent(HomeActivity.this, AddTaskActivity.class);
            startActivity(goToAddTaskActivity);


        });
    }

    private void logout() {
        Amplify.Auth.signOut(
                () -> {
                    Log.i(TAG, "Signed out successfully");
                    startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                    finish();
                },
                error -> Log.e(TAG, error.toString())
        );
    }
    private void init(){
        AnalyticsEvent event = AnalyticsEvent.builder()
                .name("openedApp")
                .addProperty("timeOpened", Long.toString(new Date().getTime()))
                .addProperty("eventDescription", "Opened HomeActivity")
                .build();

        Amplify.Analytics.recordEvent(event);
    }
    private  void advertisement(){
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        AdRequest adRequestInterstitial = new AdRequest.Builder().build();

        InterstitialAd.load(this,"ca-app-pub-3940256099942544/1033173712", adRequestInterstitial,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.i(TAG, "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i(TAG, loadAdError.getMessage());
                        mInterstitialAd = null;
                    }
                });


        AdRequest rewardedAdRequest = new AdRequest.Builder().build();

        RewardedAd.load(this, "ca-app-pub-3940256099942544/5224354917",
                rewardedAdRequest, new RewardedAdLoadCallback()
                {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError)
                    {
                        // Handle the error.
                        Log.d(TAG, loadAdError.getMessage());
                        mRewardedAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd)
                    {
                        mRewardedAd = rewardedAd;
                        Log.d(TAG, "Ad was loaded.");

                        mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when ad is shown.
                                Log.d(TAG, "Ad was shown.");
                                runOnUiThread(() -> Toast.makeText(HomeActivity.this, "Ad was shown!", Toast.LENGTH_SHORT).show());
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when ad fails to show.
                                Log.d(TAG, "Ad failed to show.");
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when ad is dismissed.
                                // Set the ad reference to null so you don't show the ad a second time.
                                Log.d(TAG, "Ad was dismissed.");
                                mRewardedAd = null;
                            }
                        });
                    }
                });
        Button interstitialAdButton = (Button)findViewById(R.id.buttonInterstitialAdd1);
        interstitialAdButton.setOnClickListener(b ->
        {
            if (mInterstitialAd != null)
            {
                mInterstitialAd.show(HomeActivity.this);
            }
            else
            {
                Log.d(TAG, "The interstitial ad wasn't ready yet, but we tried to show it.");
            }
        });


        Button rewardedAdButton = (Button)findViewById(R.id.buttonInterstitialAdd2);
        rewardedAdButton.setOnClickListener(b -> {
            if (mRewardedAd != null) {
                mRewardedAd.show(HomeActivity.this, new OnUserEarnedRewardListener() {
                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                        // Handle the reward.
                        int rewardAmount = rewardItem.getAmount();
                        String rewardType = rewardItem.getType();
                        Log.d(TAG, "The user earned the reward. Amount is: " + rewardAmount + ", and type is: " + rewardType);
                    }
                });
            } else {
                Log.d(TAG, "The rewarded ad wasn't ready yet.");
            }
        });

    }

}