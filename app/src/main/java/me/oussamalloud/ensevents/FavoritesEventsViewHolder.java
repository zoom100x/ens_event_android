package me.oussamalloud.ensevents;

import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

public class FavoritesEventsViewHolder extends RecyclerView.ViewHolder {
    TextView eventTitle, eventDate, eventLocation, eventTime;
    Button removeBtn;
    ProgressBar progressBar;
    ShapeableImageView eventPicture;

    public FavoritesEventsViewHolder(@NonNull View itemView) {
        super(itemView);
        eventTitle = itemView.findViewById(R.id.titre_event);
        eventDate = itemView.findViewById(R.id.date_event);
        eventTime = itemView.findViewById(R.id.time_event);
        eventLocation = itemView.findViewById(R.id.localisation);
        eventPicture = itemView.findViewById(R.id.image_event);
        removeBtn = itemView.findViewById(R.id.removeBtn);
        progressBar = itemView.findViewById(R.id.progressBar_item);

    }
}
