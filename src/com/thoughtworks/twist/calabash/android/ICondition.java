package com.thoughtworks.twist.calabash.android;

public abstract class ICondition {
    private final String description;

    public ICondition(String description){
        this.description = description;
    }

    protected ICondition() {
        description = null;
    }

    public abstract boolean test() throws CalabashException;

    public String getDescription() {
        return description;
    }

}
