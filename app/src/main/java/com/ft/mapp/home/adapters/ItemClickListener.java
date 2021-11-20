package com.ft.mapp.home.adapters;

import com.ft.mapp.home.models.BrandItem;

public interface ItemClickListener {
    void itemClicked(BrandItem item);
    void itemClicked(Section section);
}
