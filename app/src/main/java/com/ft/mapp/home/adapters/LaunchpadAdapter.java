package com.ft.mapp.home.adapters;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ft.mapp.R;
import com.ft.mapp.home.AppDetailActivity;
import com.ft.mapp.home.models.AppData;
import com.ft.mapp.home.models.MultiplePackageAppData;
import com.ft.mapp.home.models.PackageAppData;
import com.ft.mapp.widgets.LauncherIconView;
import com.ft.mapp.utils.VUiKit;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author Lody
 */
public class LaunchpadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private LayoutInflater mInflater;
    private List<AppData> mList;
    private OnAppClickListener mAppClickListener;
    private OnAppClickListener mMoreClickListener;
    private Context mContext;

    public LaunchpadAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    public void add(AppData model) {
        if (mList == null) {
            mList = new ArrayList<>();
        }
        int insertPos = mList.size() - 1;
        if (insertPos < 0) {
            insertPos = 0;
        }
        mList.add(insertPos, model);
        notifyItemInserted(insertPos);
    }

    public void replace(int index, AppData data) {
        mList.set(index, data);
        notifyItemChanged(index);
    }

    public void remove(AppData data) {
        if (mList.remove(data)) {
            notifyDataSetChanged();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE.ITEM_TYPE_NORMAL.ordinal()) {
            return new ViewHolder(mInflater.inflate(R.layout.item_launcher_app,  parent, false));
        } else {
            return new AddViewHolder(mInflater.inflate(R.layout.item_launcher_add, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder) {
            ViewHolder viewHolder = (ViewHolder) holder;
            AppData data = mList.get(position);
            viewHolder.iconView.setImageDrawable(data.getIcon());
            viewHolder.nameView.setText(data.getName());
            holder.itemView.setOnClickListener(v -> {
                if (mAppClickListener != null) {
                    mAppClickListener.onAppClick(holder.itemView, position, data);
                }
            });
            viewHolder.moreIv.setOnClickListener(v -> {
                if (mMoreClickListener != null) {
                    mMoreClickListener.onAppClick(viewHolder.moreIv, position, data);
                } else {
                    gotoAppDetail(data);
                }
            });
            if (data instanceof MultiplePackageAppData) {
                MultiplePackageAppData multipleData = (MultiplePackageAppData) data;
                String name = data.getName() + "-" + (multipleData.userId + 1);
                viewHolder.nameView.setText(name);
            }
            if (data.isLoading()) {
                startLoadingAnimation(viewHolder.iconView);
            } else {
                viewHolder.iconView.setProgress(100, false);
            }
        } else if (holder instanceof AddViewHolder) {
            AddViewHolder addViewHolder = (AddViewHolder) holder;
            addViewHolder.moreIv.setOnClickListener(v -> {
                if (mAppClickListener != null) {
                    mAppClickListener.onAppClick(addViewHolder.moreIv, position, null);
                }
            });
            holder.itemView.setOnClickListener(v -> {
                if (mAppClickListener != null) {
                    mAppClickListener.onAppClick(holder.itemView, position, null);
                }
            });
        }
    }

    private void startLoadingAnimation(LauncherIconView iconView) {
        iconView.setProgress(40, true);
        VUiKit.postDelayed(900, () -> iconView.setProgress(80, true));
    }

    @Override
    public int getItemViewType(int position) {
        if (position < 0 || position >= mList.size()) {
            return ITEM_TYPE.ITEM_TYPE_NORMAL.ordinal();
        }
        String name = mList.get(position).getName();
        if (TextUtils.isEmpty(name)) {
            return ITEM_TYPE.ITEM_TYPE_ADD.ordinal();
        }
        return ITEM_TYPE.ITEM_TYPE_NORMAL.ordinal();
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    public List<AppData> getList() {
        return mList;
    }

    public void setList(List<AppData> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    public void setAppClickListener(OnAppClickListener clickListener) {
        this.mAppClickListener = clickListener;
    }

    public void setMoreClickListener(OnAppClickListener clickListener) {
        this.mMoreClickListener = clickListener;
    }

    public void moveItem(int pos, int targetPos) {
        AppData model = mList.remove(pos);
        mList.add(targetPos, model);
        notifyItemMoved(pos, targetPos);
    }

    public void refresh(AppData model) {
        int index = mList.indexOf(model);
        if (index >= 0) {
            notifyItemChanged(index);
        }
    }

    public interface OnAppClickListener {
        void onAppClick(View view, int position, AppData model);
    }

    public static enum ITEM_TYPE {
        ITEM_TYPE_NORMAL,
        ITEM_TYPE_ADD
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public int color;
        LauncherIconView iconView;
        TextView nameView;
        AppCompatImageView moreIv;

        ViewHolder(View itemView) {
            super(itemView);
            iconView = itemView.findViewById(R.id.item_app_icon);
            nameView = itemView.findViewById(R.id.item_app_name);
            moreIv = itemView.findViewById(R.id.item_app_more_iv);
        }
    }

    public static class AddViewHolder extends RecyclerView.ViewHolder {
        AppCompatImageView moreIv;

        AddViewHolder(View itemView) {
            super(itemView);
            moreIv = itemView.findViewById(R.id.item_app_more_iv);
        }
    }

    private void gotoAppDetail(AppData data) {
        try {
            if (data instanceof PackageAppData) {
                PackageAppData appData = (PackageAppData) data;
                appData.isFirstOpen = false;
                AppDetailActivity
                        .gotoAppDetail((Activity) mContext, appData.name, appData.packageName, 0, appData.icon);
            } else if (data instanceof MultiplePackageAppData) {
                MultiplePackageAppData multipleData = (MultiplePackageAppData) data;
                multipleData.isFirstOpen = false;
                AppDetailActivity.gotoAppDetail((Activity) mContext, multipleData.name, multipleData.appInfo.packageName,
                        multipleData.userId, multipleData.icon);
            } else {
                AppDetailActivity.gotoAppDetail((Activity) mContext, data.getName(), data.getPackageName(), 0, data.getIcon());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
