package com.voidloop.home.adapter;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.voidloop.home.MainFragmentInterface;
import com.voidloop.home.ModuleFragment;
import com.voidloop.home.R;
import com.voidloop.home.model.Appliance;
import com.voidloop.home.model.Module;

import java.util.List;

/**
 * Created by ameh on 15/06/2017.
 */

public class ApplianceAdapter extends RecyclerView.Adapter<ApplianceAdapter.ViewHolder> {

    private List<Appliance> mAppliances;
    private ApplianceInterface listener;

    public interface ApplianceInterface {
        void onApplianceClicked(Appliance appliance);

        void onDeleteAppliance(Appliance appliance);
    }

    public ApplianceAdapter(List<Appliance> items, ModuleFragment listener) {
        mAppliances = items;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Create a new view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appliance_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.bindView(position);
    }

    @Override
    public int getItemCount() {
        return mAppliances.size();
    }

    public void refill(List<Appliance> items) {
        mAppliances.clear();
        mAppliances.addAll(items);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView applianceTitle;
        ImageView applianceImage;
        Switch applianceSwitch;
        Appliance appliance;

        ViewHolder(View itemView) {
            super(itemView);

            applianceTitle = (TextView) itemView.findViewById(R.id.appliance_title);
            applianceImage = (ImageView) itemView.findViewById(R.id.appliance_image);
            applianceSwitch = (Switch) itemView.findViewById(R.id.appliance_switch);

            itemView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onDeleteAppliance(appliance);
                }
            });

            itemView.setOnClickListener(this);
        }

        void bindView(int position) {
            appliance = mAppliances.get(position);

            applianceTitle.setText(appliance.getTitle());

//                if (appliance.getIsOn()) {
//                    applianceSwitch.setChecked(appliance.getIsOn());
//                }

            applianceSwitch.setChecked(appliance.getIsOn());

            switch (mAppliances.get(position).getTag()) {
                case "Light":
                    applianceImage.setImageResource(R.drawable.icon_globe_bulb);
                    break;

                case "Air Conditioner":
                    applianceImage.setImageResource(R.drawable.icon_air_conditioner);
                    break;

                case "Fridge":
                    applianceImage.setImageResource(R.drawable.icon_fridge);
                    break;
            }
        }

        @Override
        public void onClick(View v) {
            appliance.setIsOn(!appliance.getIsOn());
            applianceSwitch.setChecked(appliance.getIsOn());
            listener.onApplianceClicked(appliance);
        }
    }
}