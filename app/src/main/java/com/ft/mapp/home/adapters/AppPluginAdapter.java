package com.ft.mapp.home.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ft.mapp.R;
import com.ft.mapp.home.models.PluginInfo;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class AppPluginAdapter extends RecyclerView.Adapter<AppPluginAdapter.ViewHolder> {

    private List<PluginInfo> mPluginList;
    private onItemClickListener mClickListener;
    private Context mContext;

    public AppPluginAdapter(Context context) {
        this.mContext = context;

    }

    public List<PluginInfo> getList() {
        return mPluginList;
    }

    public void setList(List<PluginInfo> models) {
        this.mPluginList = models;
        notifyDataSetChanged();
    }

    public void add(PluginInfo info) {
        if (mPluginList == null) {
            mPluginList = new ArrayList<>();
        }
        mPluginList.add(info);
        notifyDataSetChanged();
    }

    public void clear() {
        if (mPluginList != null) {
            mPluginList.clear();
        }
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AppPluginAdapter.ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_app_plugin, null));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PluginInfo info = mPluginList.get(position);
        holder.iconView.setImageResource(info.iconId);
        holder.nameView.setText(info.name);
        holder.descTV.setText(info.desc);
        holder.itemView.setOnClickListener(v -> {
            mClickListener.onItemClick(info, position);
        });
    }

    @Override
    public int getItemCount() {
        return mPluginList == null ? 0 : mPluginList.size();
    }

    public void setOnItemClickListener(onItemClickListener listener) {
        mClickListener = listener;
    }

    public interface onItemClickListener {

        void onItemClick(PluginInfo pluginInfo, int position);

    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView iconView;
        private TextView nameView;
        private TextView descTV;

        ViewHolder(View itemView) {
            super(itemView);
            iconView = itemView.findViewById(R.id.plugin_icon_iv);
            nameView = itemView.findViewById(R.id.plugin_function_tv);
            descTV = itemView.findViewById(R.id.plugin_desc_tv);
        }
    }
}
