package com.ft.mapp.home.models;

import androidx.annotation.Nullable;

public class BrandItem {
    private final String brand;
    private final String model;

    public void setDisplayBrand(String displayBrand) {
        this.displayBrand = displayBrand;
    }

    public String getDisplayBrand() {
        return displayBrand;
    }

    private String displayBrand;

    public BrandItem(String brand, String model) {
        this.brand = brand;
        this.model = model;
    }

    public String getModel() {
        return model;
    }

    public String getBrand() {
        return brand;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        BrandItem item = (BrandItem) obj;
        if (item != null) {
            return brand.equalsIgnoreCase(item.brand) && model.equalsIgnoreCase(item.model);
        }
        return false;
    }
}
