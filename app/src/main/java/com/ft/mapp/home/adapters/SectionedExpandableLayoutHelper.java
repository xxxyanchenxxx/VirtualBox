package com.ft.mapp.home.adapters;

import android.content.Context;

import com.ft.mapp.home.models.BrandItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SectionedExpandableLayoutHelper implements SectionStateChangeListener {

    //data list
    private LinkedHashMap<Section, ArrayList<BrandItem>>
            mSectionDataMap = new LinkedHashMap<>();
    private ArrayList<Object> mDataArrayList = new ArrayList<Object>();

    //section map
    //TODO : look for a way to avoid this
    private HashMap<String, Section> mSectionMap = new HashMap<>();

    //adapter
    private SectionedExpandableGridAdapter mSectionedExpandableGridAdapter;

    //recycler view
    RecyclerView mRecyclerView;

    public SectionedExpandableLayoutHelper(Context context, RecyclerView recyclerView, ItemClickListener itemClickListener,
                                           int gridSpanCount) {

        //setting the recycler view
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, gridSpanCount);
        recyclerView.setLayoutManager(gridLayoutManager);
        mSectionedExpandableGridAdapter = new SectionedExpandableGridAdapter(context, mDataArrayList,
                gridLayoutManager, itemClickListener, this);
        recyclerView.setAdapter(mSectionedExpandableGridAdapter);

        mRecyclerView = recyclerView;
    }

    public void notifyDataSetChanged() {
        //TODO : handle this condition such that these functions won't be called if the recycler view is on scroll
        generateDataList();
        mSectionedExpandableGridAdapter.notifyDataSetChanged();
    }

    public void setSelectBrandItem(BrandItem item) {
        mSectionedExpandableGridAdapter.setSelectBrandItem(item);
    }

    public BrandItem getSelectBrandItem() {
        return mSectionedExpandableGridAdapter.getSelectBrandItem();
    }

    public void addSection(String section, boolean isExpanded, ArrayList<BrandItem> items) {
        Section newSection = new Section(section);
        newSection.isExpanded = isExpanded;
        mSectionMap.put(section, newSection);
        mSectionDataMap.put(newSection, items);
    }

    public void addSection(String section, ArrayList<BrandItem> items) {
        addSection(section, false, items);
    }

    public void addItem(String section, BrandItem item) {
        mSectionDataMap.get(mSectionMap.get(section)).add(item);
    }

    public void removeItem(String section, BrandItem item) {
        mSectionDataMap.get(mSectionMap.get(section)).remove(item);
    }

    public void removeSection(String section) {
        mSectionDataMap.remove(mSectionMap.get(section));
        mSectionMap.remove(section);
    }

    private void generateDataList () {
        mDataArrayList.clear();
        for (Map.Entry<Section, ArrayList<BrandItem>> entry : mSectionDataMap.entrySet()) {
            Section key;
            mDataArrayList.add((key = entry.getKey()));
            if (key.isExpanded)
                mDataArrayList.addAll(entry.getValue());
        }
    }

    @Override
    public void onSectionStateChanged(Section section, boolean isOpen) {
        if (!mRecyclerView.isComputingLayout()) {
            section.isExpanded = isOpen;
            notifyDataSetChanged();
        }
    }
}
