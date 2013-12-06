package com.thoughtworks.twist.calabash.android;

public class CalabashException extends Exception {

    public CalabashException(String message, Exception e) {
        super(message, e);
    }

    public CalabashException(String message) {
        super(message);
    }
}
