package com.example.taskmaster.activity;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Geocoder;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.Task;
import com.amplifyframework.datastore.generated.model.Team;
import com.example.taskmaster.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnTokenCanceledListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
@RequiresApi(api = Build.VERSION_CODES.N)
public class ViewTaskActivity extends AppCompatActivity {

    private static final String TAG = "Edit Task";

    private Task taskToEdit = null;
    private CompletableFuture<Task> taskCompletableFuture = null;
    private CompletableFuture<List<Team>> teamsFuture = null;
    private EditText taskTitleEditText;
    FusedLocationProviderClient locationProviderClient = null;
    Geocoder geocoder;
    private final MediaPlayer mp = new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_task);
        taskCompletableFuture = new CompletableFuture<>();
        teamsFuture = new CompletableFuture<>();


        setUpUIElementsOG();
        setUpUIElementsEdit();

    }



    private void setUpUIElementsEdit() {
        Intent callingIntent = getIntent();
        String taskId = null;

        if (callingIntent != null) {
            taskId = callingIntent.getStringExtra(HomeActivity.TASK_ID_TAG);
        }

        String taskId2 = taskId;

        Amplify.API.query(
                ModelQuery.list(Task.class),
                success ->
                {
                    Log.i(TAG, "Read task successfully!");

                    for (Task databaseTask : success.getData()) {
                        if (databaseTask.getId().equals(taskId2)) {
                            taskCompletableFuture.complete(databaseTask);
                        }
                    }

                    runOnUiThread(() ->
                    {
                        // Update UI elements
                    });
                },
                failure -> Log.i(TAG, "Did not read tasks successfully!")
        );


        try {
            taskToEdit = taskCompletableFuture.get();
        } catch (InterruptedException ie) {
            Log.e(TAG, "InterruptedException while getting task");
            Thread.currentThread().interrupt();
        } catch (ExecutionException ee) {
            Log.e(TAG, "ExecutionException while getting product");
        }


        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);  // hardcoded to 1; you can pick anything between 1 and 65535
        // Step 3: Set up a FusedLocationProviderClient
        locationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        //locationProviderClient.flushLocations();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.e(TAG, "Application does not have access to either ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION!");
            return;
        }
        locationProviderClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, new CancellationToken() {
            @NonNull
            @Override
            public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                return null;
            }

            @Override
            public boolean isCancellationRequested() {
                return false;
            }
        }).addOnSuccessListener(location ->
                {
                    if (location == null) {
                        Log.e(TAG, "Location callback was null!");
                    }
                    String currentLatitude = Double.toString(location.getLatitude());
                    String currentLongitude = Double.toString(location.getLongitude());
                    Log.i(TAG, "Our current latitude: " + currentLatitude);
                    Log.i(TAG, "Our current longitude: " + currentLongitude);
                }
        );


        TextView taskViewLatitude = (TextView) findViewById(R.id.viewTaskLatitude);
        TextView taskViewLongitude = (TextView) findViewById(R.id.viewTaskLongitude);

        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        LocationRequest locationRequest = LocationRequest.create();
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                try {
                    if(taskToEdit.getTaskLatitude() != null && taskToEdit.getTaskLongitude() != null) {
                        String address = geocoder.getFromLocation(
                                Double.parseDouble(taskToEdit.getTaskLatitude()),
                                Double.parseDouble(taskToEdit.getTaskLongitude()),
                                1)  // give us only 1 best guess
                                .get(0)  // grab that best guess
                                .getAddressLine(0);  // get the first address line
                        Log.i(TAG, "Current address is: " + address);



                    } } catch (IOException ioe) {
                    Log.e(TAG, "Could not get subscribed location: " + ioe.getMessage(), ioe);
                }
            }
        };
        locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());


        runOnUiThread(() -> {
                    taskViewLatitude.setText(taskToEdit.getTaskLatitude());
                    taskViewLongitude.setText(taskToEdit.getTaskLongitude());
                }
        );


        String imageS3Key = taskToEdit.getTaskImageS3Key();
        if (imageS3Key != null && !imageS3Key.isEmpty()) {
            Amplify.Storage.downloadFile(
                    imageS3Key,
                    new File(getApplication().getFilesDir(), imageS3Key),
                    success -> {
                        ImageView viewTaskImageUpload = findViewById(R.id.imageViewViewTaskUploadFile);
                        viewTaskImageUpload.setImageBitmap(BitmapFactory.decodeFile(success.getFile().getPath()));
                    },
                    failure -> {
                        Log.e(TAG, "Unable to get image from s3 for: " + imageS3Key);
                    }
            );
        }

    }



    private void setUpUIElementsOG() {
        Intent callingIntent = getIntent();
        String taskTitleString = null;
        String taskBodyString = null;
        String taskStateEnum = null;

        if (callingIntent != null) {
            taskTitleString = callingIntent.getStringExtra(HomeActivity.TASK_TITLE_TAG);
            taskBodyString = callingIntent.getStringExtra(HomeActivity.TASK_BODY_TAG);
            taskStateEnum = callingIntent.getStringExtra(HomeActivity.TASK_STATE_TAG);
        }

        TextView taskViewTitleView = (TextView) findViewById(R.id.textTaskViewTitle);
        TextView taskViewBodyView = (TextView) findViewById(R.id.textTaskViewBody);
        TextView taskViewStateView = (TextView) findViewById(R.id.textTaskViewStatus);


        if (taskTitleString != null) {
            taskViewTitleView.setText(taskTitleString);
        } else {
            taskViewTitleView.setText(R.string.no_task_name);
        }

        if (taskBodyString != null) {
            taskViewBodyView.setText(taskBodyString);
        } else {
            taskViewBodyView.setText(R.string.no_task_name);
        }

        if (taskStateEnum != null) {
            taskViewStateView.setText(taskStateEnum);
        } else {
            taskViewStateView.setText(R.string.no_task_state);
        }
    }


    private void playAudio(InputStream data) {
        File mp3File = new File(getCacheDir(), "audio.mp3");

        try (OutputStream out = new FileOutputStream(mp3File)) {
            byte[] buffer = new byte[8 * 1_024];
            int bytesRead;
            while ((bytesRead = data.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            mp.reset();
            mp.setOnPreparedListener(MediaPlayer::start);
            mp.setDataSource(new FileInputStream(mp3File).getFD());
            mp.prepareAsync();
        } catch (IOException error) {
            Log.e("MyAmplifyApp", "Error writing audio file", error);
        }
    }
}
