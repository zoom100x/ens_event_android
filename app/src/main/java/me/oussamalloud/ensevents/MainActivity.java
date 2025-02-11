package me.oussamalloud.ensevents;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, BottomNavigationView.OnNavigationItemSelectedListener {

    private TextView userFullName;
    private ProgressBar progressBar1;
    private ImageView profileBtn;
    private TextView timeHandler;

    private ActionBar toolbar;


    private FirebaseUser user;
    private DatabaseReference reference;
    private String userId = "";



    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = getSupportActionBar();

        BottomNavigationView navigation = findViewById(R.id.bottomNavigationView);
        navigation.setOnNavigationItemSelectedListener(this);
        navigation.setSelectedItemId(R.id.home);

        userFullName = findViewById(R.id.userFullName);
        progressBar1 = findViewById(R.id.progressBar1);

        profileBtn = findViewById(R.id.profileBtn);
        profileBtn.setOnClickListener(this);

        timeHandler = findViewById(R.id.timeHandler);
        int hour = new Date().getHours()+1;

        if (hour >= 1 && hour < 12){
            timeHandler.setText("Good Morning,");
        }
        else if (hour >= 12 && hour < 16){
            timeHandler.setText("Good Afternoon,");
        }
        else if (hour >= 16 && hour < 21){
            timeHandler.setText("Good Evening,");
        }
        else if (hour >= 21 && hour < 24){
            timeHandler.setText("Good Night,");
        }

        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
        else{
            userId = user.getUid();

            reference = FirebaseDatabase.getInstance().getReference("users");



            progressBar1.setVisibility(View.VISIBLE);
            reference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Personnel personnel = snapshot.getValue(Personnel.class);
                    userFullName.setText(personnel.getFullName());
                    progressBar1.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this, "Something wrong happened! Try again", Toast.LENGTH_SHORT).show();

                }
            });
        }



    }

    //actionBar handler
    PopularEvents popularEvents = new PopularEvents();
    FavoritesEventsActivity favoritesEventsActivity = new FavoritesEventsActivity();
    AllEventCarte allEventCarte = new AllEventCarte();
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                loadFragment(popularEvents);
                return true;
            case R.id.favorite:
                loadFragment(favoritesEventsActivity);
                return true;
            case R.id.carte:
                loadFragment(allEventCarte);
                break;
        }
        return false;
    }
    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.profileBtn:
                startActivity(new Intent(this, ProfileActivity.class));
                break;
        }
    }


}