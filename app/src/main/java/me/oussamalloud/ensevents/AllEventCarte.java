package me.oussamalloud.ensevents;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.internal.location.zzz;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import me.oussamalloud.ensevents.databinding.ActivityMapsBinding;

public class AllEventCarte extends Fragment {

    public static final String EVENT_ID = "EVENT_ID";
    private ImageButton next_btn, previous_btn, myLocation;
    private int count;
    private GoogleMap mMap;
    private boolean mLocationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private List<Event> eventList;
    // A default location (Rabat) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(33.976466665212726, -6.852577432263147);
    private static final int DEFAULT_ZOOM = 15;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //initialize view
        View view = inflater.inflate(R.layout.all_events_maps, container, false);
        count = -1;
        myLocation = view.findViewById(R.id.my_location_btn);
        next_btn = view.findViewById(R.id.next_btn);
        previous_btn = view.findViewById(R.id.previous_btn);

        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        eventList = new ArrayList<>();

        //initialize the buttons

        if (count <= 0){
            previous_btn.setEnabled(false);
            previous_btn.setAlpha(0.5F);
        }





        // Initialize the Places client
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        //get current location
        myLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocationPermission();
                pickCurrentPlace();
            }
        });

        //initialize map fragment

        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.maps_fragment);
        //SupportMapFragment supportMapFragment = SupportMapFragment.newInstance();
        //async

        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                mMap = googleMap;
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.clear();

                // load event positions from Firebase
                FirebaseDatabase.getInstance().getReference("events")
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                for (DataSnapshot data : snapshot.getChildren()){
                                    Event e = data.getValue(Event.class);

                                    if (!eventList.contains(e)){
                                        eventList.add(e);
                                    }

                                }
                                //add all markers of events into the map
                                for (Event e : eventList){
                                    mMap.addMarker(new MarkerOptions().position(new LatLng(e.getEventLocationLatitude(), e.getEventLocationLongitude())).title(e.getEventTitle())).setTag(e);
                                }
                                //focus the camera to the last event added
                                LatLng lastEventPosition = new LatLng(eventList.get(eventList.size()-1).getEventLocationLatitude(), eventList.get(eventList.size()-1).getEventLocationLongitude());
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastEventPosition, 10));

                                //next place & previous place

                                next_btn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        count++;
                                        if (count >= eventList.size()-1){
                                            next_btn.setEnabled(false);
                                            next_btn.setAlpha(0.5F);
                                        }

                                        if (count > 0){
                                            previous_btn.setEnabled(true);
                                            previous_btn.setAlpha(1F);
                                        }

                                        LatLng nextPlace = new LatLng(eventList.get(count).getEventLocationLatitude(), eventList.get(count).getEventLocationLongitude());
                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(nextPlace, 15));
                                    }
                                });
                                previous_btn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        count--;
                                        if (count <= 0){
                                            previous_btn.setEnabled(false);
                                            previous_btn.setAlpha(0.5F);
                                        }
                                        if (count <= eventList.size()-1){
                                            next_btn.setEnabled(true);
                                            next_btn.setAlpha(1F);
                                        }
                                        LatLng nextPlace = new LatLng(eventList.get(count).getEventLocationLatitude(), eventList.get(count).getEventLocationLongitude());
                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(nextPlace, 15));

                                    }
                                });

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(view.getContext(), "Err : " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(@NonNull Marker marker) {
                        if(marker.getTitle().equals("Your location")){
                            return;
                        }

                        Event event = (Event) marker.getTag();
                        Intent intent = new Intent(getContext(), EventActivity.class);
                        intent.putExtra(EVENT_ID, event.getEventId());
                        startActivity(intent);
                    }
                });
            }
        });



    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        mLocationPermissionGranted = false;
        if (ContextCompat.checkSelfPermission(this.getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {


                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            //Toast.makeText(MapsActivity.this, "Latitude: " + mLastKnownLocation.getLatitude(), Toast.LENGTH_SHORT).show();
                            //Toast.makeText(MapsActivity.this, "Longitude: " + mLastKnownLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                            mMap.addMarker(new MarkerOptions().title("Your location").position(new LatLng(mLastKnownLocation.getLatitude(),
                                    mLastKnownLocation.getLongitude()))).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), 12));
                        } else {
                            Toast.makeText(getContext(), "Current location is null. Using defaults.", Toast.LENGTH_SHORT).show();
                            mMap.animateCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                        }

                        //getCurrentPlaceLikelihoods();
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
            Toast.makeText(getContext(), "Exception: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void pickCurrentPlace() {
        if (mMap == null) {
            return;
        }

        if (mLocationPermissionGranted) {
            getDeviceLocation();
        } else {
            // The user has not granted permission.
            Toast.makeText(getContext(), "You did not grant location permission.", Toast.LENGTH_SHORT).show();

            // focus on the default location, because the user hasn't selected a place.

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, 11));

            // Prompt the user for permission.
            getLocationPermission();
        }
    }

}
