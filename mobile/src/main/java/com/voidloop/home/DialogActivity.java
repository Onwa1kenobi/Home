package com.voidloop.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.transition.ArcMotion;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.voidloop.home.util.Constants;
import com.voidloop.home.util.MorphDialogToFab;
import com.voidloop.home.util.MorphFabToDialog;
import com.voidloop.home.util.MorphTransition;

import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class DialogActivity extends AppCompatActivity {

    public final static String RESPONSE_CODE_ARG = "response_code_arg";

    public final static String RESPONSE_CODE_APPLIANCE_ID = "appliance_id";
    public final static String RESPONSE_CODE_APPLIANCE_TAG = "appliance_tag";
    public final static String RESPONSE_CODE_APPLIANCE_TITLE = "appliance_title";

    private ViewGroup container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String dialogType = getIntent().getStringExtra(Constants.KEY_DIALOG_TYPE);

        if (dialogType.equals(Constants.KEY_MODULE)) {

            setContentView(R.layout.activity_dialog);

            container = (ViewGroup) findViewById(R.id.content);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setupSharedElementTransitions2();
            }

            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.add("Living Room");
            arrayList.add("Bedroom");
            arrayList.add("Kitchen");

            ArrayList<String> userRegisteredModules = getIntent()
                    .getStringArrayListExtra(Constants.FIREBASE_NODE_MODULES);

            for (String module : userRegisteredModules) {
                arrayList.remove(module);
            }

            final Spinner spinner = (Spinner) container.findViewById(R.id.spinner);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_dropdown_item, arrayList);
            spinner.setAdapter(adapter);

            View.OnClickListener dismissListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            };


            View.OnClickListener addListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (spinner.getSelectedItem() != null) {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(RESPONSE_CODE_ARG, spinner.getSelectedItem().toString());

                        setResult(RESULT_OK, resultIntent);
                    } else {
                        dismiss();
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        finishAfterTransition();
                    } else {
                        finish();
                    }
                }
            };

            findViewById(R.id.container).setOnClickListener(dismissListener);
            container.findViewById(R.id.close).setOnClickListener(dismissListener);
            container.findViewById(R.id.add).setOnClickListener(addListener);
        } else if (dialogType.equals(Constants.KEY_APPLIANCE)) {

            setContentView(R.layout.activity_add_appliance);

            container = (ViewGroup) findViewById(R.id.content);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setupSharedElementTransitions2();
            }

            ArrayList<String> idArray = new ArrayList<>();
            idArray.add("home_id_1");
            idArray.add("home_id_2");
            idArray.add("home_id_3");

            ArrayList<String> userRegisteredIds = getIntent()
                    .getStringArrayListExtra(Constants.FIREBASE_NODE_REGISTERED_IDS);

            for (String id : userRegisteredIds) {
                idArray.remove(id);
            }

            final Spinner idSpinner = (Spinner) container.findViewById(R.id.appliance_id_spinner);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_dropdown_item, idArray);
            idSpinner.setAdapter(adapter);

            ArrayList<String> tagArray = new ArrayList<>();
            tagArray.add("Light");
            tagArray.add("Air Conditioner");
            tagArray.add("Fridge");

            final Spinner tagSpinner = (Spinner) container.findViewById(R.id.appliance_tag_spinner);
            ArrayAdapter<String> tagAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_dropdown_item, tagArray);
            tagSpinner.setAdapter(tagAdapter);

            final TextInputLayout textInputLayout = (TextInputLayout) findViewById(R.id.appliance_title_wrapper);
            final EditText applianceTitle = (EditText) findViewById(R.id.appliance_title);

            View.OnClickListener dismissListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            };


            View.OnClickListener addListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (idSpinner.getSelectedItem() == null) {
                        dismiss();
                        return;
                    }

                    if (applianceTitle.getText().toString().trim().length() < 2) {
                        textInputLayout.setErrorEnabled(true);
                        textInputLayout.setError("Please enter a valid title");
                        return;
                    }

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(RESPONSE_CODE_APPLIANCE_ID, idSpinner.getSelectedItem().toString());
                    resultIntent.putExtra(RESPONSE_CODE_APPLIANCE_TAG, tagSpinner.getSelectedItem().toString());
                    resultIntent.putExtra(RESPONSE_CODE_APPLIANCE_TITLE, applianceTitle.getText().toString());

                    setResult(RESULT_OK, resultIntent);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        finishAfterTransition();
                    } else {
                        finish();
                    }
                }
            };

            findViewById(R.id.container).setOnClickListener(dismissListener);
            container.findViewById(R.id.close).setOnClickListener(dismissListener);
            container.findViewById(R.id.add).setOnClickListener(addListener);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setupSharedElementTransitions1() {
        ArcMotion arcMotion = new ArcMotion();
        arcMotion.setMinimumHorizontalAngle(50f);
        arcMotion.setMinimumVerticalAngle(50f);

        Interpolator easeInOut = AnimationUtils.loadInterpolator(this, android.R.interpolator.fast_out_slow_in);

        MorphFabToDialog sharedEnter = new MorphFabToDialog();
        sharedEnter.setPathMotion(arcMotion);
        sharedEnter.setInterpolator(easeInOut);

        MorphDialogToFab sharedReturn = new MorphDialogToFab();
        sharedReturn.setPathMotion(arcMotion);
        sharedReturn.setInterpolator(easeInOut);

        if (container != null) {
            sharedEnter.addTarget(container);
            sharedReturn.addTarget(container);
        }
        getWindow().setSharedElementEnterTransition(sharedEnter);
        getWindow().setSharedElementReturnTransition(sharedReturn);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setupSharedElementTransitions2() {
        ArcMotion arcMotion = new ArcMotion();
        arcMotion.setMinimumHorizontalAngle(50f);
        arcMotion.setMinimumVerticalAngle(50f);

        Interpolator easeInOut = AnimationUtils.loadInterpolator(this, android.R.interpolator.fast_out_slow_in);

        //hujiawei 100是随意给的一个数字，可以修改，需要注意的是这里调用container.getHeight()结果为0
        MorphTransition sharedEnter = new MorphTransition(ContextCompat.getColor(this, R.color.colorAccent),
                ContextCompat.getColor(this, R.color.dialog_background_color), 100,
                getResources().getDimensionPixelSize(R.dimen.dialog_border), true);
        sharedEnter.setPathMotion(arcMotion);
        sharedEnter.setInterpolator(easeInOut);

        MorphTransition sharedReturn = new MorphTransition(ContextCompat.getColor(this, R.color.dialog_background_color),
                ContextCompat.getColor(this, R.color.colorAccent), getResources().getDimensionPixelSize(R.dimen.dialog_border), 100, false);
        sharedReturn.setPathMotion(arcMotion);
        sharedReturn.setInterpolator(easeInOut);

        if (container != null) {
            sharedEnter.addTarget(container);
            sharedReturn.addTarget(container);
        }
        getWindow().setSharedElementEnterTransition(sharedEnter);
        getWindow().setSharedElementReturnTransition(sharedReturn);
    }

    @Override
    public void onBackPressed() {
        dismiss();
    }

    public void dismiss() {
        setResult(Activity.RESULT_CANCELED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAfterTransition();
        } else {
            finish();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
