package com.voidloop.home.model;

import com.voidloop.home.app.App;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ameh on 15/06/2017.
 */

public class Module {

    private String title;
    private long childrenCount;
    private List<Appliance> userAppliances = new ArrayList<>();

    public Module() {

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getChildrenCount() {
        return childrenCount;
    }

    public void setChildrenCount(long childrenCount) {
        this.childrenCount = childrenCount;
    }

    public List<Appliance> getAppliances() {
        return userAppliances;
    }

    public void addAppliances(Appliance appliance) {
        this.userAppliances.add(appliance);
    }
}
