package com.voidloop.home;


import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.nightonke.blurlockview.BlurLockView;
import com.nightonke.blurlockview.Directions.HideType;
import com.nightonke.blurlockview.Directions.ShowType;
import com.nightonke.blurlockview.Eases.EaseType;
import com.nightonke.blurlockview.Password;
import com.voidloop.home.adapter.ModuleAdapter;
import com.voidloop.home.model.Appliance;
import com.voidloop.home.model.Module;
import com.voidloop.home.model.Overview;
import com.voidloop.home.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import me.itangqi.waveloadingview.WaveLoadingView;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment implements ModuleAdapter.ModuleInterface {

    private static final int ADD_MODULE_REQUEST = 234;
    static boolean calledAlready = false;

    private DatabaseReference mRef;

    private View view, emptyListView;
    private Toolbar mToolbar;
    private FloatingActionButton mFab;
    private RecyclerView mRecyclerView;
    private TextView mTodayPowerUsageText, mTodayPowerCostText, mAggregatePowerUsageText,
            mAggregatePowerCostText;
    private WaveLoadingView mUsageProgressView;
    private BlurLockView mBlurLockView;

    private MainFragmentInterface listener;

    private ArrayList<String> mUserRegisteredIDs = new ArrayList<>();
    private ArrayList<Module> mUserModules = new ArrayList<>();
    private ArrayList<String> mUserModulesTitle = new ArrayList<>();
    private String mPasscode, firstPass;
    private boolean mLockIsShown = false;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!calledAlready) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            calledAlready = true;
        }

        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference(Constants.FIREBASE_NODE_USER);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view == null) {
            // Inflate the layout for this fragment
            view = inflater.inflate(R.layout.fragment_main, container, false);

            mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
//            toolbar.inflateMenu(R.menu.home_menu);
            mToolbar.setOverflowIcon(ActivityCompat.getDrawable(getActivity(), R.drawable.ic_more));
            ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);

