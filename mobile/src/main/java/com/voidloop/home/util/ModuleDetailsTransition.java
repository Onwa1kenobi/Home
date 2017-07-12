package com.voidloop.home.util;

import android.annotation.TargetApi;
import android.os.Build;
import android.transition.ChangeBounds;
import android.transition.ChangeClipBounds;
import android.transition.ChangeTransform;
import android.transition.TransitionSet;

/**
 * Created by Jules on 5/24/2016.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class ModuleDetailsTransition extends TransitionSet {
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ModuleDetailsTransition() {
        setOrdering(ORDERING_TOGETHER);
        addTransition(new ChangeBounds().setDuration(300))
                .addTransition(new ChangeTransform());
//                .addTransition(new ChangeImageTransform());
    }
}
