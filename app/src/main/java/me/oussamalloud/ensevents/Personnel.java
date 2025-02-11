package me.oussamalloud.ensevents;

import java.util.ArrayList;
import java.util.List;

public class Personnel {
    private String fullName;
    private String email;
    private String type;
    public final static String[] TYPES = {"Student", "Professor", "Administrative"};
//    private List<String> myEvents;
//    private List<String> interestedEvents;

    public Personnel(){}

    public Personnel(String fullName, String email, String type) {
        this.fullName = fullName;
        this.email = email;
        this.type = type;

    }

//    public Personnel(String fullName, String email, String type, List<String> myEvents){
//        this(fullName, email, type);
//        this.myEvents = myEvents;
//    }
//    public Personnel(String fullName, String email, String type, List<String> myEvents, List<String> interestedEvents){
//        this(fullName, email, type, myEvents);
//        this.interestedEvents = interestedEvents;
//    }

    //getters

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getType() {
        return type;
    }

//    public List<String> getMyEvents() {
//        return myEvents;
//    }
//
//    public List<String> getInterestedEvents() {
//        return interestedEvents;
//    }


}
