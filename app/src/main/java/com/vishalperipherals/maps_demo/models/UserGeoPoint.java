package com.vishalperipherals.maps_demo.models;

public class UserGeoPoint {

    String lat;
    String lng;
    String id;

    public UserGeoPoint(String lat, String lng, String id) {
        this.lat = lat;
        this.lng = lng;
        this.id = id;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
