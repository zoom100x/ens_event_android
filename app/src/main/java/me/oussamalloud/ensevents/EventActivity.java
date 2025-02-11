package me.oussamalloud.ensevents;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

public class EventActivity extends Activity implements View.OnClickListener {

    private TextView eventTitle, eventDate, eventTime, eventLocation, eventDescription, eventOwnerName, eventOwnerType;
    private ImageView eventPicture, returnBack, locationImage;
    private String eventId;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_activity);

        eventTitle = findViewById(R.id.eventTitle);
        eventDate = findViewById(R.id.eventDate);
        eventTime = findViewById(R.id.eventTime);
        eventLocation = findViewById(R.id.eventLocation);
        eventDescription = findViewById(R.id.eventDescription);
        eventPicture = findViewById(R.id.eventPicture);
        eventOwnerName = findViewById(R.id.eventOwnerName);
        eventOwnerType = findViewById(R.id.eventOwnerType);
        locationImage= findViewById(R.id.image_location);

        progressBar = findViewById(R.id.progressBar4);
        progressBar.setVisibility(View.VISIBLE);

        returnBack = findViewById(R.id.returnBack);
        returnBack.setOnClickListener(this);

        Intent getData = getIntent();
        eventId = getData.getStringExtra(EventAdapter.EVENT_ID);
        eventId = getData.getStringExtra(FavoritesEventsListAdapter.EVENT_ID);
        eventId = getData.getStringExtra(MyEventsListAdapter.EVENT_ID);
        eventId = getData.getStringExtra(AllEventCarte.EVENT_ID);

        if(eventId == null){
            Toast.makeText(getApplicationContext(), "error : event id is null", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseDatabase.getInstance().getReference("events")
                .child(eventId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Event e = snapshot.getValue(Event.class);
                eventTitle.setText(e.getEventTitle());
                eventDate.setText(e.getEventDate());
                eventTime.setText(e.getEventTime());
                eventLocation.setText(e.getEventLocation());
                eventDescription.setText(e.getEventDescription());
                FirebaseStorage.getInstance().getReference(e.getEventPicture())
                        .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        progressBar.setVisibility(View.GONE);
                        Picasso.get().load(uri).placeholder(R.drawable.ic_baseline_photo_24).error(R.drawable.ic_baseline_error_24).into(eventPicture);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(EventActivity.this, e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
                progressBar.setVisibility(View.VISIBLE);
                FirebaseStorage.getInstance().getReference(e.getLocationImage())
                        .getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            Picasso.get().load(task.getResult()).placeholder(R.drawable.ic_baseline_photo_24).into(locationImage);
                        }
                        else{
                            Toast.makeText(EventActivity.this, "Location image not available.", Toast.LENGTH_SHORT).show();
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                });

                FirebaseDatabase.getInstance().getReference("users")
                        .child(e.getOwnerOfEvent()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Personnel p = snapshot.getValue(Personnel.class);
                        eventOwnerName.setText(p.getFullName());
                        eventOwnerType.setText(p.getType() + " :");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(EventActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EventActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.returnBack:
                finish();
                break;
        }
    }


}
