package com.nerdgeeks.foodmap.model;

import java.io.Serializable;

/**
 * Created by hp on 11/20/2016.
 */

public class PlaceDeatilsModel implements Serializable{
    private String resName;
    private String resVicnity;
    private String resRating;
    private String resOpen;
    private String id;
    private String iconUrl;
    private double latitude;
    private double longitude;
    private String ref;
    private String thumbUrl;

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
        setThumbUrl("https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference="
                + ref
                +"&key="
                + "AIzaSyAbNDLy8J2oefyHeY-47pFrtU8EQl1Q04g");
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getResName() {
        return resName;
    }

    public void setResName(String resName) {
        this.resName = resName;
    }

    public String getResVicnity() {
        return resVicnity;
    }

    public void setResVicnity(String resVicnity) {
        this.resVicnity = resVicnity;
    }

    public String getResRating() {
        return resRating;
    }

    public void setResRating(String resRating) {
        this.resRating = resRating;
    }

    public String getResOpen() {
        return resOpen;
    }

    public void setResOpen(String resOpen) {
        this.resOpen = resOpen;
    }

}
