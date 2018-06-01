package com.voidloop.home.model;

/**
 * Created by ameh on 16/06/2017.
 */

public class Appliance {

    private String title, id, tag;
    private boolean isOn;

    public Appliance() {

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean getIsOn() {
        return isOn;
    }

    public void setIsOn(boolean on) {
        isOn = on;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
