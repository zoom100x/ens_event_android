package me.oussamalloud.ensevents;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddEventActivity extends Activity implements View.OnClickListener {

    private EditText addTitle, addDate, addTime, addLocation, addDescription;
    private TextView longitude_text_view, latitude_text_view;
    private Button creationButton;
    private ImageView returnBack, addImage, imageLocation;
    private ImageButton addImageLocationBtn, addLocationInfoBtn;
    private ProgressBar uploadProgress;
    private ConstraintLayout processIndicator;
    private TextView processIndicatorText ;

    private final Calendar myCalendar= Calendar.getInstance();
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private String currentPhotoPath;

    public static double longitude;
    public static double latitude;

    private FirebaseStorage storage;
    private StorageReference storageRef;
    private Uri imageUri;
    private UploadTask uploadTask;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_event_activity);

        addTitle = findViewById(R.id.addTitle);

        addDate = findViewById(R.id.addDate);

        addDate.setOnClickListener(this);


        addTime =findViewById(R.id.addTime);
        addTime.setOnClickListener(this);


        addLocation = findViewById(R.id.addLocation);
        addDescription = findViewById(R.id.addDescription);
        uploadProgress = findViewById(R.id.uploadProgress);
        processIndicator = findViewById(R.id.processIndicator);
        processIndicatorText = findViewById(R.id.about_creating_event);

        latitude_text_view = findViewById(R.id.latitude_text_view);
        longitude_text_view = findViewById(R.id.longitude_text_view);

        creationButton = findViewById(R.id.createEventBtn);
        creationButton.setOnClickListener(this);

        returnBack = findViewById(R.id.returnBack);
        returnBack.setOnClickListener(this);

        addImage = findViewById(R.id.addImage);
        addImage.setOnClickListener(this);

        imageLocation = findViewById(R.id.image_location);
        addImageLocationBtn = findViewById(R.id.add_image_location_btn);
        addImageLocationBtn.setOnClickListener(this);
        addLocationInfoBtn = findViewById(R.id.add_location_info_btn);
        addLocationInfoBtn.setOnClickListener(this);

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

    }

    @Override
    protected void onResume() {
        super.onResume();

        String latitudeText = "Latitude : " + latitude;
        String longitudeText = "Longitude : " + longitude;

        latitude_text_view.setText(latitudeText);
        longitude_text_view.setText(longitudeText);
    }

    private void updateLabel(){
        String myFormat="dd/MM/yyyy";
        SimpleDateFormat dateFormat=new SimpleDateFormat(myFormat, Locale.FRANCE);
        addDate.setText(dateFormat.format(myCalendar.getTime()));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.createEventBtn:
                createEvent();
                break;
            case R.id.returnBack:
                finish();
                break;
            case R.id.addImage:
                getImageFromStorage();
                break;
            case R.id.addTime:
                Calendar currentTime = Calendar.getInstance();
                int hour = currentTime.get(Calendar.HOUR_OF_DAY);
                int minute = currentTime.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddEventActivity.this, new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        addTime.setText(i + " : "+ i1);
                    }

                }, hour, minute, true);
                timePickerDialog.show();
                break;
            case R.id.addDate:
                DatePickerDialog.OnDateSetListener date =new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH,month);
                        myCalendar.set(Calendar.DAY_OF_MONTH,day);
                        updateLabel();
                    }
                };
                new DatePickerDialog(AddEventActivity.this,date,myCalendar.get(Calendar.YEAR),myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                break;
            case R.id.add_image_location_btn:
                dispatchTakePictureIntent();
                break;
            case R.id.add_location_info_btn:
                startActivity(new Intent(AddEventActivity.this, MapsActivity.class));
                break;

        }
    }



    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRENCH).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(this, "Error ! " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "me.oussamalloud.ensevents.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }



    private void getImageFromStorage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //get event image
        if (requestCode == 1 && resultCode == RESULT_OK && data.getData() != null && data != null){
            imageUri = data.getData();
            Picasso.get().load(imageUri).into(addImage);

        }
        //get location image
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            File image = new File(currentPhotoPath);
            Picasso.get().load(image).placeholder(R.drawable.ic_baseline_photo_24).into(imageLocation);
        }
    }

    private String getExtensionFile(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cR.getType(uri));

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createEvent() {
        creationButton.setEnabled(false);

        String titre = addTitle.getText().toString();
        String date = addDate.getText().toString();
        String time = addTime.getText().toString();
        String location = addLocation.getText().toString();
        String description = addDescription.getText().toString();

        if (titre.isEmpty()){
            addTitle.setError("Please write a title to your Event!!");
            Toast.makeText(this, "Please write a title to your Event!!", Toast.LENGTH_SHORT).show();
            creationButton.setEnabled(true);
            return;
        }

        if(myCalendar.getTime().before(new Date()) && date.isEmpty()){
            Toast.makeText(AddEventActivity.this, "Please enter a recent day !!", Toast.LENGTH_LONG).show();
            creationButton.setEnabled(true);
            return;
        }

        if (time.isEmpty()){
            addTime.setError("Please enter a valid time !!");
            Toast.makeText(this, "Please enter a valid time !!", Toast.LENGTH_SHORT).show();
            creationButton.setEnabled(true);
            return;
        }

        if (location.isEmpty()){
            addLocation.setError("Please write a valid location !!");
            Toast.makeText(this, "Please write a valid location !!", Toast.LENGTH_SHORT).show();
            creationButton.setEnabled(true);
            return;
        }

        if (description.isEmpty()){
            addDescription.setError("Please write a valid description for your Event !!");
            creationButton.setEnabled(true);
            return;
        }

        if(imageUri == null) {
            creationButton.setEnabled(true);
            Toast.makeText(AddEventActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
            return;
        }

        if(currentPhotoPath == null){
            creationButton.setEnabled(true);
            Toast.makeText(AddEventActivity.this, "Please !! Take a photo for your event's place!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (longitude == 0 && latitude == 0){
            creationButton.setEnabled(true);
            Toast.makeText(this, "Please , select a location from map", Toast.LENGTH_SHORT).show();
            return;
        }

        //beginning the upload of event after verification.
        String ownerOfEvent = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String eventId = "Event - " + new Date().getTime();
        String eventImageId = "images/"+System.currentTimeMillis()+"."+getExtensionFile(imageUri);
        File locationImageFile = new File(currentPhotoPath);
        String locationImageId = "locationImages/"+locationImageFile.getName();
        Event event = new Event(eventId, titre, eventImageId, description, date, time, location, locationImageId, ownerOfEvent, latitude, longitude);


        processIndicator.setVisibility(View.VISIBLE);
        uploadProgress.setVisibility(View.VISIBLE);

        uploadEventImage(eventImageId, locationImageFile, locationImageId, event);

    }

    private void uploadEventImage(String eventImageId, File locationImageFile, String locationImageId, Event event){

        StorageReference imageRef = storageRef.child(eventImageId);
        uploadTask = imageRef.putFile(imageUri);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //upload the second image(location image)
                uploadProgress.setProgress(100);
                processIndicatorText.setText("The event picture is uploaded successfully.");
                uploadLocationImage(locationImageFile, locationImageId, event);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Error :"+e.toString()+", try again please", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double progress = 100 * snapshot.getBytesTransferred()/snapshot.getTotalByteCount();
                uploadProgress.setProgress((int) progress);
                processIndicatorText.setText("Uploading of event's picture ...");
            }
        });
    }

    private void uploadLocationImage(File locationImageFile, String locationImageId, Event event){
        uploadProgress.setProgress(0);
        StorageReference locationImageRef = storageRef.child(locationImageId);
        uploadTask = locationImageRef.putFile(Uri.fromFile(locationImageFile));
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //upload the event into database
                uploadProgress.setProgress(100);
                processIndicatorText.setText("The location picture is uploaded successfully.");
                uploadEventIntoDb(event);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "location image upload Error :"+e.toString()+", try again please", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double progress = 100 * snapshot.getBytesTransferred()/snapshot.getTotalByteCount();
                uploadProgress.setProgress((int) progress);
                processIndicatorText.setText("Uploading location's picture ...");
            }
        });
    }

    private void uploadEventIntoDb(Event event){

        processIndicatorText.setText("Uploading all the content to DataBase...");
        FirebaseDatabase.getInstance().getReference("events")
                .child(event.getEventId()).setValue(event).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    processIndicatorText.setText("Event uploaded");
                    uploadProgress.setVisibility(View.GONE);
                    processIndicator.setVisibility(View.GONE);
                    Snackbar.make(findViewById(R.id.space_20dp), "Event added successfully. Go back to home page to see your event...", 500).show();
                    addEventToCurrentUser(event);
                    Handler h = new Handler();
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 200);
                }
                else{
                    Toast.makeText(getApplicationContext(), "Error, try again please", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    private void addEventToCurrentUser(Event event) {
        FirebaseDatabase.getInstance().getReference("users-events")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(event.getEventId()).setValue(event).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    return;
                }
                else{
                    Toast.makeText(AddEventActivity.this, "Error, try again please", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });

    }
}
