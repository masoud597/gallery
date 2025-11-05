package com.example.gallery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<ImageModel> imageModelArraylist;

    public ImageAdapter(Context context, ArrayList<ImageModel> imageModelArraylist) {
        this.context = context;
        this.imageModelArraylist = imageModelArraylist;
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

        Glide.with(context)
                .load(model.get_ImageURI())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_foreground)
                .into(holder.imageView);
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
