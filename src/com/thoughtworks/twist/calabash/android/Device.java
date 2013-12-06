package com.thoughtworks.twist.calabash.android;

public class Device {
    private final String serial;
    private final String state;

    public Device(String serial, String state) {
        this.serial = serial;
        this.state = state;
    }

    public String getSerial() {
        return serial;
    }

    public String getState() {
        return state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Device device = (Device) o;

        if (!serial.equals(device.serial)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = serial.hashCode();
        result = 31 * result + state.hashCode();
        return result;
    }
}
