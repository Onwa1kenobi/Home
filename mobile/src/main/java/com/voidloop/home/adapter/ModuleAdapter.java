package com.voidloop.home.adapter;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.voidloop.home.MainFragment;
import com.voidloop.home.MainFragmentInterface;
import com.voidloop.home.R;
import com.voidloop.home.model.Appliance;
import com.voidloop.home.model.Module;
import com.voidloop.home.util.Constants;

import java.util.List;

/**
 * Created by ameh on 15/06/2017.
 */

public class ModuleAdapter extends RecyclerView.Adapter<ModuleAdapter.ViewHolder> {

    private List<Module> mModules;
    private ModuleInterface listener;
    private DataSnapshot mDataSnapshot;

    public interface ModuleInterface {
        void onModuleClicked(CardView container, Module module, int index);

        void onDeleteModule(Module module);
    }

    public ModuleAdapter(List<Module> items, MainFragment listener, DataSnapshot snapshot) {
        mModules = items;
        this.listener = listener;
        mDataSnapshot = snapshot;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Create a new view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_module_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.bindView(position);
    }

    @Override
    public int getItemCount() {
        return mModules.size();
    }

    public void refill(List<Module> items, DataSnapshot dataSnapshot) {
        mDataSnapshot = dataSnapshot;
        mModules.clear();
        mModules.addAll(items);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView moduleTitle, runningAppliances;
        ImageView moduleImage;
        CardView container;
        int index;
        Module module;

        ViewHolder(View itemView) {
            super(itemView);

            moduleTitle = (TextView) itemView.findViewById(R.id.module_title);
            runningAppliances = (TextView) itemView.findViewById(R.id.module_running_items);
            moduleImage = (ImageView) itemView.findViewById(R.id.module_image);
            container = (CardView) itemView.findViewById(R.id.module_container);

            itemView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onDeleteModule(module);
                }
            });

            itemView.setOnClickListener(this);
        }

        void bindView(int position) {
            index = position;
            module = mModules.get(position);
            moduleTitle.setText(module.getTitle());

            long moduleApplianceCount = module.getChildrenCount();
            int runningAppliancesCount = 0;

            for (Appliance appliance : module.getAppliances()) {
//                if (appliance.getIsOn()) {
//                    runningAppliancesCount += 1;
//                }

                if ((boolean) mDataSnapshot.child(Constants.FIREBASE_NODE_REGISTERED_IDS)
                        .child(appliance.getId()).getValue()) {
                    runningAppliancesCount += 1;
                }
            }

            runningAppliances.setText("" + runningAppliancesCount + "/" + moduleApplianceCount + " is running");

            switch (mModules.get(position).getTitle()) {
                case "Bedroom":
                    moduleImage.setImageResource(R.drawable.icon_bed_room);
                    break;

                case "Kitchen":
                    moduleImage.setImageResource(R.drawable.icon_kitchen);
                    break;

                case "Living Room":
                    moduleImage.setImageResource(R.drawable.icon_living_room);
                    break;
            }
        }

        @Override
        public void onClick(View v) {
            listener.onModuleClicked(container, module, index);
        }
    }
}