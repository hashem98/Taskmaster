package com.example.taskmaster.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.model.temporal.Temporal;
import com.amplifyframework.datastore.generated.model.Task;
import com.amplifyframework.datastore.generated.model.TaskStatusEnum;
import com.amplifyframework.datastore.generated.model.Team;
import com.example.taskmaster.R;
import com.google.android.material.snackbar.Snackbar;


import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
@RequiresApi(api = Build.VERSION_CODES.N)
public class AddTaskActivity extends AppCompatActivity {

    private static final String TAG = "TASK";
    Spinner taskStateSpinner = null;
    Spinner teamSpinner = null;
    String imageToUploadKey = "";

    CompletableFuture<List<Team>> teamsFuture = null;

    ActivityResultLauncher<Intent> activityResultLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        activityResultLauncher = getImagePickingActivityResultLauncher();
        teamsFuture = new CompletableFuture<>();


        Intent callingIntent = getIntent();
        if ((callingIntent != null) && (callingIntent.getType() != null) && (callingIntent.getType().startsWith("image"))) {

            Uri incomingImageFileUri = callingIntent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (incomingImageFileUri != null) {
                InputStream incomingImageFileInputStream = null;
                try {
                    incomingImageFileInputStream = getContentResolver().openInputStream(incomingImageFileUri);

                } catch (FileNotFoundException fnfe) {
                    Log.e(TAG, "Could not get file stream from URI" + fnfe.getMessage(), fnfe);
                }
                String pickedImageFilename = getFileNameFromUri(incomingImageFileUri);
                uploadInputStreamToS3(incomingImageFileInputStream, pickedImageFilename, incomingImageFileUri);
                imageToUploadKey = getFileNameFromUri(incomingImageFileUri);
            }
        }