//            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//                @Override
//                public boolean onMenuItemClick(MenuItem item) {
//                    switch (item.getItemId()) {
//                        case R.id.action_secure_home:
//                            Toast.makeText(getActivity(), "here", Toast.LENGTH_SHORT).show();
//                            View view = MenuItemCompat.getActionView(item);
//                            SwitchCompat securitySwitch = (SwitchCompat) view.findViewById(R.id.security_switch);
//                            securitySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//
//                                @Override
//                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                                    // do anything here on check changed
//                                    Toast.makeText(getContext(), "Security check is " + isChecked, Toast.LENGTH_SHORT).show();
//                                }
//                            });
//                            break;
//
////                        case R.id.action_new_billing_cycle:
////                            Toast.makeText(getActivity(), "There", Toast.LENGTH_SHORT).show();
////                            break;
//
//                    }
//                    return onOptionsItemSelected(item);
//                }
//            });

            mFab = (FloatingActionButton) view.findViewById(R.id.fab_add_module);
            mFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), DialogActivity.class);
                    intent.putExtra(Constants.KEY_DIALOG_TYPE, Constants.KEY_MODULE);
                    intent.putStringArrayListExtra(Constants.FIREBASE_NODE_MODULES, mUserModulesTitle);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity(),
                                mFab, getString(R.string.transition_dialog));
                        startActivityForResult(intent, ADD_MODULE_REQUEST, options.toBundle());
                    } else {
                        startActivityForResult(intent, ADD_MODULE_REQUEST);
                    }
                }
            });

            mBlurLockView = (BlurLockView) view.findViewById(R.id.blurlockview);
            mBlurLockView.setBackgroundColor(ActivityCompat.getColor(getContext(), R.color.colorPrimary));
            mBlurLockView.setLeftButton("");
            mBlurLockView.setRightButton("Backspace");
            mBlurLockView.setType(Password.NUMBER, false);

            SharedPreferences prefs = getActivity().getSharedPreferences(
                    getActivity().getPackageName(), Context.MODE_PRIVATE);
            boolean firstUse = prefs.getBoolean(Constants.PREF_FIRST_USE, true);
            if (firstUse) {
                mFab.setVisibility(View.GONE);
                mToolbar.setVisibility(View.GONE);
                mBlurLockView.setTitle("Enter new Pass code");
                mBlurLockView.setCorrectPassword("0q20");
                mBlurLockView.show(500, ShowType.FADE_IN, EaseType.EaseInExpo);

                mBlurLockView.setOnPasswordInputListener(new BlurLockView.OnPasswordInputListener() {
                    @Override
                    public void correct(String inputPassword) {
                        mFab.setVisibility(View.VISIBLE);
                        mToolbar.setVisibility(View.VISIBLE);
                        mPasscode = inputPassword;
                        Overview overview = new Overview();
                        overview.setSecurityPassword(mPasscode);
                        overview.setUserInstanceTokenID(FirebaseInstanceId.getInstance().getToken());
                        mRef.setValue(overview,
                                new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        if (databaseError == null) {
                                            getActivity().getSharedPreferences(getActivity().getPackageName(),
                                                    Context.MODE_PRIVATE).edit().putBoolean(Constants.PREF_FIRST_USE, false)
                                                    .apply();
                                            Toast.makeText(getActivity(), "Pass code is " + mPasscode, Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getContext(), "" + databaseError.getDetails(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                        mBlurLockView.hide(500, HideType.FADE_OUT, EaseType.EaseOutExpo);
                    }

                    @Override
                    public void incorrect(String inputPassword) {
                        if (firstPass == null) {
                            if (inputPassword.length() == 4) {
                                firstPass = inputPassword;
                                mBlurLockView.setCorrectPassword(firstPass);
                                mBlurLockView.setTitle("Confirm Pass code");
                            }
                        } else {
                            Toast.makeText(getActivity(), "Pass codes do not match, start again.", Toast.LENGTH_SHORT).show();
                            mBlurLockView.setTitle("Enter new Pass code");
                            mBlurLockView.setCorrectPassword("0q20");
                            firstPass = null;
                        }
                    }

                    @Override
                    public void input(String inputPassword) {

                    }
                });
            }

            getActivity().getSharedPreferences(getActivity().getPackageName(),
                    Context.MODE_PRIVATE).edit().putBoolean(Constants.PREF_FIRST_USE, true)
                    .apply();

            emptyListView = view.findViewById(R.id.empty_feeds_view);

            mTodayPowerUsageText = (TextView) view.findViewById(R.id.today_power_usage);
            mTodayPowerCostText = (TextView) view.findViewById(R.id.today_power_cost);
            mAggregatePowerUsageText = (TextView) view.findViewById(R.id.aggregate_power_usage);
            mAggregatePowerCostText = (TextView) view.findViewById(R.id.aggregate_power_cost);

            mUsageProgressView = (WaveLoadingView) view.findViewById(R.id.usage_progress_view);

            mRecyclerView = (RecyclerView) view.findViewById(R.id.modules_recycler_view);
            mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setNestedScrollingEnabled(false);

            // Read from the database
            mRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    Overview usageOverview = dataSnapshot.getValue(Overview.class);

                    mUserModules = new ArrayList<>();
                    mUserRegisteredIDs = new ArrayList<>();

                    for (DataSnapshot snap : dataSnapshot.child(Constants.FIREBASE_NODE_REGISTERED_IDS).getChildren()) {
                        mUserRegisteredIDs.add(snap.getKey());
                    }

                    List<Module> modules = new ArrayList<>();
                    for (DataSnapshot child : dataSnapshot.child(Constants.FIREBASE_NODE_MODULES).getChildren()) {
                        modules.add(child.getValue(Module.class));
                    }

                    for (Module module : modules) {

                        mUserModulesTitle.add(module.getTitle());

                        DataSnapshot appliances = dataSnapshot.child(Constants.FIREBASE_NODE_MODULES)
                                .child(module.getTitle()).child(Constants.FIREBASE_NODE_APPLIANCES);

                        module.setChildrenCount(appliances.getChildrenCount());

                        for (DataSnapshot snapshot : appliances.getChildren()) {
                            Appliance appliance = snapshot.getValue(Appliance.class);
                            module.addAppliances(appliance);
                        }

                        mUserModules.add(module);
                    }

                    if (mRecyclerView.getAdapter() == null) {
                        ModuleAdapter adapter = new ModuleAdapter(mUserModules, MainFragment.this, dataSnapshot);
                        mRecyclerView.setAdapter(adapter);
                    } else {
                        //Refill adapter
                        ((ModuleAdapter) mRecyclerView.getAdapter()).refill(mUserModules, dataSnapshot);
                    }

                    if (mRecyclerView.getAdapter().getItemCount() > 0) {
                        emptyListView.setVisibility(View.GONE);
                    } else {
                        emptyListView.setVisibility(View.VISIBLE);
                    }

                    updateUI(usageOverview);

                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Toast.makeText(getContext(), "Failed to read value.", Toast.LENGTH_SHORT).show();
                }
            });

        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.home_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        mFab.setVisibility(View.GONE);
        mToolbar.setVisibility(View.GONE);
        mBlurLockView.setLeftButton("Cancel");
        mBlurLockView.setOnLeftButtonClickListener(new BlurLockView.OnLeftButtonClickListener() {
            @Override
            public void onClick() {
                mBlurLockView.hide(500, HideType.FROM_BOTTOM_TO_TOP, EaseType.EaseOutBack);
                mLockIsShown = false;
                mFab.setVisibility(View.VISIBLE);
                mToolbar.setVisibility(View.VISIBLE);
            }
        });
        mBlurLockView.show(300, ShowType.FROM_TOP_TO_BOTTOM, EaseType.EaseInSine);

        switch (item.getItemId()) {
            case R.id.action_new_billing_cycle:
                mLockIsShown = true;
                if (mPasscode != null && mPasscode.trim().length() == 4) {
                    mBlurLockView.setCorrectPassword(mPasscode);
                    mBlurLockView.setOnPasswordInputListener(new BlurLockView.OnPasswordInputListener() {
                        @Override
                        public void correct(String inputPassword) {
                            mBlurLockView.hide(1000, HideType.FROM_BOTTOM_TO_TOP, EaseType.EaseOutBack);
                            Toast.makeText(getActivity(), "Corrected", Toast.LENGTH_SHORT).show();
                            mLockIsShown = false;
                            mFab.setVisibility(View.VISIBLE);
                            mToolbar.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void incorrect(String inputPassword) {
                            Toast.makeText(getActivity(), "The Pass code is incorrect", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void input(String inputPassword) {

                        }
                    });
                }
                break;

            case R.id.action_secure_home:
                mLockIsShown = true;
                if (mPasscode != null && mPasscode.trim().length() == 4) {
                    mBlurLockView.setCorrectPassword(mPasscode);
                    mBlurLockView.setOnPasswordInputListener(new BlurLockView.OnPasswordInputListener() {
                        @Override
                        public void correct(String inputPassword) {
                            mBlurLockView.hide(1000, HideType.FROM_BOTTOM_TO_TOP, EaseType.EaseOutSine);
                            item.setChecked(!item.isChecked());
                            mRef.child("isHomeSecured").setValue(item.isChecked(),
                                    new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                            if (databaseError == null) {
                                                Toast.makeText(getActivity(), "Done", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(getContext(), "" + databaseError.getDetails(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                            Toast.makeText(getActivity(), "Correct", Toast.LENGTH_SHORT).show();
                            mLockIsShown = false;
                            mFab.setVisibility(View.VISIBLE);
                            mToolbar.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void incorrect(String inputPassword) {
                            Toast.makeText(getActivity(), "The Pass code is incorrect", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void input(String inputPassword) {

                        }
                    });
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (MainFragmentInterface) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        listener = null;
    }

    private void saveNewModule(String moduleTitle) {
        Module module = new Module();
        module.setTitle(moduleTitle);
        mRef.child(Constants.FIREBASE_NODE_MODULES).child(moduleTitle).setValue(module,
                new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            Toast.makeText(getActivity(), "Done", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "" + databaseError.getDetails(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateUI(Overview overview) {
        overview.setTodayPowerUsageCost();
        overview.setAggregatePowerUsageCost();

        if (overview.getSecurityPassword() != null) {
            mPasscode = overview.getSecurityPassword();
        }

        Toast.makeText(getActivity(), mPasscode, Toast.LENGTH_SHORT).show();

        mTodayPowerUsageText.setText(String.format(Locale.getDefault(), "%s %s",
                overview.getTodayPowerUsage(), "Kwh"));

        mTodayPowerCostText.setText(String.format(Locale.getDefault(), "%s %s",
                getActivity().getResources().getString(R.string.naira_sign), overview.getTodayPowerUsageCost()));

        mAggregatePowerUsageText.setText(String.format(Locale.getDefault(), "%s %s",
                overview.getAggregatePowerUsage(), "Kwh"));

        mAggregatePowerCostText.setText(String.format(Locale.getDefault(), "%s %s",
                getActivity().getResources().getString(R.string.naira_sign), overview.getAggregatePowerUsageCost()));

        int remainderPower = (int) overview.getTotalReferencePower() - (int) overview.getAggregatePowerUsage();

        int remainderPercentage = 100 - (int) ((remainderPower / overview.getTotalReferencePower()) * 100);

        mUsageProgressView.setCenterTitle(
                String.format(Locale.getDefault(), "%s %s",
                        remainderPercentage, "%"));

        mUsageProgressView.setProgressValue(remainderPercentage);

        if (remainderPercentage > 66) {
            mUsageProgressView.setWaveColor(ContextCompat.getColor(getActivity(), R.color.green));
            mUsageProgressView.setBorderColor(ContextCompat.getColor(getActivity(), R.color.green));
        } else if (remainderPercentage > 33) {
            mUsageProgressView.setWaveColor(ContextCompat.getColor(getActivity(), R.color.yellow));
            mUsageProgressView.setBorderColor(ContextCompat.getColor(getActivity(), R.color.yellow));
        } else {
            mUsageProgressView.setWaveColor(ContextCompat.getColor(getActivity(), R.color.red));
            mUsageProgressView.setBorderColor(ContextCompat.getColor(getActivity(), R.color.red));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case ADD_MODULE_REQUEST:
                    saveNewModule(data.getStringExtra(DialogActivity.RESPONSE_CODE_ARG));
                    break;
            }
        }
    }

    @Override
    public void onModuleClicked(CardView container, Module module, int index) {
        listener.onModuleClicked(container, module, index, mUserRegisteredIDs);
    }

    @Override
    public void onDeleteModule(final Module module) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Delete Module")
                .setMessage("Do you want to permanently delete this " + module.getTitle() + " module?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mRef.child(Constants.FIREBASE_NODE_MODULES).child(module.getTitle())
                                .removeValue();
                    }
                })
                .setNegativeButton("No", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
