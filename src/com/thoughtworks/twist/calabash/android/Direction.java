package com.thoughtworks.twist.calabash.android;

public enum Direction {

	LEFT("left"), RIGHT("right");

	private final String direction;

	private Direction(String direction) {
        this.direction = direction;
	}

	public String getDirection() {
		return direction;
	}
}