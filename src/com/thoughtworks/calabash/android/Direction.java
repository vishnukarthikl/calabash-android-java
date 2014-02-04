package com.thoughtworks.calabash.android;

public enum Direction {

    LEFT("left"), RIGHT("right"), UP("up"), DOWN("down");
    private final String direction;

    private Direction(String direction) {
        this.direction = direction;
    }

    public String getDirection() {
        return direction;
    }
}