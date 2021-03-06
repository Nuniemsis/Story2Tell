package com.example.ncarvalho.story2tell;


import java.util.ArrayList;

/**
 * Created by ncarvalho on 5/01/18.
 */

public class Message {

    public String username;
    public String message;
    public String date;
    public String photoUrl;
    public float rating;
    public String pushKey;
    public int numberRatings;
    public ArrayList<String> raters = new ArrayList<String>();

    public float getRating(){
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public ArrayList<String> getRaters() {
        return raters;
    }

    public int getNumberRatings() {
        return numberRatings;
    }


    public Message() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public void addRater(String rater) {
        raters.add(rater);
    }

    public String getPhotoUrl(){
        return photoUrl;
    }

    public String getPushKey(){
        return pushKey;
    }


    public Message(String username, String message, String date, String photoUrl, String pushKey) {
        this.username = username;
        this.message = message;
        this.date = date;
        this.photoUrl  = photoUrl;
        this.pushKey = pushKey;

    }


    public String getName() {

        return username;
    }
    public String getDate() {

        return date;
    }
    public String getMessage() {
        return message;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}