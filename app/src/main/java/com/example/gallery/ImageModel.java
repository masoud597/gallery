package com.example.gallery;

import android.net.Uri;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageModel {
    private Uri _ImageURI;
    private String _ImageDate;
    private String _ImageSize;

    public ImageModel(Long imageSize, Long imageDate, Uri imageID) {
        Date date = new Date(imageDate);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());

        double sizeInMB = (double) imageSize / (1024 * 1024);
        _ImageSize = String.format(Locale.getDefault(), "%.2f MB", sizeInMB);
        _ImageDate = sdf.format(date);
        _ImageURI = imageID;
    }

    public Uri get_ImageURI() {
        return _ImageURI;
    }
    public String get_ImageDate() {
        return _ImageDate;
    }
    public String get_ImageSize() {
        return _ImageSize;
    }
}
