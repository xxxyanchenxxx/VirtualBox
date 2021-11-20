package com.ft.mapp.home.adapters;

public class Section {
    private final String name;
    public boolean isExpanded;

    public Section(String name) {
        this.name = name;
        isExpanded = false;
    }
    public String getName() {
        return name;
    }
}
