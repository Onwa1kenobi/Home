package com.voidloop.home;

import android.content.Context;
import android.os.Build;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.transition.Fade;

import com.nightonke.blurlockview.Directions.HideType;
import com.nightonke.blurlockview.Eases.EaseType;
import com.voidloop.home.model.Module;
import com.voidloop.home.util.Constants;
import com.voidloop.home.util.ModuleDetailsTransition;

import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity implements MainFragmentInterface {

    public final String MAIN_FRAGMENT = "main_fragment";
    public final String MODULE_FRAGMENT = "module_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager().beginTransaction().replace(R.id.content_holder, new MainFragment(),
                MAIN_FRAGMENT)
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).commit();

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onModuleClicked(CardView container, Module module, int index,
                                ArrayList<String> userRegisteredIDs) {
        ModuleFragment moduleFragment = new ModuleFragment();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            MainFragment mainFragment = (MainFragment) getSupportFragmentManager()
                    .findFragmentByTag(MAIN_FRAGMENT);
            if (mainFragment != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mainFragment.setExitTransition(new Fade());
                    moduleFragment.setExitTransition(new Fade());
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    mainFragment.setExitTransition(new Fade());
                    moduleFragment.setExitTransition(new Fade());
                }
            }

            ModuleDetailsTransition moduleDetailsTransition = new ModuleDetailsTransition();

            moduleFragment.setSharedElementEnterTransition(moduleDetailsTransition);
            moduleFragment.setSharedElementReturnTransition(moduleDetailsTransition);
        }

        ViewCompat.setTransitionName(container, "container_" + index);

        Bundle bundle = new Bundle();
        bundle.putString(Constants.TRANSITION_NAME, "container_" + index);
        bundle.putString(Constants.KEY_MODULE, module.getTitle());
        bundle.putStringArrayList(Constants.FIREBASE_NODE_REGISTERED_IDS, userRegisteredIDs);
//        bundle.putString(Constants.KEY_URI, String.valueOf(postImageUri));
//        bundle.putString(Constants.KEY_CREATED_AT, postCreatedAt);
//        bundle.putString(Constants.KEY_POST_TEXT, postText);
//        bundle.putString(Constants.KEY_USER_PROFILE_IMAGE, senderImageUri);
//        bundle.putString(Constants.KEY_USER_FULL_NAME, senderName);
//
        moduleFragment.setArguments(bundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                    android.R.anim.fade_in, android.R.anim.fade_out);
        }
        fragmentTransaction.addSharedElement(container, "container_" + index);
        fragmentTransaction.replace(R.id.content_holder, moduleFragment, MODULE_FRAGMENT);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

}
