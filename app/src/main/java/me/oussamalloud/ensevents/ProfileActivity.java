package me.oussamalloud.ensevents;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView goBackBtn;
    private Button showMyEventsBtn, logoutBtn;
    private TextView userFullName, userEmail, userType;

    private FirebaseUser user;
    private DatabaseReference reference;
    private String userId = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_layout);

        goBackBtn = findViewById(R.id.goBackBtn);
        goBackBtn.setOnClickListener(this);

        showMyEventsBtn = findViewById(R.id.showMyEventsBtn);
        showMyEventsBtn.setOnClickListener(this);

        logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(this);

        userFullName = findViewById(R.id.userFullName);
        userEmail = findViewById(R.id.userEmail);
        userType = findViewById(R.id.userType);

        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("users");
        if (user != null){
            userId = user.getUid();
        }


        reference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Personnel p = snapshot.getValue(Personnel.class);

                userFullName.setText(p.getFullName());
                userEmail.setText(p.getEmail());
                userType.setText(p.getType());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Something wrong happened! Try again", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onClick(View view) {

        switch(view.getId()){
            case R.id.showMyEventsBtn:
                startActivity(new Intent(this, MyEventsActivity.class));
                break;
            case R.id.logoutBtn:
                FirebaseAuth.getInstance().signOut();
                finish();
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
            case R.id.goBackBtn:
                finish();
                break;
        }

    }
}
