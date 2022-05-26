package com.example.taskmaster.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.Team;
import com.example.taskmaster.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class UserSettingsActivity extends AppCompatActivity {
    private static final String TAG = "";
    public static final String USER_TEAM_NAME_TAG = "";
    SharedPreferences preferences;
    public static final String USER_USERNAME_TAG = "userUsername";
    Spinner teamSpinner = null;
Toast toast;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        setUpTeamSpinner();


        String userTeamName = preferences.getString(USER_TEAM_NAME_TAG,"");
        String userUsername = preferences.getString(USER_USERNAME_TAG,"");
        if (!userUsername.isEmpty()) {
            EditText userUsernameEditText = (EditText) findViewById(R.id.editTextUserSettingsUsername);
            userUsernameEditText.setText(userUsername);
        }


        Button saveButton = findViewById(R.id.buttonUserSettingsSaveUsername);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor preferencesEditor = preferences.edit();
                EditText userUsernameEditText = (EditText) findViewById(R.id.editTextUserSettingsUsername);
                String userUsernameString = userUsernameEditText.getText().toString();
                String userTeamNameString = teamSpinner.getSelectedItem().toString();
                preferencesEditor.putString(USER_USERNAME_TAG, userUsernameString);
                preferencesEditor.putString(USER_TEAM_NAME_TAG, userTeamNameString);
                preferencesEditor.apply();
                Snackbar.make(findViewById(R.id.userSettingsActivity), "User Settings Saved!", Snackbar.LENGTH_SHORT).show();
            }

        });


    }

    private void setUpTeamSpinner() {

        teamSpinner = (Spinner) findViewById(R.id.spinnerUserSettingsTeam);

        Amplify.API.query(
                ModelQuery.list(Team.class),
                success -> {

                    ArrayList<String> teamNames = new ArrayList<>();
                    ArrayList<Team> teams = new ArrayList<>();
                    for(Team team : success.getData()){
                        teamNames.add(team.getName());
                        teams.add(team);
                    }
                    runOnUiThread(() -> {
                        teamSpinner.setAdapter(new ArrayAdapter<>(
                                this,
                                android.R.layout.preference_category,
                                teamNames
                        ));
                    });
                },
                failure -> {
                    toast = Toast.makeText(getApplicationContext(), "failed with this response: ", Toast.LENGTH_LONG);
                }
        );

    }
}