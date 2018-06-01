package com.voidloop.home;

import android.support.v7.widget.CardView;

import com.voidloop.home.model.Module;

import java.util.ArrayList;

/**
 * Created by ameh on 16/06/2017.
 */

public interface MainFragmentInterface {

    void onModuleClicked(CardView container, Module module, int index, ArrayList<String> mUserRegisteredIDs);

}
