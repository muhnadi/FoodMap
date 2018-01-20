package com.nerdgeeks.foodmap.model;

import java.io.Serializable;

/**
 * Created by hp on 11/24/2016.
 */

public class PhotoModel implements Serializable{
    private String photoReference;
    private String photoUrl;

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getPhotoReference() {
        return photoReference;
    }

    public void setPhotoReference(String photoReference) {
        this.photoReference = photoReference;
        setPhotoUrl("https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference="
                + photoReference
                +"&key="
                + "AIzaSyAbNDLy8J2oefyHeY-47pFrtU8EQl1Q04g");
    }
}
