package com.example.gallery;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private int columns = 2;
    private RecyclerView recView;
    private GridLayoutManager gridLayoutManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Context context = getApplicationContext();

        recView = findViewById(R.id.recView);
        FloatingActionButton changeLayoutBtn = findViewById(R.id.btnChangeLayout);

        gridLayoutManager = new GridLayoutManager(this, columns);
        recView.setLayoutManager(gridLayoutManager);

        ArrayList<ImageModel> images = new ArrayList<>();

        String[] listImages;
        try {
            listImages = context.getAssets().list("img");
        } catch (IOException e) {
            listImages = new String[0];
        }

        for (String image : listImages) {
            String assetPath = "file:///android_asset/img/" + image;
            long imageSize = 0L;
            try {
                AssetFileDescriptor afd = context.getAssets().openFd("img/"+image);
                imageSize = afd.getLength();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            images.add(new ImageModel(imageSize , System.currentTimeMillis(), Uri.parse(assetPath)));

        }
        ImageAdapter imageAdapter = new ImageAdapter(this, images);
        recView.setAdapter(imageAdapter);

        changeLayoutBtn.setOnClickListener(v -> {
            columns = (columns == 2) ? 1 : 2;
            gridLayoutManager.setSpanCount(columns);
        });
    }
}