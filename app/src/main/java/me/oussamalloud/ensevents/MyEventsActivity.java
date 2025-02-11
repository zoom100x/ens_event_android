package me.oussamalloud.ensevents;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyEventsActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView goBack;

    private MyEventsListAdapter myEventsListAdapter;
    private RecyclerView myEventsPlace;
    private LinearLayout noEventsHere;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_events_layout);

        goBack = findViewById(R.id.goBack);
        goBack.setOnClickListener(this);

        noEventsHere = findViewById(R.id.noEvents);
        noEventsHere.setOnClickListener(this);

        myEventsPlace = findViewById(R.id.myEventsPlace);
        progressBar = findViewById(R.id.progressBar4);
        progressBar.setVisibility(View.VISIBLE);

        //Import events from firebase db
        FirebaseDatabase.getInstance().getReference("users-events")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Event> events = new ArrayList<>();
                        for (DataSnapshot data : snapshot.getChildren()){
                            Event e = data.getValue(Event.class);

                            if (!events.contains(e)){
                                events.add(e);
                            }

                        }
                        if (events.isEmpty()){
                            progressBar.setVisibility(View.GONE);
                            noEventsHere.setVisibility(View.VISIBLE);
                            myEventsListAdapter.setMyEventList(events);
                            myEventsListAdapter.notifyDataSetChanged();
                        }
                        else{
                            noEventsHere.setVisibility(View.GONE);
                            Collections.reverse(events);
                            myEventsListAdapter.setMyEventList(events);
                            myEventsListAdapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //set events in the recyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MyEventsActivity.this, LinearLayoutManager.HORIZONTAL, false);
        myEventsPlace.setLayoutManager(linearLayoutManager);
        myEventsPlace.setHasFixedSize(true);
        EventViewSpacingItems eventViewSpacingItems = new EventViewSpacingItems(80);
        myEventsPlace.addItemDecoration(eventViewSpacingItems);
        myEventsListAdapter = new MyEventsListAdapter(MyEventsActivity.this);
        myEventsPlace.setAdapter(myEventsListAdapter);

        //snapping items to center

        SnapHelper snapHelper = new GravitySnapHelper(Gravity.CENTER);
        snapHelper.attachToRecyclerView(myEventsPlace);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.goBack:
                finish();
                break;
            case R.id.noEvents:
                startActivity(new Intent(this, AddEventActivity.class));
                break;
        }
    }
}
