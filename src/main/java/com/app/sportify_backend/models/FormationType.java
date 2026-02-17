package com.app.sportify_backend.models;

public enum FormationType {
    FORMATION_2_1_2_1("2-1-2-1"),
    FORMATION_3_2_1("3-2-1"),
    FORMATION_2_2_2("2-2-2");

    private final String displayName;

    FormationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}