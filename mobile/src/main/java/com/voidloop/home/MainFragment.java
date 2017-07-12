package com.voidloop.home;


import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
    private RecyclerView mRecyclerView;
    private TextView mTodayPowerUsageText, mTodayPowerCostText, mAggregatePowerUsageText,
            mAggregatePowerCostText;
    private WaveLoadingView mUsageProgressView;

    private MainFragmentInterface listener;

    private ArrayList<String> mUserRegisteredIDs = new ArrayList<>();
    private ArrayList<Module> mUserModules = new ArrayList<>();
    private ArrayList<String> mUserModulesTitle = new ArrayList<>();

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view == null) {
            // Inflate the layout for this fragment
            view = inflater.inflate(R.layout.fragment_main, container, false);

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

            final FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab_add_module);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), DialogActivity.class);
                    intent.putExtra(Constants.KEY_DIALOG_TYPE, Constants.KEY_MODULE);
                    intent.putStringArrayListExtra(Constants.FIREBASE_NODE_MODULES, mUserModulesTitle);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity(),
                                fab, getString(R.string.transition_dialog));
                        startActivityForResult(intent, ADD_MODULE_REQUEST, options.toBundle());
                    } else {
                        startActivityForResult(intent, ADD_MODULE_REQUEST);
                    }
                }
            });
        }

        return view;
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
