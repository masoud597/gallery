package com.example.gallery;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "GPref";
    public static final String KEY_DID_RUN = "DidRun";
    private Context context;
    private RecyclerView recView;
    private GridLayoutManager gridLayoutManager;
    private CustomFAB bottomPanel;
    private SharedPreferences sharedpref;
    ImageAdapter imageAdapter;

    private  ArrayList<ImageModel> images = new ArrayList<>();

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

        context = getApplicationContext();
        recView = findViewById(R.id.recView);
        bottomPanel = findViewById(R.id.bottomPanel);
        gridLayoutManager = new GridLayoutManager(this, 2);
        recView.setLayoutManager(gridLayoutManager);
        String[] image = getAssetsList();
        for (String img : image) {
            Log.d("tag", img);
        }


        imageAdapter = new ImageAdapter(this, images);
        recView.setAdapter(imageAdapter);

        loadImages();
        bottomPanel.setOnSelectedColumnChangeListener(c -> {
            gridLayoutManager.setSpanCount(c);
        });
    }

    private String[] getAssetsList() {
        String[] listImages;
        try {
            listImages = context.getAssets().list("img");
        } catch (IOException e) {
            listImages = new String[0];
        }
        return listImages;
    }

    private void loadImages() {
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        Handler handler = new Handler(Looper.getMainLooper());

        executorService.execute(() -> {
            sharedpref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            if (!sharedpref.getBoolean(KEY_DID_RUN, false)) {
                copyAssetImagesToInternalStorage();
                sharedpref.edit().putBoolean(KEY_DID_RUN, true).apply();
            }
            java.util.ArrayList<ImageModel> tempImages = getImageListsFromInternalStorage();

            handler.post(() -> {
                images.clear();
                images.addAll(tempImages);
                imageAdapter.notifyDataSetChanged();
            });
        });


    }
    private void copyAssetImagesToInternalStorage(){
        for (String image : getAssetsList()) {
            File copiedFile = new File(context.getFilesDir(), image);
            if (copiedFile.exists()) continue;

            try(InputStream inputStream = context.getAssets().open("img/" + image);
                OutputStream outputStream = new FileOutputStream(copiedFile)){
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
    private ArrayList<ImageModel> getImageListsFromInternalStorage(){
        ArrayList<ImageModel> imageList = new ArrayList<>();
        File internalStorage = context.getFilesDir();

        File[] imageFiles =internalStorage.listFiles((dir, name) -> {
            String lowerCaseName = name.toLowerCase();
            return lowerCaseName.endsWith(".jpg") ||lowerCaseName.endsWith(".jpeg") || lowerCaseName.endsWith(".png");
        });
        if (imageFiles != null) {
            Arrays.sort(imageFiles, Comparator.comparingLong(File::lastModified));

            for (File file : imageFiles) {
                long fileSize = file.length();
                long fileDate = file.lastModified();
                Uri fileUri = Uri.fromFile(file);

                imageList.add(new ImageModel(fileSize, fileDate, fileUri));
            }
        }
        return imageList;
    }
}