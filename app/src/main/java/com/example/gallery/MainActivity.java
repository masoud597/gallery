package com.example.gallery;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements ImageAdapter.OnImageLongClickListener {

    public static final String PREFS_NAME = "GPref";
    public static final String KEY_DID_RUN = "DidRun";
    private Context context;
    private GridLayoutManager gridLayoutManager;
    private SharedPreferences sharedpref;
    ImageAdapter imageAdapter;

    private final ArrayList<ImageModel> images = new ArrayList<>();
    private ActivityResultLauncher<String> selectImage;

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
        RecyclerView recView = findViewById(R.id.recView);
        CustomFAB bottomPanel = findViewById(R.id.bottomPanel);
        gridLayoutManager = new GridLayoutManager(this, 2);
        recView.setLayoutManager(gridLayoutManager);

        selectImage = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                copySelectedImageToInternalStorage(uri);
            }
        });

        imageAdapter = new ImageAdapter(images, this);
        recView.setAdapter(imageAdapter);

        loadImages();
        bottomPanel.setOnSelectedColumnChangeListener(c -> gridLayoutManager.setSpanCount(c));
        bottomPanel.setOpenButtonOnClickListener(() -> selectImage.launch("image/*"));
    }
    private void copySelectedImageToInternalStorage(Uri uri) {
        try (ExecutorService executorService = Executors.newSingleThreadExecutor()) {
            Handler handler = new Handler(Looper.getMainLooper());

            executorService.execute(() -> {
                long time = System.currentTimeMillis();
                File newFile = new File(context.getFilesDir(), "IMG_" + time + ".jpg");
                try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
                     OutputStream outputStream = new FileOutputStream(newFile)) {
                    if (inputStream == null) return;
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                handler.post(this::loadImages);
            });
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    private void loadImages() {
        try (ExecutorService executorService = Executors.newFixedThreadPool(4)) {
            Handler handler = new Handler(Looper.getMainLooper());

            executorService.execute(() -> {
                sharedpref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                if (!sharedpref.getBoolean(KEY_DID_RUN, false)) {
                    copyAssetImagesToInternalStorage();
                    sharedpref.edit().putBoolean(KEY_DID_RUN, true).apply();
                }
                ArrayList<ImageModel> tempImages = getImageListsFromInternalStorage();

                handler.post(() -> {
                    images.clear();
                    images.addAll(tempImages);
                    imageAdapter.notifyDataSetChanged();
                });
            });
        }


    }
    private void copyAssetImagesToInternalStorage(){
        String[] listImages;
        if (context != null) {
            try {
                listImages = context.getAssets().list("img");
                if (listImages == null) listImages = new String[0];
            } catch (IOException e) {
                listImages = new String[0];
            }
        }else {
            listImages = new String[0];
        }

        for (String image : listImages) {
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

    @Override
    public void onImageLongClicked(int position) {
        ImageModel imgToDelete = images.get(position);
        new AlertDialog.Builder(this)
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to delete this image?")
                .setPositiveButton("Delete", (dialog, which) -> deleteImage(imgToDelete, position))
                .setNegativeButton("Cancel", null)
                .show();

    }

    private void deleteImage(ImageModel imgToDelete, int position) {
        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            Handler handler = new Handler(getMainLooper());

            executor.execute(() -> {
                File fileToDelete = new File(Objects.requireNonNull(imgToDelete.get_ImageURI().getPath()));
                boolean gotDeleted;
                if (fileToDelete.exists()) gotDeleted = fileToDelete.delete();
                else {
                    gotDeleted = false;
                }

                handler.post(() -> {
                    if (gotDeleted) {
                        images.remove(position);
                        imageAdapter.notifyItemRemoved(position);
                        imageAdapter.notifyItemRangeChanged(position, images.size());
                    }
                });
            });
        }
    }
}