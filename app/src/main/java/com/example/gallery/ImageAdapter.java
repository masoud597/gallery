package com.example.gallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private static final int SAMPLE_WIDTH = 150;
    private static final int SAMPLE_HEIGHT = 150;

    private final ArrayList<ImageModel> imageModelArraylist;
    ExecutorService executor;
    private final OnImageLongClickListener longClickListener;
    public interface OnImageLongClickListener {
        void onImageLongClicked(int position);
    }
    public ImageAdapter(ArrayList<ImageModel> imageModelArraylist, OnImageLongClickListener listener) {
        this.imageModelArraylist = imageModelArraylist;
        this.executor = Executors.newFixedThreadPool(4);
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageModel model = imageModelArraylist.get(position);

        holder.textDate.setText(model.get_ImageDate());
        holder.textSize.setText(model.get_ImageSize());
        holder.imageView.setImageResource(R.drawable.ic_launcher_background);

        Handler handler = new Handler(Looper.getMainLooper());

        holder.imageView.setTag(model.get_ImageURI().toString());
        executor.execute(() -> {
            Bitmap bitmap;

            try {
                bitmap = decodeSampledBitmap(model.get_ImageURI());

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Bitmap finalBitmap = bitmap;
            handler.post(() -> {
                if (holder.imageView.getTag().equals(model.get_ImageURI().toString())) {
                    if (finalBitmap != null) {
                        holder.imageView.setImageBitmap(finalBitmap);
                    }else {
                        holder.imageView.setImageResource(R.drawable.ic_launcher_foreground);
                    }
                }
            });
        });
        holder.itemView.setOnLongClickListener( v -> {
            if (longClickListener != null) {
                int currentPosition = holder.getBindingAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    longClickListener.onImageLongClicked(currentPosition);
                }
                return true;
            }
            return false;
        });
    }

    private Bitmap decodeSampledBitmap(Uri fileUri) throws IOException {
        String filePath = fileUri.getPath();
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(filePath, options);

        options.inSampleSize = calculateSampleSize(options);
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(filePath, options);
    }

    private int calculateSampleSize(BitmapFactory.Options options) {
        final int acWidth = options.outWidth;
        final int acHeight = options.outHeight;
        int sampleSize = 1;

        if (acHeight > ImageAdapter.SAMPLE_HEIGHT || acWidth > ImageAdapter.SAMPLE_WIDTH) {
            final int halfHeight = acHeight / 2;
            final int halfWidth = acWidth / 2;

            while ((halfHeight / sampleSize) >= ImageAdapter.SAMPLE_HEIGHT && (halfWidth / sampleSize) >= ImageAdapter.SAMPLE_WIDTH) {
                sampleSize *= 2;
            }
        }
        return sampleSize;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        executor.shutdown();

    }

    @Override
    public int getItemCount() {
        return imageModelArraylist.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView textDate;
        private final TextView textSize;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.cardIMG);
            textDate = itemView.findViewById(R.id.cardDate);
            textSize = itemView.findViewById(R.id.cardSize);
        }

    }
}
