package com.example.taskmaster.activity;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.location.Geocoder;
import android.location.LocationRequest;
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
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.model.temporal.Temporal;
import com.amplifyframework.datastore.generated.model.Task;
import com.amplifyframework.datastore.generated.model.TaskStatusEnum;
import com.amplifyframework.datastore.generated.model.Team;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.material.snackbar.Snackbar;
import com.example.taskmaster.R;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RequiresApi(api = Build.VERSION_CODES.M)
public class AddTaskActivity extends AppCompatActivity {

    private static final String TAG = "TASK";
    Spinner taskStateSpinner = null;
    Spinner teamSpinner = null;
    String imageToUploadKey = "";
    CompletableFuture<List<Team>> teamsFuture = null;
    ActivityResultLauncher<Intent> activityResultLauncher;
    FusedLocationProviderClient locationProviderClient = null;
    Geocoder geocoder = null;



    @RequiresApi(api = Build.VERSION_CODES.N)
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

        setUpDeleteImageButton();
        setUpSpinners();
        setUpSaveButtons();
        setUpAddImageButton();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setUpSaveButtons() {
        LinearLayout addTaskButton = findViewById(R.id.buttonAddTaskTaskActivity);
        addTaskButton.setOnClickListener(v -> {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Log.i(TAG, "Application does not have access to FINE LOCATION or COARSE LOCATION");
                return;
            }
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);


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

            locationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
            locationProviderClient.getLastLocation().addOnSuccessListener(location ->
                    {
                        if (location == null) {
                            Log.e(TAG, "Locations is not found!");
                        }
                        String latitude = Double.toString(location.getLatitude());
                        String longitude = Double.toString(location.getLongitude());
                        Log.i(TAG, "Out Latitude: " + location.getLatitude());
                        Log.i(TAG, "Out Latitude: " + location.getLongitude());
                        saveTask(title, description, latitude, longitude, selectedTeam);
                    }
            ).addOnCanceledListener(() -> {
                Log.e(TAG, "Location request was canceled!");
            }).addOnFailureListener(failure ->
            {
                Log.e(TAG, "Location request failed! Error was: " + failure.getMessage(), failure.getCause());
            })
                    .addOnCompleteListener(complete ->
                    {
                        Log.i(TAG, "Location request completed!");
                    });

        });

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void saveTask(String title, String description, String latitude, String longitude, Team selectedTeam) {

        Task newTask = Task.builder()
                .title(title)
                .description(description)
                .taskStatusEnum((TaskStatusEnum) taskStateSpinner.getSelectedItem())
                .teamName(selectedTeam)
                .taskImageS3Key(imageToUploadKey)
                .taskLongitude(longitude)
                .taskLatitude(latitude)
                .build();

        Amplify.API.mutate(
                ModelMutation.create(newTask),
                successResponse -> Log.i(TAG, "Made a Task successfully!"),
                failureResponse -> Log.i(TAG, "failed with this response: " + failureResponse)
        );
        Snackbar.make(findViewById(R.id.addTaskActivity), "Task saved!", Snackbar.LENGTH_SHORT).show();
        startActivity(new Intent(this,HomeActivity.class));

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
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

    private void setUpDeleteImageButton() {
        Button deleteImageButton = (Button) findViewById(R.id.buttonAddTaskRemoveUploadImage);
        deleteImageButton.setOnClickListener(v ->
        {
            deleteImageFromS3();
        });
    }

    private void deleteImageFromS3() {
        if (!imageToUploadKey.isEmpty()) {
            Amplify.Storage.remove(
                    imageToUploadKey,
                    success ->
                    {


                        imageToUploadKey = "";
                        ImageView taskImageView = findViewById(R.id.imageViewAddTaskUploadFile);


                        runOnUiThread(() ->
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


    private void updateImageButtons() {
        Button addImageButton = (Button) findViewById(R.id.buttonAddTaskUploadImage);
        Button deleteImageButton = (Button) findViewById(R.id.buttonAddTaskRemoveUploadImage);
        if (imageToUploadKey.isEmpty()) {
            runOnUiThread(() ->
                    {
                        deleteImageButton.setVisibility(View.INVISIBLE);
                        addImageButton.setVisibility(View.VISIBLE);
                    }
            );
        } else {
            runOnUiThread(() ->
                    {
                        deleteImageButton.setVisibility(View.VISIBLE);
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
