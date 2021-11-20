package com.ft.mapp.home.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.ft.mapp.R;
import com.ft.mapp.home.models.BrandItem;

import java.util.ArrayList;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SectionedExpandableGridAdapter extends RecyclerView.Adapter<SectionedExpandableGridAdapter.ViewHolder> {

    //data array
    private ArrayList<Object> mDataArrayList;

    //context
    private final Context mContext;

    //listeners
    private final ItemClickListener mItemClickListener;
    private final SectionStateChangeListener mSectionStateChangeListener;

    //view type
    private static final int VIEW_TYPE_SECTION = R.layout.layout_section;
    private static final int VIEW_TYPE_ITEM = R.layout.layout_item;

    private BrandItem mSelectBrandItem = null;

    public SectionedExpandableGridAdapter(Context context, ArrayList<Object> dataArrayList,
                                          final GridLayoutManager gridLayoutManager, ItemClickListener itemClickListener,
                                          SectionStateChangeListener sectionStateChangeListener) {
        mContext = context;
        mItemClickListener = itemClickListener;
        mSectionStateChangeListener = sectionStateChangeListener;
        mDataArrayList = dataArrayList;

        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return isSection(position)?gridLayoutManager.getSpanCount():1;
            }
        });
    }

    private boolean isSection(int position) {
        return mDataArrayList.get(position) instanceof Section;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(viewType, parent, false), viewType);
    }

    public void setSelectBrandItem(BrandItem item) {
        mSelectBrandItem = item;
    }

    public BrandItem getSelectBrandItem() {
        return mSelectBrandItem;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        switch (holder.viewType) {
            case VIEW_TYPE_ITEM :
                final BrandItem item = (BrandItem) mDataArrayList.get(position);
                if (item.equals(mSelectBrandItem)) {
                    holder.itemTextView.setBackground(ContextCompat.getDrawable(mContext, R.drawable.white_rect));
                } else {
                    holder.itemTextView.setBackgroundColor(mContext.getResources().getColor(R.color.desktopColorA));
                }
                holder.itemTextView.setText(item.getModel());
                holder.view.setOnClickListener(v -> {
                    mSelectBrandItem = item;
                    mItemClickListener.itemClicked(item);
                });
                break;
            case VIEW_TYPE_SECTION :
                final Section section = (Section) mDataArrayList.get(position);
                holder.sectionTextView.setText(section.getName());
                holder.sectionTextView.setOnClickListener(
                        v -> mItemClickListener.itemClicked(section));
                holder.sectionToggleButton.setChecked(section.isExpanded);
                holder.sectionToggleButton.setOnCheckedChangeListener(
                        (buttonView, isChecked) -> mSectionStateChangeListener.onSectionStateChanged(section, isChecked));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mDataArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (isSection(position))
            return VIEW_TYPE_SECTION;
        else return VIEW_TYPE_ITEM;
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {

        //common
        View view;
        int viewType;

        //for section
        TextView sectionTextView;
        ToggleButton sectionToggleButton;

        //for item
        TextView itemTextView;

        public ViewHolder(View view, int viewType) {
            super(view);
            this.viewType = viewType;
            this.view = view;
            if (viewType == VIEW_TYPE_ITEM) {
                itemTextView = view.findViewById(R.id.text_item);
            } else {
                sectionTextView = view.findViewById(R.id.text_section);
                sectionToggleButton = view.findViewById(R.id.toggle_button_section);
            }
        }
    }
}
