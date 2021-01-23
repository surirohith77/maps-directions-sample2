package com.vishalperipherals.maps_demo.models;

public class Customer_LatLng {

    Double Lat;
    Double Lng;

    public Customer_LatLng() {
    }

    public Customer_LatLng(Double lat, Double lng) {
        Lat = lat;
        Lng = lng;
    }


    public Double getLat() {
        return Lat;
    }

    public void setLat(Double lat) {
        Lat = lat;
    }

    public Double getLng() {
        return Lng;
    }

    public void setLng(Double lng) {
        Lng = lng;
    }
}
