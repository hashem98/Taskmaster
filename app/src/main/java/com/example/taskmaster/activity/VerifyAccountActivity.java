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
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.AWSDataStorePlugin;
import com.example.taskmaster.R;

public class VerifyAccountActivity extends AppCompatActivity {

    public static final String TAG = "VerifyAccountActivity";
    public static final String VERIFY_ACCOUNT_EMAIL_TAG = "Verify_Account_Email_Tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_account);


        Intent callingIntent = getIntent();
        String email = callingIntent.getStringExtra(SignUpActivity.SIGNUP_EMAIL_TAG);
        EditText usernameEditText = findViewById(R.id.editVerifyUserNameSignIn);
        usernameEditText.setText(email);

        LinearLayout verifyAccountVerifyButton = findViewById(R.id.buttonVerifyUserAccount);
        verifyAccountVerifyButton.setOnClickListener(v ->
        {
            String username = usernameEditText.getText().toString();
            String verificationCode = ((EditText) findViewById(R.id.editVerifyPassWordSignIn)).getText().toString();

            Amplify.Auth.confirmSignUp(username,
                    verificationCode,
                    good ->
                    {
                        Log.i(TAG, "Verification succeeded: " + good.toString());
                        Intent goToLogInIntent = new Intent(VerifyAccountActivity.this, LoginActivity.class);
                        goToLogInIntent.putExtra(VERIFY_ACCOUNT_EMAIL_TAG, username);
                        startActivity(goToLogInIntent);
                    },
                    bad ->
                    {
                        Log.i(TAG, "Verification failed with username: " + "hashemsmadi98@gmail.com" + " with this message: " + bad.toString());
                        runOnUiThread(() ->
                                {
                                    Toast.makeText(VerifyAccountActivity.this, "Verify account failed!", Toast.LENGTH_SHORT);
                                }
                        );
                    }
            );
        });

    }
}
