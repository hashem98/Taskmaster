package com.example.taskmaster.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.api.aws.AWSApiPlugin;
//import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.AWSDataStorePlugin;
import com.example.taskmaster.R;

public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
//        try {
//            Amplify.addPlugin(new AWSDataStorePlugin());
//            Amplify.addPlugin(new AWSApiPlugin());
//            Amplify.addPlugin(new AWSCognitoAuthPlugin());
//            Amplify.configure(getApplicationContext());
//
//            Log.i("Tutorial", "Initialized Amplify");
//        } catch (AmplifyException e) {
//            Log.e("Tutorial", "Could not initialize Amplify", e);
//        }

        Intent callingIntent = getIntent();
        String email = callingIntent.getStringExtra(VerifyAccountActivity.VERIFY_ACCOUNT_EMAIL_TAG);
        EditText usernameEditText = (EditText) findViewById(R.id.editUserNameSignIn);
        usernameEditText.setText(email);

        LinearLayout loginButton = findViewById(R.id.buttonLoginUserFromLogin);
        loginButton.setOnClickListener(v ->
        {
            String username = usernameEditText.getText().toString();
            String password = ((EditText) findViewById(R.id.editPassWordSignIn)).getText().toString();

            Amplify.Auth.signIn(username,
                    password,
                    success ->
                    {
                        Log.i(TAG, "Login succeeded: " + success.toString());
                        Intent goToProductListIntent = new Intent(LoginActivity.this, HomeActivity.class);
                        startActivity(goToProductListIntent);
                        finish();
                    },
                    failure ->
                    {
                        Log.i(TAG, "Login failed: " + failure.toString());
                        runOnUiThread(() ->
                                {
                                    Toast.makeText(LoginActivity.this, "Login failed!", Toast.LENGTH_SHORT);
                                }
                        );
                    }
            );
        });


        LinearLayout signUpButton = findViewById(R.id.buttonToSignUpTaskActivity);
        signUpButton.setOnClickListener(v ->
        {
            Intent goToSignUpIntent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(goToSignUpIntent);
        });

    }
}