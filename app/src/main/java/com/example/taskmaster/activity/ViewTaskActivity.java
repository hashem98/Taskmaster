package com.example.taskmaster.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.Task;
import com.amplifyframework.datastore.generated.model.Team;
import com.example.taskmaster.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
@RequiresApi(api = Build.VERSION_CODES.N)
public class ViewTaskActivity extends AppCompatActivity {
Context context=this;
    private static final String TAG = "Edit Task";
    private final MediaPlayer mp = new MediaPlayer();

    private Task taskToEdit = null;
    private CompletableFuture<Task> taskCompletableFuture = null;
    private CompletableFuture<List<Team>> teamsFuture = null;
    private EditText taskTitleEditText;

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

        if (callingIntent != null)
        {
            taskId = callingIntent.getStringExtra(HomeActivity.TASK_ID_TAG);
        }

        String taskId2 = taskId;

        Amplify.API.query(
                ModelQuery.list(Task.class),
                success ->
                {
                    Log.i(TAG, "Read task successfully!");

                    for (Task databaseTask : success.getData())
                    {
                        if (databaseTask.getId().equals(taskId2))
                        {
                            taskCompletableFuture.complete(databaseTask);
                        }
                    }
                    Log.i(TAG, "Read task successfully!"+ taskCompletableFuture.toString());

                    runOnUiThread(() ->
                    {
                        // Update UI elements
                    });
                },
                failure -> Log.i(TAG, "Did not read tasks successfully!")
        );

        try
        {
            taskToEdit = taskCompletableFuture.get();
        }
        catch (InterruptedException ie)
        {
            Log.e(TAG, "InterruptedException while getting task");
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException ee)
        {
            Log.e(TAG, "ExecutionException while getting product");
        }

        String imageS3Key = taskToEdit.getTaskImageS3Key();
        if (imageS3Key != null && !imageS3Key.isEmpty()){
            Amplify.Storage.downloadFile(
                    imageS3Key,
                    new File(getApplication().getFilesDir(),imageS3Key),
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


    private void setUpUIElementsOG(){
        Intent callingIntent = getIntent();
        String taskTitleString = null;
        String taskBodyString = null;
        String taskStateEnum = null;

        if (callingIntent != null){
            taskTitleString = callingIntent.getStringExtra(HomeActivity.TASK_TITLE_TAG);
            taskBodyString = callingIntent.getStringExtra(HomeActivity.TASK_BODY_TAG);
            taskStateEnum = callingIntent.getStringExtra(HomeActivity.TASK_STATE_TAG);
        }

        TextView taskViewTitleView = (TextView) findViewById(R.id.textTaskViewTitle);
        TextView taskViewBodyView = (TextView) findViewById(R.id.textTaskViewBody);
        TextView taskViewStateView = (TextView) findViewById(R.id.textTaskViewStatus);


        if (taskTitleString != null){
            taskViewTitleView.setText(taskTitleString);
        } else {
            taskViewTitleView.setText(R.string.no_task_name);
        }

        if (taskBodyString != null){
            taskViewBodyView.setText(taskBodyString);
        } else {
            taskViewBodyView.setText(R.string.no_task_name);
        }

        if (taskStateEnum != null){
            taskViewStateView.setText(taskStateEnum);
        } else {
            taskViewStateView.setText(R.string.no_task_state);
        }
        taskViewBodyView.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                String[] event = {"Translate Text", "Speech"};

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Pick an Action");

                builder.setItems(event, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // the user clicked on colors[which]
                        if (which==0)
                        {
                            String taskDescription = taskToEdit.getDescription();

                            Amplify.Predictions.translateText(taskDescription,
                                    result -> {
                                        Log.i("MyAmplifyApp", result.getTranslatedText());
                                        runOnUiThread(() ->
                                        {
                                            taskViewBodyView.setText(result.getTranslatedText());
                                        });

                                    },
                                    error -> Log.e("MyAmplifyApp", "Translation failed", error)
                            );
                        }
                        else
                        {
                            String taskDescription = taskToEdit.getDescription();

                            Amplify.Predictions.convertTextToSpeech(
                                    taskDescription,
                                    result -> {
                                        playAudio(result.getAudioData());

                                    },
                                    error -> Log.e(TAG, "Conversion failed", error)
                            );

                        }
                    }
                });
                builder.show();


                return true;
            }
        });
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
