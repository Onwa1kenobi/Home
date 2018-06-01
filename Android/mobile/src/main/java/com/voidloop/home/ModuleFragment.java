package com.voidloop.home;


import android.app.Activity;
import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.voidloop.home.adapter.ApplianceAdapter;
import com.voidloop.home.model.Appliance;
import com.voidloop.home.util.Constants;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class ModuleFragment extends Fragment implements ApplianceAdapter.ApplianceInterface {

    private static final int ADD_APPLIANCE_REQUEST = 345;

    private DatabaseReference mRef;
    private DatabaseReference mAppliancesRef;

    private View view, emptyListView;
    private RecyclerView mRecyclerView;

    private ArrayList<Appliance> mModuleAppliances = new ArrayList<>();
    private ArrayList<String> mUserRegisteredIDs = new ArrayList<>();

    private String moduleNode = "";

    public ModuleFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference(Constants.FIREBASE_NODE_USER);

        moduleNode = getArguments().getString(Constants.KEY_MODULE);

        assert moduleNode != null;
        mAppliancesRef = mRef.child(Constants.FIREBASE_NODE_MODULES).child(moduleNode)
                .child(Constants.FIREBASE_NODE_APPLIANCES);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view == null) {
            // Inflate the layout for this fragment
            view = inflater.inflate(R.layout.fragment_module, container, false);

            emptyListView = view.findViewById(R.id.empty_feeds_view);

            CardView holder = (CardView) view.findViewById(R.id.container_holder);

            final Bundle bundle = getArguments();

            Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
            toolbar.setTitle(bundle.getString(Constants.KEY_MODULE));
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().onBackPressed();
                }
            });

            mUserRegisteredIDs = bundle.getStringArrayList(Constants.FIREBASE_NODE_REGISTERED_IDS);

            String transitionName = bundle.getString(Constants.TRANSITION_NAME);
            ViewCompat.setTransitionName(holder, transitionName);

            mRecyclerView = (RecyclerView) view.findViewById(R.id.appliances_recycler_view);
            mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setNestedScrollingEnabled(false);

            // Read from the database
            mRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.

                    mModuleAppliances = new ArrayList<>();

                    for (DataSnapshot child : dataSnapshot.child(Constants.FIREBASE_NODE_MODULES)
                            .child(moduleNode)
                            .child(Constants.FIREBASE_NODE_APPLIANCES).getChildren()) {
                        Appliance appliance = child.getValue(Appliance.class);
                        appliance.setIsOn((boolean) dataSnapshot.child(Constants.FIREBASE_NODE_REGISTERED_IDS)
                                .child(appliance.getId()).getValue());
                        mModuleAppliances.add(appliance);
                    }

//                    for (Module module : modules) {
//
//                        mUserModulesTitle.add(module.getTitle());
//
//                        DataSnapshot appliances = dataSnapshot.child(Constants.FIREBASE_NODE_MODULES)
//                                .child(module.getTitle()).child(Constants.FIREBASE_NODE_APPLIANCES);
//
//                        module.setChildrenCount(appliances.getChildrenCount());
//
//                        for (DataSnapshot snapshot : appliances.getChildren()) {
//                            Appliance appliance = snapshot.getValue(Appliance.class);
//                            module.addAppliances(appliance);
//                        }
//
//                        mUserModules.add(module);
//                    }

                    if (mRecyclerView.getAdapter() == null) {
                        ApplianceAdapter adapter = new ApplianceAdapter(mModuleAppliances, ModuleFragment.this);
                        mRecyclerView.setAdapter(adapter);
                    } else {
                        //Refill adapter
                        ((ApplianceAdapter) mRecyclerView.getAdapter()).refill(mModuleAppliances);
                    }

                    if (mRecyclerView.getAdapter().getItemCount() > 0) {
                        emptyListView.setVisibility(View.GONE);
                    } else {
                        emptyListView.setVisibility(View.VISIBLE);
                    }

//                    updateUI(usageOverview);

                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Toast.makeText(getContext(), "Failed to read value.", Toast.LENGTH_SHORT).show();
                }
            });

            final FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab_add_appliance);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), DialogActivity.class);
                    intent.putExtra(Constants.KEY_DIALOG_TYPE, Constants.KEY_APPLIANCE);
                    intent.putStringArrayListExtra(Constants.FIREBASE_NODE_REGISTERED_IDS,
                            mUserRegisteredIDs);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity(),
                                fab, getString(R.string.transition_dialog));
                        startActivityForResult(intent, ADD_APPLIANCE_REQUEST, options.toBundle());
                    } else {
                        startActivityForResult(intent, ADD_APPLIANCE_REQUEST);
                    }
                }
            });
        }

        return view;
    }

    @Override
    public void onApplianceClicked(Appliance appliance) {
        mAppliancesRef.child(appliance.getTitle()).setValue(appliance);
        mRef.child(Constants.FIREBASE_NODE_REGISTERED_IDS).child(appliance.getId()).setValue(appliance.getIsOn());
    }

    @Override
    public void onDeleteAppliance(final Appliance appliance) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Delete Appliance")
                .setMessage("Do you want to permanently delete this " + appliance.getTitle() + " appliance?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mAppliancesRef.child(appliance.getTitle()).removeValue();
                        mRef.child(Constants.FIREBASE_NODE_REGISTERED_IDS).child(appliance.getId()).removeValue();
                    }
                })
                .setNegativeButton("No", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case ADD_APPLIANCE_REQUEST:
                    saveNewAppliance(data);
                    break;
            }
        }
    }

    private void saveNewAppliance(Intent data) {
        Appliance appliance = new Appliance();
        appliance.setTitle(data.getStringExtra(DialogActivity.RESPONSE_CODE_APPLIANCE_TITLE));
        appliance.setTag(data.getStringExtra(DialogActivity.RESPONSE_CODE_APPLIANCE_TAG));
        appliance.setId(data.getStringExtra(DialogActivity.RESPONSE_CODE_APPLIANCE_ID));
        appliance.setIsOn(false);

        mRef.child(Constants.FIREBASE_NODE_REGISTERED_IDS).child(appliance.getId()).setValue(false);
        mAppliancesRef.child(appliance.getTitle()).setValue(appliance);

        mUserRegisteredIDs.add(appliance.getId());

        Toast.makeText(getContext(), "Appliance added successfully.", Toast.LENGTH_SHORT).show();
    }
}
