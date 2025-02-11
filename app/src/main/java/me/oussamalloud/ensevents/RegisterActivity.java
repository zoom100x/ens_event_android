package me.oussamalloud.ensevents;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaCodec;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;


public class RegisterActivity extends Activity implements View.OnClickListener {

    private EditText fullNameRegister, emailRegister, passwordRegister1,passwordRegister2;
    private TextView registerButton, login;
    private ProgressBar progressBar;
    private Spinner spinnerOfType;
    private String[] spinnerItems = Personnel.TYPES;


    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);
        mAuth = FirebaseAuth.getInstance();

        registerButton = (Button) findViewById(R.id.registerButton);
        registerButton.setOnClickListener(this);

        login = findViewById(R.id.login);
        login.setOnClickListener(this);

        progressBar = findViewById(R.id.progress_circular);

        fullNameRegister = findViewById(R.id.fullNameRegister);
        emailRegister = findViewById(R.id.emailRegister);
        passwordRegister1 = findViewById(R.id.passwordRegister1);
        passwordRegister2 = findViewById(R.id.passwordRegister2);
        spinnerOfType = findViewById(R.id.typeSpinner);

        //set items on spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(RegisterActivity.this, R.layout.spinner_item, spinnerItems);
        spinnerOfType.setAdapter(adapter);


    }



    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.login:
                startActivity(new Intent(this, LoginActivity.class));
                break;
            case R.id.registerButton:
                registerUser();
                break;
        }

    }

    private void registerUser() {
        String fullName = fullNameRegister.getText().toString();
        String email = emailRegister.getText().toString();
        String type = spinnerOfType.getSelectedItem().toString();
        String password = passwordRegister1.getText().toString();
        String re_password = passwordRegister2.getText().toString();

        if(fullName.isEmpty()){
            fullNameRegister.setError("You must enter your full name correctly please. ex: \"John Doe\"");
            fullNameRegister.requestFocus();
        }

        if(email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailRegister.setError("You must enter your email correctly please. ex: \"John.Doe@test.com\"");
            emailRegister.requestFocus();
            return;
        }

        if(password.length() < 6 || password.isEmpty()){
            passwordRegister1.setError("You must enter a strong password please. ex: \"John@2022\"");
            passwordRegister1.requestFocus();
            return;
        }

        if(!re_password.equals(password)){
            passwordRegister2.setError("You must enter the same password please.");
            passwordRegister2.requestFocus();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Personnel user = new Personnel(fullName, email, type);

                            FirebaseDatabase.getInstance().getReference("users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(getApplicationContext(), "Registering SUCCESS", Toast.LENGTH_LONG).show();
                                    }
                                    else{
                                        Toast.makeText(getApplicationContext(), "Registering FAILED try again !", Toast.LENGTH_LONG).show();
                                    }
                                    progressBar.setVisibility(View.GONE);
                                }
                            });

                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Registering FAILED try again !", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }

}
