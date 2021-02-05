package com.vishalperipherals.maps_demo.models;


public class Fhandler {

    String image;
    String image_name;

    public Fhandler() {
    }

    public Fhandler(String image, String image_name) {
        this.image = image;
        this.image_name = image_name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage_name() {
        return image_name;
    }

    public void setImage_name(String image_name) {
        this.image_name = image_name;
    }
}