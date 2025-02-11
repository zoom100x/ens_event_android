package me.oussamalloud.ensevents;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PopularEvents extends Fragment implements View.OnClickListener {

    private RecyclerView eventPlace;
    private ImageView addEventBtn;
    private EventAdapter eventAdapter;
    private LinearLayout noEventsHere;
    private ProgressBar progressBar;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        eventAdapter = new EventAdapter(getContext());
        return inflater.inflate(R.layout.popular_event_layout, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventPlace = view.findViewById(R.id.eventPlace);

        addEventBtn = view.findViewById(R.id.addEventBtn);
        addEventBtn.setOnClickListener(this);

        noEventsHere = view.findViewById(R.id.noPopularEvents);

        progressBar = view.findViewById(R.id.progressBar_popularEvent);
        progressBar.setVisibility(View.VISIBLE);



        //Import events from firebase db
        FirebaseDatabase.getInstance().getReference("events")
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
                        //verify if the list isEmpty
                        if(events.size() == 0){
                            progressBar.setVisibility(View.GONE);
                            noEventsHere.setVisibility(View.VISIBLE);
                            eventAdapter.setEventList(events);
                            eventAdapter.notifyDataSetChanged();
                        }
                        else {
                            noEventsHere.setVisibility(View.GONE);
                            Collections.reverse(events);
                            eventAdapter.setEventList(events);
                            eventAdapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



        //set events in the recyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        eventPlace.setLayoutManager(linearLayoutManager);
        eventPlace.setHasFixedSize(true);
        EventViewSpacingItems eventViewSpacingItems = new EventViewSpacingItems(80);
        eventPlace.addItemDecoration(eventViewSpacingItems);
        eventPlace.setAdapter(eventAdapter);


        //snapping items to center

        SnapHelper snapHelper = new GravitySnapHelper(Gravity.CENTER);
        snapHelper.attachToRecyclerView(eventPlace);
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.addEventBtn:
                startActivity(new Intent(getActivity(), AddEventActivity.class));
                break;
        }

    }
}
