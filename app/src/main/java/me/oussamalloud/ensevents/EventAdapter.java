package me.oussamalloud.ensevents;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventViewHolder> {

    private Context context;
    private List<Event> eventList = new ArrayList<>();
    public static final String EVENT_ID = "EVENT_ID";

    public EventAdapter(Context context) {
        this.context = context;
    }
    public void setEventList(List<Event> eventList){
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.item_events, null);
        return new EventViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

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

        //Listener click for interested button
        holder.interestedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.interestedBtn.setEnabled(false);
                FirebaseDatabase.getInstance().getReference("users-interested-event")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(event.getEventId()).setValue(event).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Snackbar.make(view.findViewById(R.id.interestedBtn), "Event added to your list...", Snackbar.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(context.getApplicationContext(), "Error ! Please Verify your connexion.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });


    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }
}
