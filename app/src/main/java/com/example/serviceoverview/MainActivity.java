package com.example.serviceoverview;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton btnSelectMusicFile;

    public static final String ON_FAILURE = "Failed to Fetch Resources";
    public static final String ON_NO_DATA = "No Audio File Available on Device";
    public static final String EXTRA_URI_PATH = "UriPath";

    ActivityResultLauncher<String[]> audioFilesLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(), new ActivityResultCallback<Uri>() {
        @Override
        public void onActivityResult(Uri result) {
            Intent intent = new Intent(MainActivity.this,AudioPlayActivity.class);
            intent.putExtra(EXTRA_URI_PATH,result.toString());
            startActivity(intent);
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSelectMusicFile = findViewById(R.id.btnSelectMusicFile);

        btnSelectMusicFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioFilesLauncher.launch(new String[]{"audio/*"});
            }
        });

    }
}