package com.ft.mapp.home.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.ft.mapp.R;
import com.ft.mapp.home.models.AppLaunchData;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AppLaunchCfgAdapter extends RecyclerView.Adapter<AppLaunchCfgAdapter.ViewHolder> {

    private List<AppLaunchData> mAppList;
    private onItemClickListener mClickListener;
    private onItemSwitchListener mSwitchListener;
    private Context mContext;

    public AppLaunchCfgAdapter(Context context) {
        this.mContext = context;
    }

    public List<AppLaunchData> getList() {
        return mAppList;
    }

    public void setList(List<AppLaunchData> models) {
        this.mAppList = models;
        notifyDataSetChanged();
    }

    public void add(AppLaunchData info) {
        if (mAppList == null) {
            mAppList = new ArrayList<>();
        }
        mAppList.add(info);
        notifyDataSetChanged();
    }

    public void clear() {
        if (mAppList != null) {
            mAppList.clear();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AppLaunchCfgAdapter.ViewHolder(
                LayoutInflater.from(mContext).inflate(R.layout.item_app_launch_cfg, null));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppLaunchData info = mAppList.get(position);
        holder.iconView.setImageDrawable(info.icon);
        holder.nameView.setText(info.name);
        holder.pluginSwitch.setChecked(info.monopoly == 1);
        holder.pluginSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mSwitchListener != null) {
                mSwitchListener.onItemSwitch(info, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mAppList == null ? 0 : mAppList.size();
    }

    public void setOnItemClickListener(onItemClickListener listener) {
        mClickListener = listener;
    }

    public void setOnItemSwitchListener(onItemSwitchListener listener) {
        mSwitchListener = listener;
    }

    public interface onItemClickListener {

        void onItemClick(AppLaunchData pluginInfo, int position);

    }

    public interface onItemSwitchListener {

        void onItemSwitch(AppLaunchData pluginInfo, boolean isChecked);

    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView iconView;
        private TextView nameView;
        private Switch pluginSwitch;

        ViewHolder(View itemView) {
            super(itemView);
            iconView = itemView.findViewById(R.id.app_icon_iv);
            nameView = itemView.findViewById(R.id.app_name_tv);
            pluginSwitch = itemView.findViewById(R.id.app_launch_switch);
        }
    }
}
