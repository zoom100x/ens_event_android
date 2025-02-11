package me.oussamalloud.ensevents;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class FavoritesEventsListAdapter extends RecyclerView.Adapter<FavoritesEventsViewHolder> {

    private Context context;
    private List<Event> favoritesEventList = new ArrayList<>();
    public static final String EVENT_ID = "EVENT_ID";

    public FavoritesEventsListAdapter(Context context) {
        this.context = context;
    }
    public void setFavoritesEventList(List<Event> favoritesEventList){
        this.favoritesEventList = favoritesEventList;
    }

    @NonNull
    @Override
    public FavoritesEventsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.item_favorite_event, null);
        return new FavoritesEventsViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull FavoritesEventsViewHolder holder, int position) {
        Event event = favoritesEventList.get(position);


        holder.eventTitle.setText(event.getEventTitle());
        //Listener click for eventTitle
        holder.eventTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent (view.getContext(), EventActivity.class);
                intent.putExtra(EVENT_ID, event.getEventId());
                view.getContext().startActivity(intent);
            }
        });

        holder.eventDate.setText(event.getEventDate());
        holder.eventLocation.setText(event.getEventLocation());
        holder.eventTime.setText(event.getEventTime());
        holder.progressBar.setVisibility(View.VISIBLE);
        //Listener click for eventPicture
        holder.eventPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent (view.getContext(), EventActivity.class);
                intent.putExtra(EVENT_ID, event.getEventId());
                view.getContext().startActivity(intent);
            }
        });

        //download image from firebase
        FirebaseStorage.getInstance().getReference(event.getEventPicture()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                holder.progressBar.setVisibility(View.GONE);
                Picasso.get().load(uri.toString()).into(holder.eventPicture);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context.getApplicationContext(),"Err :"+ e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
        holder.removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase.getInstance().getReference("users-interested-event")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(event.getEventId()).removeValue();
                Snackbar.make(view.findViewById(R.id.removeBtn), "Event removed successfully...", 1500).show();
            }
        });


    }
    @Override
    public int getItemCount() {
        return favoritesEventList.size();
    }
}