        setUpSpinners();
        setUpSaveButtons();
        setUpAddImageButton();
    }

    private void setUpSaveButtons() {
        Button addTaskButton = findViewById(R.id.buttonAddTaskTaskActivity);
        addTaskButton.setOnClickListener(v -> {

            String title = ((EditText) findViewById(R.id.editTextTaskTitle)).getText().toString();
            String description = ((EditText) findViewById(R.id.editTextTaskDescription)).getText().toString();
            String currentDateString = com.amazonaws.util.DateUtils.formatISO8601Date(new Date());
            String selectedTeamString = teamSpinner.getSelectedItem().toString();


            List<Team> teams = null;
            try {
                teams = teamsFuture.get();
            } catch (InterruptedException ie) {
                Log.e(TAG, "InterruptedException while getting teams.");
                Thread.currentThread().interrupt();
            } catch (ExecutionException ee) {
                Log.e(TAG, "ExecutionException while getting teams.");
            }

            assert teams != null;
            Team selectedTeam = teams.stream().filter(c -> c.getName().equals(selectedTeamString)).findAny().orElseThrow(RuntimeException::new);

            Task newTask = Task.builder()
                    .title(title)
                    .description(description)
                    .taskStatusEnum((TaskStatusEnum) taskStateSpinner.getSelectedItem())
                    .teamName(selectedTeam)
                    .taskImageS3Key(imageToUploadKey)
                    .build();


            Amplify.API.mutate(
                    ModelMutation.create(newTask),
                    successResponse -> Log.i(TAG, "Made a Task successfully!"),
                    failureResponse -> Log.i(TAG, "failed with this response: " + failureResponse)
            );
            Snackbar.make(findViewById(R.id.addTaskActivity), "Task saved!", Snackbar.LENGTH_SHORT).show();
        });
    }

    private void setUpSpinners() {

        teamSpinner = (Spinner) findViewById(R.id.editAddTaskStateSpinnerTeam);
        taskStateSpinner = (Spinner) findViewById(R.id.editAddTaskStateSpinner);

        Amplify.API.query(
                ModelQuery.list(Team.class),
                success -> {
                    Log.i(TAG, "Read Teams Successfully!");
                    ArrayList<String> teamNames = new ArrayList<>();
                    ArrayList<Team> teams = new ArrayList<>();
                    for (Team team : success.getData()) {
                        teamNames.add(team.getName());
                        teams.add(team);
                    }
                    teamsFuture.complete(teams);

                    runOnUiThread(() -> teamSpinner.setAdapter(new ArrayAdapter<>(
                            this,
                            android.R.layout.preference_category,
                            teamNames
                    )));
                },
                failure -> {
                    teamsFuture.complete(null);
                    Log.i(TAG, "Failed to add team names!");
                }
        );

        taskStateSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.preference_category, TaskStatusEnum.values()));
    }


    private void setUpAddImageButton() {
        Button addImageButton = (Button) findViewById(R.id.buttonAddTaskUploadImage);
        addImageButton.setOnClickListener(b ->
        {
            launchImageSelectionIntent();
        });
    }

    private void launchImageSelectionIntent() {

        Intent imageFilePickingIntent = new Intent(Intent.ACTION_GET_CONTENT);
        imageFilePickingIntent.setType("*/*");  // only allow one kind or category of file; if you don't have this, you get a very cryptic error about "No activity found to handle Intent"
        imageFilePickingIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/jpeg", "image/png"});

        activityResultLauncher.launch(imageFilePickingIntent);
    }

    private ActivityResultLauncher<Intent> getImagePickingActivityResultLauncher() {
        // Part 2: Create an image picking activity result launcher
        ActivityResultLauncher<Intent> imagePickingActivityResultLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        new ActivityResultCallback<ActivityResult>() {
                            @Override
                            public void onActivityResult(ActivityResult result) {
                                if (result.getResultCode() == Activity.RESULT_OK) {
                                    if (result.getData() != null) {
                                        Uri pickedImageFileUri = result.getData().getData();
                                        try {
                                            InputStream pickedImageInputStream = getContentResolver().openInputStream(pickedImageFileUri);
                                            String pickedImageFilename = getFileNameFromUri(pickedImageFileUri);
                                            Log.i(TAG, "Succeeded in getting input stream from file on phone! Filename is: " + pickedImageFilename);
                                            // Part 3: Use our InputStream to upload file to S3
                                            uploadInputStreamToS3(pickedImageInputStream, pickedImageFilename, pickedImageFileUri);

                                        } catch (FileNotFoundException fnfe) {
                                            Log.e(TAG, "Could not get file from file picker! " + fnfe.getMessage(), fnfe);
                                        }
                                    }
                                } else {
                                    Log.e(TAG, "Activity result error in ActivityResultLauncher.onActivityResult");
                                }
                            }
                        }
                );

        return imagePickingActivityResultLauncher;
    }

    private void uploadInputStreamToS3(InputStream pickedImageInputStream, String pickedImageFilename, Uri pickedImageFileUri) {
        Amplify.Storage.uploadInputStream(
                pickedImageFilename,  // S3 key
                pickedImageInputStream,
                success ->
                {
                    imageToUploadKey = success.getKey();
                    ImageView taskImageView = findViewById(R.id.imageViewAddTaskUploadFile);
                    Log.i(TAG, "Succeeded in getting file uploaded to S3! Key is: " + success.getKey());
                    InputStream pickedImageInputStreamCopy = null;
                    try {
                        pickedImageInputStreamCopy = getContentResolver().openInputStream(pickedImageFileUri);
                    } catch (FileNotFoundException fnfe) {
                        Log.e(TAG, "Could not get file from URI!" + fnfe.getMessage(), fnfe);
                    }
                    taskImageView.setImageBitmap(BitmapFactory.decodeStream(pickedImageInputStreamCopy));
                    updateImageButtons();
                },
                failure ->
                {
                    Log.e(TAG, "Failure in uploading file to S3 with filename: " + pickedImageFilename + " with error: " + failure.getMessage());
                }
        );
    }



    private void deleteImageFromS3() {
        if (!imageToUploadKey.isEmpty())
        {
            Amplify.Storage.remove(
                    imageToUploadKey,
                    success ->
                    {


                        imageToUploadKey = "";
                        ImageView taskImageView = findViewById(R.id.imageViewAddTaskUploadFile);


                        runOnUiThread(()->
                                {
                                    taskImageView.setImageResource(android.R.color.transparent);
                                }
                        );

                        updateImageButtons();
                        Log.i(TAG, "Succeeded in deleting file on S3! Key is: " + success.getKey());
                    },
                    failure ->
                    {
                        Log.e(TAG, "Failure in deleting file on S3 with key: " + imageToUploadKey + " with error: " + failure.getMessage());
                    }
            );
        }
    }


    private void updateImageButtons()
    {
        Button addImageButton = (Button)findViewById(R.id.buttonAddTaskUploadImage);
        if (imageToUploadKey.isEmpty())
        {
            runOnUiThread(() ->
                    {
                        addImageButton.setVisibility(View.VISIBLE);
                    }
            );
        }
        else
        {
            runOnUiThread(() ->
                    {
                        addImageButton.setVisibility(View.INVISIBLE);
                    }
            );
        }
    }

    @SuppressLint("Range")
    public String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

}