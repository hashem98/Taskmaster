package com.example.taskmaster.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.auth.AuthUser;
import com.amplifyframework.auth.AuthUserAttribute;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.Task;
import com.example.taskmaster.R;
import com.example.taskmaster.adapter.TaskListRecycleReviewAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
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
}