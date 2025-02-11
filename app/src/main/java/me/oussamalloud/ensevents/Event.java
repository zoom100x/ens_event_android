package me.oussamalloud.ensevents;

import android.app.DownloadManager;
import android.graphics.drawable.Drawable;
import android.service.autofill.FillEventHistory;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.util.Date;
import java.util.UUID;

public class Event {
    private String eventId;
    private String eventTitle;
    private String eventDescription;
    private String eventDate;
    private String EventTime;
    private String eventPicture;
    private String eventLocation;
    private double eventLocationLongitude;
    private double eventLocationLatitude;
    private String ownerOfEvent;
    private String locationImage;


///constructor
    public Event(){}





    public Event(String eventId, String eventTitle, String eventPicture, String eventDescription, String eventDate, String eventTime, String eventLocation, String locationImage, String ownerOfEvent, double eventLocationLatitude, double eventLocationLongitude) {
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.eventPicture = eventPicture;
        this.eventDescription = eventDescription;
        this.eventDate = eventDate;
        this.EventTime = eventTime;
        this.eventLocation = eventLocation;
        this.ownerOfEvent = ownerOfEvent;
        this.locationImage = locationImage;
        this.eventLocationLatitude = eventLocationLatitude;
        this.eventLocationLongitude = eventLocationLongitude;

    }

    ///getters and setters
    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventTime() {
        return EventTime;
    }

    public void setEventTime(String eventTime) {
        EventTime = eventTime;
    }

    public String getEventPicture() {
        return eventPicture;
    }

    public void setEventPicture(String eventPicture) {
        this.eventPicture = eventPicture;
    }

    public String getEventLocation(){
        return eventLocation;
    }

    public void setEventLocation(String eventLocation){
        this.eventLocation = eventLocation;
    }

    public String getOwnerOfEvent() {
        return ownerOfEvent;
    }
    public String getEventId(){
        return eventId;
    }

    public String getLocationImage() {
        return locationImage;
    }

    public double getEventLocationLongitude() {
        return eventLocationLongitude;
    }

    public double getEventLocationLatitude() {
        return eventLocationLatitude;
    }
}
