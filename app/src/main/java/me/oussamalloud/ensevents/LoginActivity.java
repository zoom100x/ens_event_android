package me.oussamalloud.ensevents;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends Activity implements View.OnClickListener {
    private EditText emailLogin, passwordLogin;
    private TextView loginButton, register;
    private ProgressBar progressBar;
    private ImageButton fbLogin, googleLogin, microsoftLogin;
    private LoginButton fbLoginManager;
    private Spinner spinnerOfType;
    private String[] spinnerItems = Personnel.TYPES;


    private FirebaseAuth mAuth;

    private GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 123;
    private CallbackManager mCallbackManager;
    private OAuthProvider.Builder provider = OAuthProvider.newBuilder("microsoft.com");


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        mAuth = FirebaseAuth.getInstance();

        register = findViewById(R.id.register);
        register.setOnClickListener(this);

        loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);

        fbLogin = findViewById(R.id.fbLogin);
        fbLogin.setOnClickListener(this);

        googleLogin = findViewById(R.id.googleLogin);
        googleLogin.setOnClickListener(this);

        microsoftLogin = findViewById(R.id.microsoftLogin);
        microsoftLogin.setOnClickListener(this);

        spinnerOfType = findViewById(R.id.typeSpinner);
        //set items on spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(LoginActivity.this, R.layout.spinner_item, spinnerItems);
        spinnerOfType.setAdapter(adapter);

        emailLogin = findViewById(R.id.emailLogin);
        passwordLogin = findViewById(R.id.passwordLogin);
        progressBar = findViewById(R.id.progressBar);

        //google login
        createGoogleRequest();

        //facebook login
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        initializeFacebookLogin();

    }

    //methode for fb login
    private void initializeFacebookLogin(){
        mCallbackManager = CallbackManager.Factory.create();
        fbLoginManager = findViewById(R.id.login_button_fb);
        fbLoginManager.setReadPermissions("email", "public_profile");
        fbLoginManager.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                firebaseAuthWithFb(loginResult.getAccessToken());


            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, "signIn with Fb CANCELLED",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(LoginActivity.this, "Error : "+ error.getMessage(),
                        Toast.LENGTH_SHORT).show();

            }
        });
    }
    private void firebaseAuthWithFb(AccessToken token) {

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            GraphRequest request = GraphRequest.newMeRequest(
                                    token,
                                    new GraphRequest.GraphJSONObjectCallback() {
                                        @Override
                                        public void onCompleted(JSONObject object, GraphResponse response) {

                                            // Application code
                                            String email = null;
                                            try {
                                                email = object.getString("email");
                                            } catch (JSONException e) {
                                                Toast.makeText(getApplicationContext(), "errr : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                            getDataFromFbAccount(email);
                                        }
                                    });
                            Bundle parameters = new Bundle();
                            parameters.putString("fields", "id,name,email");
                            request.setParameters(parameters);
                            request.executeAsync();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                            Toast.makeText(getApplicationContext(), "signInWithCredential:success",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void getDataFromFbAccount(String email){
        String fullName;
        String type = spinnerOfType.getSelectedItem().toString();
        Profile currentProfile = Profile.getCurrentProfile();
        if (currentProfile != null){
            fullName = currentProfile.getName();
            Personnel user = new Personnel(fullName, email, type);

            FirebaseDatabase.getInstance().getReference("users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){

                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Registering FAILED try again !", Toast.LENGTH_LONG).show();
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });
        }

    }

    //methode for google login
    private void createGoogleRequest() {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    private void firebaseAuthWithGoogle(String idToken) {

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            getDataFromGoogleAccount();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            Toast.makeText(getApplicationContext(), "signInWithCredential:success",
                                    Toast.LENGTH_SHORT).show();
                            finish();


                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Something wrong try again!", Toast.LENGTH_SHORT).show();

                        }
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }
    private void getDataFromGoogleAccount() {
        String fullName ;
        String email ;
        String type = spinnerOfType.getSelectedItem().toString();
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (signInAccount != null){
            fullName = signInAccount.getDisplayName();
            email = signInAccount.getEmail();
            Personnel user = new Personnel(fullName, email, type);

            FirebaseDatabase.getInstance().getReference("users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){

                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Registering FAILED try again !", Toast.LENGTH_LONG).show();
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }

    //methode for microsoft login
    private void microsoftSignIn(){
        Task<AuthResult> pendingResultTask = mAuth.getPendingAuthResult();
        if (pendingResultTask != null) {
            // There's something already here! Finish the sign-in for your user.
            pendingResultTask
                    .addOnSuccessListener(
                            new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    // User is signed in.
                                    // IdP data available in
                                    // authResult.getAdditionalUserInfo().getProfile().
                                    // The OAuth access token can also be retrieved:
                                    // authResult.getCredential().getAccessToken().
                                    // The OAuth ID token can also be retrieved:
                                    // authResult.getCredential().getIdToken().
                                    getDataFromMicrosoftAccount(authResult.getUser().getDisplayName(), authResult.getUser().getEmail());
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    Toast.makeText(getApplicationContext(), "signInWithCredential:success",
                                            Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Handle failure.
                                    Toast.makeText(LoginActivity.this, "Something is wrong try again!", Toast.LENGTH_SHORT).show();
                                }
                            });
        } else {
            // There's no pending result so you need to start the sign-in flow.
            mAuth.startActivityForSignInWithProvider(/* activity= */ this, provider.build())
                    .addOnSuccessListener(
                            new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    // User is signed in.
                                    // IdP data available in
                                    // authResult.getAdditionalUserInfo().getProfile().
                                    // The OAuth access token can also be retrieved:
                                    // authResult.getCredential().getAccessToken().
                                    // The OAuth ID token can also be retrieved:

                                    getDataFromMicrosoftAccount(authResult.getUser().getDisplayName(), authResult.getUser().getEmail());
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    Toast.makeText(getApplicationContext(), "signInWithCredential:success",
                                            Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Handle failure.
                                    Toast.makeText(LoginActivity.this, "Something is wrong try again!\n"+ e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
        }
    }
    private void getDataFromMicrosoftAccount(String fullName, String email){

        String type = spinnerOfType.getSelectedItem().toString();

        Personnel user = new Personnel(fullName, email, type);

        FirebaseDatabase.getInstance().getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){

                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Registering FAILED try again !", Toast.LENGTH_LONG).show();
                    }
                    progressBar.setVisibility(View.GONE);
                }
        });

    }

    //login with email/password
    private void userLogin() {
        String email = emailLogin.getText().toString().trim();
        String password = passwordLogin.getText().toString().trim();

        if(email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailLogin.setError("You must enter your email correctly please. ex: \"John.Doe@test.com\"");
            emailLogin.requestFocus();
            return;
        }

        if (password.isEmpty()){
            passwordLogin.setError("Enter a valid password please");
            passwordLogin.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                    progressBar.setVisibility(View.GONE);
                }
                else{
                    Toast.makeText(LoginActivity.this, "Login failed verify your credentials", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, e.getStatus().toString(), Toast.LENGTH_SHORT).show();

            }
        }

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
    public void onClick(View view) {
        switch(view.getId()){
            case(R.id.register):
                Intent toRegister = new Intent(this, RegisterActivity.class);
                startActivity(toRegister);
                break;
            case(R.id.loginButton):
                userLogin();
                break;
            case (R.id.googleLogin):
                signIn();
                break;
            case (R.id.fbLogin):
                fbLoginManager.performClick();
                break;
            case (R.id.microsoftLogin):
                microsoftSignIn();
                break;
        }

    }
}
