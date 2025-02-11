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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FavoritesEventsActivity extends Fragment implements View.OnClickListener {

    private RecyclerView favoritesEventPlace;
    private ImageView addEventBtn;
    private FavoritesEventsListAdapter favoritesEventsListAdapter;
    private LinearLayout noFavoriteEvents;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.favorites_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        favoritesEventPlace = view.findViewById(R.id.favoritesEventPlace);

        addEventBtn = view.findViewById(R.id.addEventBtn);
        addEventBtn.setOnClickListener(this);

        noFavoriteEvents = view.findViewById(R.id.noFavoriteEvents);

        progressBar = view.findViewById(R.id.progressBar_favoritesEvents);
        progressBar.setVisibility(View.VISIBLE);
        //Import events from firebase db
        FirebaseDatabase.getInstance().getReference("users-interested-event")
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
                        if(events.isEmpty()){
                            progressBar.setVisibility(View.GONE);
                            noFavoriteEvents.setVisibility(View.VISIBLE);
                            favoritesEventsListAdapter.setFavoritesEventList(events);
                            favoritesEventsListAdapter.notifyDataSetChanged();
                        }
                        else {
                            noFavoriteEvents.setVisibility(View.GONE);
                            Collections.reverse(events);
                            favoritesEventsListAdapter.setFavoritesEventList(events);
                            favoritesEventsListAdapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //set events in the recyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        favoritesEventPlace.setLayoutManager(linearLayoutManager);
        favoritesEventPlace.setHasFixedSize(true);
        EventViewSpacingItems eventViewSpacingItems = new EventViewSpacingItems(80);
        favoritesEventPlace.addItemDecoration(eventViewSpacingItems);
        favoritesEventsListAdapter = new FavoritesEventsListAdapter(getContext());
        favoritesEventPlace.setAdapter(favoritesEventsListAdapter);

        //snapping items to center

        SnapHelper snapHelper = new GravitySnapHelper(Gravity.CENTER);
        snapHelper.attachToRecyclerView(favoritesEventPlace);
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
