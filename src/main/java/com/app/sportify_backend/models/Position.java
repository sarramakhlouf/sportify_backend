package com.app.sportify_backend.models;

public enum Position {
    GK("Goalkeeper"),
    LB("Left Back"),
    RB("Right Back"),
    CDM("Center Defensive Midfielder"),
    LW("Left Wing"),
    RW("Right Wing"),
    CF("Center Forward");

    private final String displayName;

    Position(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}